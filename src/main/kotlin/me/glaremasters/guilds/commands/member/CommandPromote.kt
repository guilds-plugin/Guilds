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
internal class CommandPromote : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("promote")
    @Description("{@@descriptions.promote}")
    @CommandPermission(Constants.BASE_PERM + "promote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    fun promote(player: Player, @Conditions("perm:perm=PROMOTE") guild: Guild, @Values("@members") @Single target: String) {
        val user = Bukkit.getOfflinePlayer(target)

        if (user.name.equals(player.name)) {
            throw ExpectationNotMet(Messages.PROMOTE__CANT_PROMOTE)
        }

        if (!RoleUtils.inGuild(guild, user) && !RoleUtils.checkPromote(guild, user, player)) {
            throw ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", target)
        }

        if (RoleUtils.isOfficer(guild, user)) {
            throw ExpectationNotMet(Messages.PROMOTE__CANT_PROMOTE)
        }

        val asMember = guild.getMember(user.uniqueId)

        RoleUtils.promote(guildHandler, guild, user)

        val oldRole = RoleUtils.getPrePromotedRoleName(guildHandler, asMember)
        val newRole = RoleUtils.getCurrentRoleName(asMember)

        currentCommandIssuer.sendInfo(Messages.PROMOTE__PROMOTE_SUCCESSFUL, "{player}", target, "{old}", oldRole, "{new}", newRole)

        if (!user.isOnline) {
            return
        }

        currentCommandManager.getCommandIssuer(user).sendInfo(Messages.PROMOTE__YOU_WERE_PROMOTED, "{old}", oldRole, "{new}", newRole)
    }
}
