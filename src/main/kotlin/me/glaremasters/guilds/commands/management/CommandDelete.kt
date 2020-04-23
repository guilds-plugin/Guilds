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

package me.glaremasters.guilds.commands.management

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.api.events.GuildRemoveEvent
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandDelete : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var permission: Permission
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var actionHandler: ActionHandler

    @Subcommand("delete")
    @Description("{@@descriptions.delete}")
    @CommandPermission(Constants.BASE_PERM + "delete")
    @Syntax("")
    @Conditions("NotMigrating")
    fun delete(player: Player, @Conditions("perm:perm=REMOVE_GUILD") guild: Guild) {
        currentCommandIssuer.sendInfo(Messages.DELETE__WARNING)
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                val event = GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.PLAYER_DELETED)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    return
                }

                guildHandler.removePermsFromAll(permission, guild, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC))
                guildHandler.removeAlliesOnDelete(guild)
                guildHandler.notifyAllies(guild, currentCommandManager)
                guild.sendMessage(currentCommandManager, Messages.LEAVE__GUILDMASTER_LEFT, "{player}", player.name)
                ClaimUtils.deleteWithGuild(guild, settingsManager)
                guildHandler.removeGuild(guild)
                currentCommandIssuer.sendInfo(Messages.DELETE__SUCCESSFUL, "{guild}", guild.name)
                actionHandler.removeAction(player)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.DELETE__CANCELLED)
                actionHandler.removeAction(player)
            }
        })
    }
}