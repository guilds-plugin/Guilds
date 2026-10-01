/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.commands.console

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.*
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.database.DatabaseBackend
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.persistence.PersistenceCoordinator
import me.glaremasters.guilds.utils.BackupUtils
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.LoggingUtils
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandConsole : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var actionHandler: ActionHandler
    @Dependency lateinit var arenaHandler: ArenaHandler
    @Dependency lateinit var challengeHandler: ChallengeHandler
    @Dependency lateinit var cooldownHandler: CooldownHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("console backup")
    @Description("{@@descriptions.console-backup}")
    @CommandPermission(Constants.ADMIN_PERM)
    fun backup(issuer: CommandIssuer) {
        if (issuer.isPlayer) {
            throw ExpectationNotMet(Messages.ERROR__CONSOLE_COMMAND)
        }

        currentCommandIssuer.sendInfo(Messages.BACKUP__WARNING)
        actionHandler.addAction(issuer.getIssuer(), object : ConfirmAction {
            override fun accept() {
                guilds.commandManager.getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.BACKUP__STARTED)
                Guilds.newChain<Any>().async {
                    try {
                        BackupUtils.zipDir("guilds-backup-" + System.currentTimeMillis() + ".zip", guilds.dataFolder.path)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.sync { guilds.commandManager.getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.BACKUP__FINISHED) }.execute()
                actionHandler.removeAction(issuer.getIssuer())
            }

            override fun decline() {
                actionHandler.removeAction(issuer.getIssuer())
                currentCommandIssuer.sendInfo(Messages.BACKUP__CANCELLED)
            }
        })
    }

    /**
     * Copies every collection to another backend, then swaps to it.
     *
     * <p>Three things this has to get right, each of which was a defect before.
     *
     * <p>A failure must leave the plugin running on the backend it already had. So the flag and the permit
     * are claimed before anything can throw, the snapshot is taken before the destination is opened, and the
     * old adapter is closed only after the new one is published.
     *
     * <p>The destination must be complete before it is published, or the operator loses data and is told it
     * worked. So every collection is confirmed written, and the reconciliation that follows confirms the
     * destination matches the live plugin rather than the snapshot.
     *
     * <p>A failure must not leave a destructive action armed. The {@code ConfirmAction} is removed as the
     * first thing {@code accept} does, because every check after it throws and TaskChain skips the trailing
     * steps when one does.
     */
    @Subcommand("console migrate")
    @Description("{@@descriptions.console-migrate}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("%new-backend")
    @CommandCompletion("@sources")
    fun migrate(issuer: CommandIssuer, @Values("@sources") toBackend: String) {
        if (issuer.isPlayer) {
            throw ExpectationNotMet(Messages.ERROR__CONSOLE_COMMAND)
        }

        currentCommandIssuer.sendInfo(Messages.MIGRATE__WARNING)
        actionHandler.addAction(issuer.getIssuer(), object : ConfirmAction {
            override fun accept() {
                // First statement, before anything that can throw. Every one of the checks below throws
                // `ExpectationNotMet`, and TaskChain runs the rest of the chain only when a step returns
                // normally, so an action left armed here would sit waiting for a `/guilds confirm` and
                // re-run the whole migration with a stale capture behind it.
                actionHandler.removeAction(issuer.getIssuer())

                val resolvedBackend = DatabaseBackend.getByBackendName(toBackend)
                    ?: throw ExpectationNotMet(Messages.MIGRATE__INVALID_BACKEND)
                val coordinator = guilds.persistenceCoordinator
                    ?: throw ExpectationNotMet(Messages.MIGRATE__FAILED)
                val gate = coordinator.gate

                // Claimed on the main thread, where the command handler runs. The flag gates the autosave
                // and the NotMigrating commands, both of which read it from other threads, so setting it
                // from a worker left a window where it was not yet visible.
                if (!gate.beginMigration()) {
                    throw ExpectationNotMet(Messages.MIGRATE__BUSY)
                }
                if (gate.isShuttingDown) {
                    gate.endMigration()
                    throw ExpectationNotMet(Messages.MIGRATE__FAILED)
                }

                val commandIssuer = guilds.commandManager.getCommandIssuer(issuer.getIssuer())

                // Captured synchronously. A nested `newChain().sync { capture() }.execute()` does not block:
                // it queues for the next tick and returns, so the result is read before the capture runs and
                // is always null.
                val captured = try {
                    coordinator.capture()
                } catch (ex: RuntimeException) {
                    gate.endMigration()
                    LoggingUtils.severe("Migration to ${resolvedBackend.backendName} failed: could not read plugin data.", ex)
                    commandIssuer.sendInfo(Messages.MIGRATE__FAILED)
                    return
                }

                val outcome = MigrationOutcome()

                Guilds.newChain<Any>().async {
                    // Never propagates out of this step, so the chain always reaches the one below.
                    outcome.write(guilds, gate, coordinator, resolvedBackend, captured)
                }.sync {
                    // Main thread: nothing can run between the reconciliation and the publication.
                    outcome.publish(coordinator, captured, resolvedBackend)
                }.sync {
                    // Main thread: the destination holds the snapshot, not what the plugin looks like now.
                    outcome.catchUp(coordinator, gate)
                }.async {
                    outcome.writeCatchUp(coordinator, gate)
                }.sync {
                    // Reached only after the catch-up write returned, so "complete" means the changes made
                    // during the migration are on the new backend. `finish` in a `finally` because every
                    // step above is total, and this is the one place the permit and the flag come back.
                    try {
                        outcome.report(commandIssuer, resolvedBackend)
                    } finally {
                        outcome.finish(gate)
                        gate.endMigration()
                    }
                }.execute()
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.MIGRATE__CANCELLED)
                actionHandler.removeAction(issuer.getIssuer())
            }
        })
    }

    @Subcommand("console unclaimall")
    @Description("{@@descriptions.console-unclaim-all}")
    @CommandPermission(Constants.ADMIN_PERM)
    fun unclaim(issuer: CommandIssuer) {
        if (issuer.isPlayer) {
            throw ExpectationNotMet(Messages.ERROR__CONSOLE_COMMAND)
        }

        if (!ClaimUtils.isEnabled(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_WARNING)
        actionHandler.addAction(issuer.getIssuer(), object : ConfirmAction {
            override fun accept() {
                val wrapper = WorldGuardWrapper.getInstance()
                guildHandler.guilds.values.forEach { guild ->
                    if (ClaimUtils.checkAlreadyExist(wrapper, guild)) {
                        ClaimUtils.removeClaim(wrapper, guild)
                    }
                }
                currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_SUCCESS)
                // Without this the action stays registered, so every later /guilds confirm silently re-runs a
                // destructive unclaim-all.
                actionHandler.removeAction(issuer.getIssuer())
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_CANCELLED)
                actionHandler.removeAction(issuer.getIssuer())
            }
        })
    }

}
