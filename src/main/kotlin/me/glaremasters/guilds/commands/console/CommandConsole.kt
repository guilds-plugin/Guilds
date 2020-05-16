/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Values
import java.io.IOException
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
import me.glaremasters.guilds.utils.BackupUtils
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
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

    @Subcommand("console migrate")
    @Description("{@@descriptions.console-migrate}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@sources")
    fun migrate(issuer: CommandIssuer, @Values("@sources") toBackend: String) {
        if (issuer.isPlayer) {
            throw ExpectationNotMet(Messages.ERROR__CONSOLE_COMMAND)
        }

        currentCommandIssuer.sendInfo(Messages.MIGRATE__WARNING)
        actionHandler.addAction(issuer.getIssuer(), object : ConfirmAction {
            override fun accept() {
                val resolvedBackend = DatabaseBackend.getByBackendName(toBackend) ?: throw ExpectationNotMet(Messages.MIGRATE__INVALID_BACKEND)
                Guilds.newChain<Any>().async {
                    try {
                        guildHandler.isMigrating = true
                        val resolvedAdapter = guilds.database.cloneWith(resolvedBackend)
                        if (!resolvedAdapter.isConnected) {
                            guildHandler.isMigrating = false
                            throw ExpectationNotMet(Messages.MIGRATE__CONNECTION_FAILED)
                        }

                        resolvedAdapter.guildAdapter.saveGuilds(guildHandler.guilds)
                        resolvedAdapter.arenaAdapter.saveArenas(arenaHandler.getArenas())
                        resolvedAdapter.cooldownAdapter.saveCooldowns(cooldownHandler.cooldowns.values)
                        resolvedAdapter.challengeAdapter.saveChallenges(challengeHandler.challenges)

                        val old = guilds.database
                        guilds.database = resolvedAdapter
                        old.close()

                        guildHandler.isMigrating = false
                    } catch (ex: IllegalArgumentException) {
                        guildHandler.isMigrating = false
                        throw ExpectationNotMet(Messages.MIGRATE__SAME_BACKEND)
                    } catch (ex: IOException) {
                        guildHandler.isMigrating = false
                        ex.printStackTrace()
                    }
                }.sync {
                    guilds.commandManager.getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.MIGRATE__COMPLETE, "{amount}", guildHandler.guildsSize.toString())
                    actionHandler.removeAction(issuer.getIssuer())
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

        if (!ClaimUtils.isEnable(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_WARNING)
        actionHandler.addAction(issuer.getIssuer(), object : ConfirmAction {
            override fun accept() {
                val wrapper = WorldGuardWrapper.getInstance()
                guildHandler.guilds.forEach { guild ->
                    if (ClaimUtils.checkAlreadyExist(wrapper, guild)) {
                        ClaimUtils.removeClaim(wrapper, guild)
                    }
                }
                currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_SUCCESS)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.UNCLAIM__ALL_CANCELLED)
                actionHandler.removeAction(issuer.getIssuer())
            }
        })
    }
}
