package me.glaremasters.guilds.commands.admin

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminSpy : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler

    @Subcommand("admin spy")
    @Description("{@@descriptions.admin-spy}")
    @CommandPermission("guilds.chat.spy")
    fun spy(player: Player) {
        guildHandler.toggleSpy(guilds.commandManager, player)
    }
}
