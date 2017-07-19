package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

public class CommandPrefix extends CommandBase {


    public CommandPrefix() {
        super("prefix", "Change your guild's prefix", "guilds.command.prefix", false, null,
            "<new prefix>", 1, 1);
    }

    @Override public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangePrefix()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        if (!args[0].matches(Main.getInstance().getConfig().getString("prefix.regex"))) {
            Message.sendMessage(player, Message.COMMAND_PREFIX_REQUIREMENTS);
            return;
        }

        Message.sendMessage(player, Message.COMMAND_PREFIX_SUCCESSFUL);
        guild.updatePrefix(args[0]);
    }
}
