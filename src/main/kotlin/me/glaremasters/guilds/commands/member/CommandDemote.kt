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

package me.glaremasters.guilds.commands.member

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.RoleUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandDemote : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("demote")
    @Description("{@@descriptions.demote}")
    @CommandPermission(Constants.BASE_PERM + "demote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    fun demote(player: Player, @Conditions("perm:perm=DEMOTE") guild: Guild, @Values("@members") @Single target: String) {
        val user = Bukkit.getOfflinePlayer(target)

        if (user.name.equals(player.name)) {
            throw ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE)
        }

        if (!RoleUtils.inGuild(guild, user)) {
            throw ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", target)
        }

        val asMember = guild.getMember(user.uniqueId)

        if (RoleUtils.sameRole(guild, player, user)) {
            throw ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE)
        }

        if (RoleUtils.isLowest(guildHandler, asMember) || RoleUtils.isLower(asMember, guild.getMember(player.uniqueId))) {
            throw ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE)
        }

        RoleUtils.demote(guildHandler, guild, user)

        val oldRole = RoleUtils.getPreDemotedRoleName(guildHandler, asMember)
        val newRole = RoleUtils.getCurrentRoleName(asMember)

        currentCommandIssuer.sendInfo(Messages.DEMOTE__DEMOTE_SUCCESSFUL, "{player}", target, "{old}", oldRole, "{new}", newRole)

        if (!user.isOnline) {
            return
        }

        currentCommandManager.getCommandIssuer(user).sendInfo(Messages.DEMOTE__YOU_WERE_DEMOTED, "{old}", oldRole, "{new}", newRole)
    }
}