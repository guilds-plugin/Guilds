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
import me.glaremasters.guilds.utils.ClaimUtils
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
        if (!ClaimUtils.isEnabled(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (ClaimUtils.isInDisabledWorld(player, settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (settingsManager.getProperty(ClaimSettings.FORCE_CLAIM_SIGNS)) {
            throw ExpectationNotMet(Messages.CLAIM__SIGN_FORCED)
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

    @Subcommand("unclaim")
    @Description("{@@descriptions.unclaim}")
    @CommandPermission(Constants.BASE_PERM + "unclaim")
    @Syntax("")
    fun unclaim(player: Player, @Conditions("perm:perm=UNCLAIM_LAND") guild: Guild) {
        if (!ClaimUtils.isEnabled(settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (ClaimUtils.isInDisabledWorld(player, settingsManager)) {
            throw ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED)
        }

        if (settingsManager.getProperty(ClaimSettings.FORCE_CLAIM_SIGNS)) {
            throw ExpectationNotMet(Messages.CLAIM__SIGN_FORCED)
        }

        val wrapper = WorldGuardWrapper.getInstance()

        if (!ClaimUtils.checkAlreadyExist(wrapper, guild)) {
            throw ExpectationNotMet(Messages.UNCLAIM__NOT_FOUND)
        }

        ClaimUtils.removeClaim(wrapper, guild)
        currentCommandIssuer.sendInfo(Messages.UNCLAIM__SUCCESS)
    }
}
