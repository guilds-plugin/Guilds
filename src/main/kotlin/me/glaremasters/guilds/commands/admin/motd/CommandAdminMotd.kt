package me.glaremasters.guilds.commands.admin.motd

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
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.StringUtils

@CommandAlias("%guilds")
internal class CommandAdminMotd : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin motd")
    @Description("{@@descriptions.admin-motd}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun get(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        val motd = guild.motd ?: throw ExpectationNotMet(Messages.MOTD__NOT_SET)
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD, "{guild}", guild.name, "{motd}", motd)
    }

    @Subcommand("admin motd remove")
    @Description("{@@descriptions.admin-motd-remove}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun remove(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        guild.motd = null
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD_REMOVE, "{guild}", guild.name)
    }

    @Subcommand("admin motd set")
    @Description("{@@descriptions.admin-motd-set}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %motd")
    fun set(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, motd: String) {
        guild.motd = StringUtils.color(motd)
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD_SUCCESS, "{guild}", guild.name, "{motd}", guild.motd)
    }
}
