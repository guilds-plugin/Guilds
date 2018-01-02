package me.glaremasters.guilds.commands;

import java.util.stream.Collectors;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Created by GlareMasters on 7/7/2017.
 */
public class CommandInspect extends CommandBase {

    public CommandInspect() {
        super("inspect", Guilds.getInstance().getConfig().getString("commands.description.inspect"),
                "guilds.command.inspect", true, null,
                "<guild name>", 1, 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Guild guild = Guild.getGuild(args[0]);
        if (guild == null) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_NO_GUILD);
            return;
        } else {
            Message.sendMessage(sender,
                    Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
            Message.sendMessage(sender, Message.COMMAND_INFO_NAME
                    .replace("{guild}", guild.getName(), "{prefix}", guild.getPrefix()));
            Message.sendMessage(sender, Message.COMMAND_INFO_MASTER.replace("{master}",
                    Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
            Message.sendMessage(sender, Message.COMMAND_INFO_MEMBER_COUNT
                    .replace("{members}", String.valueOf(guild.getMembers().size()),
                            "{members-online}",
                            String.valueOf(guild.getMembers().stream()
                                    .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                                    .filter(OfflinePlayer::isOnline).count())));
            Message.sendMessage(sender, Message.COMMAND_INFO_PLAYERS.replace("{players}",
                    guild.getMembers().stream()
                            .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName())
                            .collect(Collectors.joining(", "))));
        }
    }
}
