package me.glaremasters.guilds.commands.member

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandCheck : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("check")
    @Description("{@@descriptions.check}")
    @CommandPermission(Constants.BASE_PERM + "check")
    fun check(@Conditions("NoGuild") player: Player) {
        guildHandler.checkInvites(guilds.commandManager, player)
    }
}
