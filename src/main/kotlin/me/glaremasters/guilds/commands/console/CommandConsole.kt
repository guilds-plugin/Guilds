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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.database.DatabaseAdapter
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
     * The shape of this is the fix. It used to hand the adapters the live collections from inside the
     * async block, so the migration read `guildHandler.guilds`, the arena map and the challenge set
     * while the main thread kept changing them. Three consequences followed from that:
     *
     * - A `ConcurrentModificationException` mid-migration matched neither `catch` clause, so
     *   `isMigrating` was left `true` forever: the autosave skipped on every tick after it, and every
     *   command carrying `NotMigrating` was refused for the rest of the session.
     * - TaskChain aborts the rest of the chain when a step throws, so the `.sync` step that calls
     *   `removeAction` never ran. The `ConfirmAction` stayed registered, and the next `/guilds confirm`
     *   silently started the whole migration again. The `unclaimall` command below carries a comment
     *   about exactly this hazard.
     * - `guilds.database` was swapped and the old pool closed while an autosave that had already passed
     *   its `isMigrating` check could still be writing to it.
     *
     * Now: the gate is claimed synchronously so the flag is never briefly unset, the snapshot is taken
     * on the main thread, the write happens under the same permit the autosave respects, and every exit
     * path releases both in a `finally`. A failure leaves the old backend in place and open, and the
     * swap only happens once the new backend has been confirmed to hold every collection.
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
                val resolvedBackend = DatabaseBackend.getByBackendName(toBackend)
                    ?: throw ExpectationNotMet(Messages.MIGRATE__INVALID_BACKEND)
                val coordinator = guilds.persistenceCoordinator
                    ?: throw ExpectationNotMet(Messages.MIGRATE__FAILED)
                val gate = coordinator.gate

                // Claimed here, on the main thread, rather than inside the async block. The flag gates
                // both the autosave and the NotMigrating commands, and both of those run on other
                // threads; setting it from a worker left a window where the flag was not yet visible.
                if (!gate.beginMigration()) {
                    throw ExpectationNotMet(Messages.MIGRATE__BUSY)
                }

                if (gate.isShuttingDown) {
                    gate.endMigration()
                    throw ExpectationNotMet(Messages.MIGRATE__FAILED)
                }

                // Removed up front, not in a `.sync` step at the end. If the async block throws, TaskChain
                // aborts the chain and the trailing step never runs, which is how a failed migration used
                // to leave a registered action that silently re-ran on the next `/guilds confirm`.
                actionHandler.removeAction(issuer.getIssuer())

                val commandIssuer = guilds.commandManager.getCommandIssuer(issuer.getIssuer())

                // Captured here, synchronously, on the main thread.
                //
                // The obvious alternative is a nested `newChain().sync { capture() }.execute()` inside the
                // async block, and that is broken: `execute()` does not block. It hands the task to the
                // next tick and returns, so the read of the captured value happens before the capture does
                // and is always null. The gate is already claimed at this point, so nothing can be writing,
                // and this is the one place the live collections may be read.
                val captured = try {
                    coordinator.capture()
                } catch (ex: RuntimeException) {
                    gate.endMigration()
                    LoggingUtils.severe("Migration to ${resolvedBackend.backendName} failed: could not read plugin data.", ex)
                    commandIssuer.sendInfo(Messages.MIGRATE__FAILED)
                    return
                }

                // Written on a worker, with the outcome reported back on the main thread.
                //
                // `migrated` is atomic because it is written on the worker and read on the main thread in
                // the `.sync` step below. The TaskChain handoff happens to supply a happens-before edge
                // today, but the same class of bug as the non-volatile `isMigrating` flag this change
                // removed, and invisible if the handoff ever changes.
                val migrated = AtomicBoolean(false)
                val failureMessage = AtomicReference<Messages?>(null)

                Guilds.newChain<Any>().async {
                    var resolvedAdapter: DatabaseAdapter? = null
                    var swapped = false
                    try {
                        val adapter = guilds.database.cloneWith(resolvedBackend)
                        resolvedAdapter = adapter

                        if (!adapter.isConnected) {
                            failureMessage.set(Messages.MIGRATE__CONNECTION_FAILED)
                            return@async
                        }

                        // The permit is held across the write and the swap, so no autosave can be writing to
                        // the old backend while it is being closed. The gate's flag was claimed earlier, so
                        // the autosave has been skipping for the whole time up to here.
                        if (!gate.acquireWriter(MIGRATION_LOCK_TIMEOUT_MS)) {
                            failureMessage.set(Messages.MIGRATE__BUSY)
                            return@async
                        }

                        try {
                            if (gate.isShuttingDown) {
                                failureMessage.set(Messages.MIGRATE__FAILED)
                                return@async
                            }

                            // Checked, not assumed. Every collection is written inside its own try, so a
                            // failure on the new backend is otherwise invisible here: the swap would go
                            // ahead, the old pool would close, and the operator would be told it worked
                            // while the new backend held nothing.
                            val failures = ArrayList<String>()
                            if (!coordinator.writeTo(adapter, captured, failures)) {
                                LoggingUtils.severe(
                                    "Migration to ${resolvedBackend.backendName} failed for: ${failures.joinToString("; ")}",
                                )
                                failureMessage.set(Messages.MIGRATE__FAILED)
                                return@async
                            }

                            val old = guilds.database
                            guilds.database = adapter
                            old.close()
                            swapped = true
                            migrated.set(true)
                        } finally {
                            gate.releaseWriter()
                        }
                    } catch (ex: IllegalArgumentException) {
                        LoggingUtils.warn("Migration to ${resolvedBackend.backendName} was refused: it is the backend already in use.", ex)
                        failureMessage.set(Messages.MIGRATE__SAME_BACKEND)
                    } catch (ex: Exception) {
                        LoggingUtils.severe("Migration to ${resolvedBackend.backendName} failed. The previous backend is still in use.", ex)
                        failureMessage.set(Messages.MIGRATE__FAILED)
                    } finally {
                        // Close the half-built adapter on every path that did not swap it in, so a failed
                        // attempt does not leak a connection pool. The old backend is never touched on those
                        // paths, so the plugin keeps running on it.
                        if (!swapped) {
                            resolvedAdapter?.close()
                        }
                        gate.endMigration()
                    }
                }.sync {
                    // Always runs: the async block above returns normally on every path, including the
                    // early returns, so TaskChain never aborts the chain and this step is never skipped.
                    val failure = failureMessage.get()
                    when {
                        failure != null -> commandIssuer.sendInfo(failure)
                        migrated.get() -> commandIssuer.sendInfo(Messages.MIGRATE__COMPLETE, "{amount}", guildHandler.guildsSize.toString())
                        else -> commandIssuer.sendInfo(Messages.MIGRATE__FAILED)
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
                // Without this the action stays registered, so every later /guilds confirm silently
                // re-runs a destructive unclaim-all.
                actionHandler.removeAction(issuer.getIssuer())
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_CANCELLED)
                actionHandler.removeAction(issuer.getIssuer())
            }
        })
    }

    private companion object {
        /**
         * How long a migration waits for an in-flight save before giving up.
         *
         * Bounded so a wedged save cannot make the command hang indefinitely.
         */
        const val MIGRATION_LOCK_TIMEOUT_MS = 10_000L
    }
}
