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
package me.glaremasters.guilds.commands.admin.claims

import ch.jalu.configme.SettingsManager
import co.aikar.commands.ACFBukkitUtil
import co.aikar.commands.BaseCommand
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
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandAdminClaim : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin claim")
    @Description("{@@descriptions.admin-claim}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun claim(player: Player, @Flags("other") @Values("@guilds") guild: Guild) {
        if (!ClaimUtils.isEnabled(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        val wrapper = WorldGuardWrapper.getInstance()

        if (ClaimUtils.checkAlreadyExist(wrapper, guild)) {
            throw ExpectationNotMet(Messages.CLAIM__ALREADY_EXISTS)
        }

        if (ClaimUtils.checkOverlap(wrapper, player, settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__OVERLAP)
        }

        ClaimUtils.createClaim(wrapper, guild, player, settingsManager)

        ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent { region ->
            ClaimUtils.addOwner(region, guild)
            ClaimUtils.addMembers(region, guild)
            ClaimUtils.setEnterMessage(wrapper, region, settingsManager, guild)
            ClaimUtils.setExitMessage(wrapper, region, settingsManager, guild)
        }

        currentCommandIssuer.sendInfo(Messages.CLAIM__SUCCESS,
                "{loc1}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointOne(player, settingsManager)),
                "{loc2}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointTwo(player, settingsManager)))
    }

    @Subcommand("admin unclaim")
    @Description("{@@descriptions.admin-unclaim}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun unclaim(player: Player, @Flags("other") @Values("@guilds") guild: Guild) {
        if (!ClaimUtils.isEnabled(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        val wrapper = WorldGuardWrapper.getInstance()

        if (!ClaimUtils.checkAlreadyExist(wrapper, guild)) {
            throw (ExpectationNotMet(Messages.UNCLAIM__NOT_FOUND))
        }

        ClaimUtils.removeClaim(wrapper, guild)
        currentCommandIssuer.sendInfo(Messages.UNCLAIM__SUCCESS)
    }
}
