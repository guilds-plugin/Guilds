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
package me.glaremasters.guilds.commands.admin.manage

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
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
internal class CommandAdminRemove : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var actionHandler: ActionHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var permission: Permission

    @Subcommand("admin remove")
    @Description("{@@descriptions.admin-remove}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    @Conditions("NotMigrating")
    fun remove(player: Player, @Flags("other") @Values("@guilds") guild: Guild) {
        val name = guild.name

        currentCommandIssuer.sendInfo(Messages.ADMIN__DELETE_WARNING, "{guild}", name)
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                val event = GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.ADMIN_DELETED)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    return
                }

                guild.members.forEach { member ->
                    guildHandler.removeFromChat(member.uuid)
                }

                guildHandler.removeGuildPermsFromAll(permission, guild)
                guildHandler.removeRolePermsFromAll(permission, guild)
                guildHandler.removeAlliesOnDelete(guild)
                guildHandler.notifyAllies(guild, guilds.commandManager)
                ClaimUtils.deleteWithGuild(guild, settingsManager)
                guild.sendMessage(currentCommandManager, Messages.LEAVE__GUILDMASTER_LEFT, "{player}", guild.guildMaster.name)
                guildHandler.removeGuild(guild)
                currentCommandIssuer.sendInfo(Messages.ADMIN__DELETE_SUCCESS, "{guild}", name)
                actionHandler.removeAction(player)
            }

            override fun decline() {
                actionHandler.removeAction(player)
            }
        })
    }
}
