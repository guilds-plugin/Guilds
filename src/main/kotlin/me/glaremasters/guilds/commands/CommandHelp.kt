package me.glaremasters.guilds.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.HelpCommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.Constants

@CommandAlias("%guilds")
internal class CommandHelp : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler

    @HelpCommand
    @CommandPermission(Constants.BASE_PERM + "help")
    @Description("{@@descriptions.help}")
    fun help(help: CommandHelp) {
        help.helpEntries.sortBy { it.command }
        help.showHelp()
    }
}
