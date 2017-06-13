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

    public static boolean guildchat = false;

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
            if (guildchat) {
                player.sendMessage("GUILD CHAT IS ALREADY ENABLED");
            } else {
                guildchat = false;
                player.sendMessage("GUILD CHAT HAS BEEN DISABLED");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            if (!guildchat) {
                player.sendMessage("GUILD CHAT IS ALREADY DISABLED");
            } else {
                guildchat = true;
                player.sendMessage("GUILD CHAT HAS BEEN ENABLED");
            }
            return;
        }

        String message = String.join(" ", args);
        guild.sendMessage(Message.COMMAND_CHAT_MESSAGE.replace("{role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName(), "{player}", player.getName(), "{message}", message));
    }
}
