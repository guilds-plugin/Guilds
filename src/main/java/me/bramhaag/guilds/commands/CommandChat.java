package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

public class CommandChat extends CommandBase {

    public CommandChat() {
        super("chat", "Send a message to your guild members", "guilds.command.chat", false, new String[]{"c"}, "<message>", 1, -1);
    }

    public static boolean guildchat;

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChat()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            if (!guildchat) {
                player.sendMessage("§aYou have enabled your guild chat!");
            } else {
                player.sendMessage("§4Your guild chat has already been enabled!");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            if (guildchat) {
                player.sendMessage("§cYou have now disabled your guild chat!");
            } else {
                player.sendMessage("§cYour guild chat is not enabled!");
            }
            return;
        }

        String message = String.join(" ", args);
        guild.sendMessage(Message.COMMAND_CHAT_MESSAGE.replace("{role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName(), "{player}", player.getName(), "{message}", message));
    }
}
