package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandChat extends CommandBase {

    public CommandChat() {
        super("chat", "Send a message to your guild members", "guilds.command.chat", false, new String[]{"c"}, "<message>", 1, -1);
    }

    public static List<UUID> guildchat = new ArrayList<UUID>();

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

        if (args.length == 0) {
            if (guildchat.contains(player.getUniqueId())) {
                guildchat.remove(player.getUniqueId());
                player.sendMessage("§cYou have been removed from the guild chat.");
            } else {
                guildchat.add(player.getUniqueId());
                player.sendMessage("§aYou have been added to the guild chat.");
            }
            return;
        }

        String message = String.join(" ", args);
        guild.sendMessage(Message.COMMAND_CHAT_MESSAGE.replace("{role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName(), "{player}", player.getName(), "{message}", message));
    }
}
