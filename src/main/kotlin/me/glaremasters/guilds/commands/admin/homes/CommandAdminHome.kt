package me.glaremasters.guilds.commands.admin.homes

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
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminHome : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin delhome")
    @Description("{@@descriptions.admin-delhome}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun delete(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        guild.delHome()
        currentCommandIssuer.sendInfo(Messages.ADMIN__DELHOME, "{guild}", guild.name)
    }

    @Subcommand("admin home")
    @Description("{@@descriptions.admin-home}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun home(player: Player, @Flags("other") @Values("@guilds") guild: Guild) {
        val home = guild.home ?: throw ExpectationNotMet(Messages.HOME__NO_HOME_SET)
        player.teleport(home.asLocation)
        currentCommandIssuer.sendInfo(Messages.ADMIN__HOME, "{guild}", guild.name)
    }

    @Subcommand("admin sethome")
    @Description("{@@descriptions.admin-sethome}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun set(player: Player, @Flags("other") @Values("@guilds") guild: Guild) {
        guild.setNewHome(player)
        currentCommandIssuer.sendInfo(Messages.ADMIN__SETHOME, "{guild}", guild.name)
    }
}
