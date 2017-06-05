package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildMember;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRole extends CommandBase {

    public CommandRole() {
        super("role", "View all players with the specified role", "guilds.command.role", false, new String[]{"rank"},
                "<" + String.join(" | ", Stream.of(Main.getInstance().getGuildHandler().getRoles().toArray(new GuildRole[0])).map(GuildRole::getName).collect(Collectors.toList())) + ">",
                1, 1);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = Main.getInstance().getGuildHandler().getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if (role == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_INVALID_ROLE.replace("{input}", args[0]));
            return;
        }

        List<GuildMember> members = guild.getMembers().stream().filter(member -> member.getRole() == role.getLevel()).collect(Collectors.toList());
        members.forEach(member -> Message.sendMessage(player, Message.COMMAND_ROLE_PLAYERS.replace("{player}", Bukkit.getPlayer(member.getUniqueId()).getName(), "{role}", role.getName())));
    }
}
