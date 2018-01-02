package me.glaremasters.guilds.commands;

import java.util.Arrays;
import java.util.Objects;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildAddAllyEvent;
import me.glaremasters.guilds.api.events.GuildRemoveAllyEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAlly extends CommandBase {

    public CommandAlly() {
        super("ally", Guilds.getInstance().getConfig().getString("commands.description.ally"),
                "guilds.command.ally", false, null,
                "<add | remove> <guild>, or chat <guild>. or <list>", 1, -1);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if (args[0].equals("list")) {
            if (guild.getAllies().size() < 1) {
                Message.sendMessage(player, Message.COMMAND_ALLY_NONE);
                return;
            }
            Message.sendMessage(player, Message.COMMAND_ALLY_LIST);
            for (String list : guild.getAllies()) {
                player.sendMessage(list);
            }
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ARGS);
            return;
        }

        Guild targetGuild = Guild.getGuild(args[1]);
        if (targetGuild == null) {
            Message.sendMessage(player,
                    Message.COMMAND_ERROR_GUILD_NOT_FOUND.replace("{input}", args[1]));
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        if (args[0].equals("accept")) {

            if (!guild.getPendingAllies().contains(targetGuild.getName())) {
                Message.sendMessage(player, Message.COMMAND_ALLY_GUILD_NOT_PENDING);
                return;
            }

            guild.removePendingAlly(targetGuild);

            guild.addAlly(targetGuild);
            targetGuild.addAlly(guild);

            guild.getMembers().stream()
                    .map(member -> Bukkit.getPlayer(member.getUniqueId()))
                    .filter(Objects::nonNull).forEach(online -> Message.sendMessage(online, Message.COMMAND_ALLY_ACCEPTED_TARGET
                    .replace("{guild}", guild.getName())));

            targetGuild.getMembers().stream()
                    .map(member -> Bukkit.getPlayer(member.getUniqueId()))
                    .filter(Objects::nonNull).forEach(online -> Message.sendMessage(online, Message.COMMAND_ALLY_ACCEPTED_TARGET
                    .replace("{guild}", guild.getName())));
        } else if (args[0].equalsIgnoreCase("decline")) {
            if (!role.canAddAlly()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            if (!guild.getPendingAllies().contains(targetGuild.getName())) {
                Message.sendMessage(player, Message.COMMAND_ALLY_GUILD_NOT_PENDING);
                return;
            }

            guild.removePendingAlly(targetGuild);

            guild.getMembers().stream()
                    .map(member -> Bukkit.getPlayer(member.getUniqueId()))
                    .filter(Objects::nonNull).forEach(online -> Message.sendMessage(online, Message.COMMAND_ALLY_DECLINED
                    .replace("{guild}", targetGuild.getName())));

            targetGuild.getMembers().stream()
                    .map(member -> Bukkit.getPlayer(member.getUniqueId()))
                    .filter(Objects::nonNull).forEach(online -> Message.sendMessage(online, Message.COMMAND_ALLY_DECLINED
                    .replace("{guild}", targetGuild.getName())));

        }
        if (args[0].equalsIgnoreCase("add")) {
            if (!role.canAddAlly()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            if (guild.getAllies().contains(targetGuild.getName()) || guild.getName()
                    .equals(targetGuild.getName())) {
                Message.sendMessage(player,
                        Message.COMMAND_ALLY_ALREADY_ALLIES
                                .replace("{guild}", targetGuild.getName()));
                return;
            }

            GuildAddAllyEvent event = new GuildAddAllyEvent(player, guild, targetGuild);
            if (event.isCancelled()) {
                return;
            }

            Message.sendMessage(player, Message.COMMAND_ALLY_SEND);

            targetGuild.getMembers().stream()
                    .map(member -> Bukkit.getPlayer(member.getUniqueId()))
                    .filter(Objects::nonNull).forEach(online -> Message.sendMessage(online, Message.COMMAND_ALLY_SEND_TARGET
                    .replace("{guild}", guild.getName())));

            targetGuild.addPendingAlly(guild);
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (!role.canRemoveAlly()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            if (!guild.getAllies().contains(targetGuild.getName())) {
                Message.sendMessage(player,
                        Message.COMMAND_ALLY_NOT_ALLIES.replace("{guild}", targetGuild.getName()));
                return;
            }

            GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(player, guild, targetGuild);
            if (event.isCancelled()) {
                return;
            }

            guild.removeAlly(targetGuild);
            targetGuild.removeAlly(guild);

            guild.sendMessage(
                    Message.COMMAND_ALLY_REMOVED.replace("{guild}", targetGuild.getName()));
            targetGuild.sendMessage(
                    Message.COMMAND_ALLY_REMOVED_TARGET.replace("{guild}", guild.getName()));
        } else if (args[0].equalsIgnoreCase("chat")) {
            if (!role.useAllyChat()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            String message = String
                    .format("[%s] [%s] %s: %s", guild.getName(), role.getName(), player.getName(),
                            String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

            guild.sendMessage(message);
            targetGuild.sendMessage(message);
        }
    }
}
