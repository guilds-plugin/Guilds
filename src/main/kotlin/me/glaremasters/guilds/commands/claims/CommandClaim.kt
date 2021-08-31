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

package me.glaremasters.guilds.commands.claims

import ch.jalu.configme.SettingsManager
import co.aikar.commands.ACFBukkitUtil
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.ClaimSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.claim.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandClaim : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager

    @Subcommand("claim")
    @Description("{@@descriptions.claim}")
    @CommandPermission(Constants.BASE_PERM + "claim")
    @Syntax("")
    fun claim(player: Player, @Conditions("perm:perm=CLAIM_LAND") guild: Guild) {
        if (!ClaimUtils.isEnable(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (ClaimUtils.isInDisabledWorld(player, settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (settingsManager.getProperty(ClaimSettings.FORCE_CLAIM_SIGNS)) {
            throw ExpectationNotMet(Messages.CLAIM__SIGN_FORCED)
        }

        val wrapper = WorldGuardWrapper.getInstance()

        if (ClaimUtils.checkMaxAlreadyExist(wrapper, guild)) {
            throw ExpectationNotMet(Messages.CLAIM__ALREADY_EXISTS)
        }

        if (ClaimUtils.checkOverlap(wrapper, player)) {
            throw ExpectationNotMet(Messages.CLAIM__OVERLAP)
        }

        val claim = ClaimUtils.createClaim(wrapper, guild, player)
        guild.addGuildClaim(claim)

        ClaimUtils.addOwner(claim, guild)
        ClaimUtils.addMembers(claim, guild)
        ClaimUtils.setEnterMessage(wrapper, claim, settingsManager, guild)
        ClaimUtils.setExitMessage(wrapper, claim, settingsManager, guild)

        currentCommandIssuer.sendInfo(Messages.CLAIM__SUCCESS,
                "{loc1}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointOne(player)),
                "{loc2}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointTwo(player)))
    }

    @Subcommand("unclaim")
    @Description("{@@descriptions.unclaim}")
    @CommandPermission(Constants.BASE_PERM + "unclaim")
    @Syntax("")
    fun unclaim(player: Player, @Conditions("perm:perm=UNCLAIM_LAND") guild: Guild) {
        if (!ClaimUtils.isEnable(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (ClaimUtils.isInDisabledWorld(player, settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (settingsManager.getProperty(ClaimSettings.FORCE_CLAIM_SIGNS)) {
            throw ExpectationNotMet(Messages.CLAIM__SIGN_FORCED)
        }

        val wrapper = WorldGuardWrapper.getInstance()

        if (!ClaimUtils.checkIfHaveClaims(wrapper, guild)) {
            throw ExpectationNotMet(Messages.UNCLAIM__NOT_FOUND)
        }

        if (ClaimUtils.checkOverlap(wrapper, player)) {
            val claim = ClaimUtils.getStandingOnClaim(wrapper, player, guild)
            if (claim != null) {
                ClaimUtils.removeClaim(wrapper, claim, guild)
                guild.removeGuildClaim(claim)
            }
        }
        currentCommandIssuer.sendInfo(Messages.UNCLAIM__SUCCESS)
    }
}
