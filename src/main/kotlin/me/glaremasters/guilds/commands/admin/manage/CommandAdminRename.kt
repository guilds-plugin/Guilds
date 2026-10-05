package me.glaremasters.guilds.commands.admin.manage

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.GuildInputValidator
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminRename : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin rename")
    @Description("{@@descriptions.admin-rename}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %new-name")
    fun rename(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, @Single name: String) {
        // The same validation the player-facing rename runs, minus the economy cost. Without it an
        // admin could set a duplicate visible name, and every --other command resolves the target
        // guild by that name, so the duplicate would make those commands non-deterministic.
        if (GuildInputValidator.isNameTaken(name, guildHandler.guilds.values, guild.id)) {
            throw ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN)
        }

        if (!GuildInputValidator.isValidName(name, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__REQUIREMENTS)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        guild.name = StringUtils.color(name)
        currentCommandIssuer.sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name)
    }
}
