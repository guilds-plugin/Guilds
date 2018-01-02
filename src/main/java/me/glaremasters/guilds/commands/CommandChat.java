package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.listeners.GuildChatListener.GUILD_CHAT_PLAYERS;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

public class CommandChat extends CommandBase {

    public CommandChat() {
        super("chat", Guilds.getInstance().getConfig().getString("commands.description.chat"),
                "guilds.command.chat", false,
                new String[]{"gc"}, "<message>", 0, 100);
    }

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
            if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {
                GUILD_CHAT_PLAYERS.remove(player.getUniqueId());
                Message.sendMessage(player, Message.COMMAND_CHAT_DISABLED);
            } else {
                GUILD_CHAT_PLAYERS.add(player.getUniqueId());
                Message.sendMessage(player, Message.COMMAND_CHAT_ENABLED);
            }
        } else {
            String message = String.join(" ", args);
            guild.sendMessage(Message.COMMAND_CHAT_MESSAGE.replace("{role}",
                    GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName(),
                    "{player}", player.getName(), "{message}", message));
        }

    }
}
