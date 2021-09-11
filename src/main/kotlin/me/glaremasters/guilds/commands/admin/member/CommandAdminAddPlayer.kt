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

package me.glaremasters.guilds.commands.admin.member

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.claim.ClaimEditor
import me.glaremasters.guilds.claim.ClaimUtils
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.Bukkit
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandAdminAddPlayer : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin addplayer")
    @Description("{@@descriptions.admin-addplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds @online")
    @Syntax("<%syntax> <player>")
    fun add(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, target: String) {
        val user = Bukkit.getOfflinePlayer(target)

        if (!user.hasPlayedBefore() && !user.isOnline) {
            throw ExpectationNotMet(Messages.ERROR__PLAYER_NO_EXIST)
        }

        if (guildHandler.getGuild(user) != null) {
            throw ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD)
        }

        val name = guild.name

        guild.addMember(user, guildHandler)

        if (user.isOnline) {
            currentCommandManager.getCommandIssuer(user).sendInfo(Messages.ADMIN__PLAYER_ADDED, "{guild}", name)
        }

        if (ClaimUtils.isEnable(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()

            for (claim in guild.claimedLand) {
                ClaimEditor.addMember(wrapper, claim, user.uniqueId)
            }
        }

        currentCommandIssuer.sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED, "{player}", user.name, "{guild}", name)
        guild.sendMessage(currentCommandManager, Messages.ADMIN__ADMIN_GUILD_ADD, "{player}", user.name)
    }
}
