package me.glaremasters.guilds.commands.management

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandStatus : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("status")
    @Description("{@@descriptions.status}")
    @CommandPermission(Constants.BASE_PERM + "status")
    @Syntax("")
    fun status(player: Player, @Conditions("perm:perm=TOGGLE_GUILD") guild: Guild) {
        guild.toggleStatus()
        currentCommandIssuer.sendInfo(Messages.STATUS__SUCCESSFUL, "{status}", guild.status.name)
    }
}
