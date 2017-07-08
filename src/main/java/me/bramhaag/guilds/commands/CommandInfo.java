package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class CommandInfo extends CommandBase {

    public CommandInfo() {
        super("info", "View your guild's info", "guilds.command.info", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        Message.sendMessage(player, Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_NAME.replace("{guild}", guild.getName(), "{prefix}", guild.getPrefix()));
        Message.sendMessage(player, Message.COMMAND_INFO_MASTER.replace("{master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_MEMBER_COUNT.replace("{members}", String.valueOf(guild.getMembers().size()), "{members-online}", String.valueOf(guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId())).filter(OfflinePlayer::isOnline).count())));
        Message.sendMessage(player, Message.COMMAND_INFO_RANK.replace("{rank}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_PLAYERS.replace("{players}", guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", "))));
    }
}
