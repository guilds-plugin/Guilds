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
import me.glaremasters.guilds.api.events.GuildRenameEvent
import me.glaremasters.guilds.configuration.sections.CostSettings
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuildInputValidator
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandRename : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager

    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission(Constants.BASE_PERM + "rename")
    @Syntax("%name")
    fun rename(player: Player, @Conditions("perm:perm=RENAME") guild: Guild, name: String) {
        // Exclude this guild so it can keep or return to a name it already has.
        if (GuildInputValidator.isNameTaken(name, guildHandler.guilds.values, guild.id)) {
            throw ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN)
        }

        if (!GuildInputValidator.isValidName(name, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__REQUIREMENTS)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        val renameCost = settingsManager.getProperty(CostSettings.RENAME)
        val charge = renameCost != 0.0

        if (charge && !EconomyUtils.hasEnough(guild.balance, renameCost)) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        val event = GuildRenameEvent(player, guild, name)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guild.balance = guild.balance - renameCost

        guild.name = StringUtils.color(name)

        if (ClaimUtils.isEnabled(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()
            if (ClaimUtils.checkAlreadyExist(wrapper, guild)) {
                ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent { region ->
                    ClaimUtils.setEnterMessage(wrapper, region, settingsManager, guild)
                    ClaimUtils.setExitMessage(wrapper, region, settingsManager, guild)
                }
            }
        }

        currentCommandIssuer.sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name)
    }
}
