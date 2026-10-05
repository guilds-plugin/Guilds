package me.glaremasters.guilds.commands.motd

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandMotd : BaseCommand() {
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Dependency
    lateinit var settingsManager: SettingsManager

    @Subcommand("motd")
    @Description("{@@descriptions.motd}")
    @CommandPermission(Constants.BASE_PERM + "motd")
    @Syntax("")
    fun check(player: Player, guild: Guild) {
        val motd = guild.motd ?: throw ExpectationNotMet(Messages.MOTD__NOT_SET)
        currentCommandIssuer.sendInfo(Messages.MOTD__MOTD, "{motd}", motd)
    }

    @Subcommand("motd remove")
    @Description("{@@descriptions.motd-remove}")
    @CommandPermission(Constants.MOTD_PERM + "modify")
    @Syntax("")
    fun remove(player: Player, @Conditions("perm:perm=MODIFY_MOTD") guild: Guild) {
        guild.motd = null
        currentCommandIssuer.sendInfo(Messages.MOTD__REMOVE)
    }

    @Subcommand("motd set")
    @Description("{@@descriptions.motd-set}")
    @CommandPermission(Constants.MOTD_PERM + "modify")
    @Syntax("%motd")
    fun set(player: Player, @Conditions("perm:perm=MODIFY_MOTD") guild: Guild, motd: String) {
        guild.motd = StringUtils.color(motd)
        currentCommandIssuer.sendInfo(Messages.MOTD__SUCCESS, "{motd}", guild.motd)
    }
}
