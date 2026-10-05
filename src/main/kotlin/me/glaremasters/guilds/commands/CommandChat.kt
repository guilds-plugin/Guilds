package me.glaremasters.guilds.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.listeners.ChatListener
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandChat : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds

    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("gc")
    @Description("{@@descriptions.chat}")
    @CommandPermission(Constants.BASE_PERM + "chat")
    @Syntax("%msg")
    fun guildChat(player: Player, @Conditions("perm:perm=CHAT") guild: Guild, @Optional msg: String?) {
        if (msg == null) {
            guilds.chatListener.handleToggle(player, ChatListener.ChatType.GUILD)
        } else {
            guildHandler.handleGuildChat(guild, player, msg)
        }
    }

    @Subcommand("ac")
    @Description("{@@descriptions.ally-chat}")
    @CommandPermission(Constants.BASE_PERM + "chat")
    @Syntax("%msg")
    fun chat(player: Player, @Conditions("perm:perm=ALLY_CHAT") guild: Guild, @Optional msg: String?) {
        if (msg == null) {
            guilds.chatListener.handleToggle(player, ChatListener.ChatType.ALLY)
        } else {
            guildHandler.handleAllyChat(guild, player, msg)
        }
    }
}
