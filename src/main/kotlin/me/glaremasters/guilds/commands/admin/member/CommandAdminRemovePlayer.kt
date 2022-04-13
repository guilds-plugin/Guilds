/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
package me.glaremasters.guilds.commands.admin.member

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildKickEvent
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import org.bukkit.Bukkit
import org.bukkit.entity.Player

// todo Fix the logic on this because what if you force remove the guild master?
@CommandAlias("%guilds")
internal class CommandAdminRemovePlayer : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin removeplayer")
    @Description("{@@descriptions.admin-removeplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<name>")
    fun remove(player: Player, target: String) {
        val user = Bukkit.getOfflinePlayer(target)
        val guild = guildHandler.getGuild(user) ?: throw ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST)
        val event = GuildKickEvent(player, guild, user, GuildKickEvent.Cause.ADMIN_KICKED)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        ClaimUtils.kickMember(user, player, guild, settingsManager)
        guild.removeMember(user)

        if (user.isOnline) {
            currentCommandManager.getCommandIssuer(user).sendInfo(Messages.ADMIN__PLAYER_REMOVED)
        }

        currentCommandIssuer.sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED, "{player}", user.name, "{guild}", guild.name)
        guild.sendMessage(currentCommandManager, Messages.ADMIN__ADMIN_GUILD_REMOVE, "{player}", user.name, "{guild}", guild.name)
    }
}
