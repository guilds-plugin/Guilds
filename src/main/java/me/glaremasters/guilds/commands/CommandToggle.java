package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/12/2017.
 */
public class CommandToggle extends CommandBase {
    public CommandToggle() {
        super("toggle", "Toggle your guild Public / Private", "guilds.command.toggle", false, null, "<public | private>", 1, 1);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canToggleGuild()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }
        guild.updateStatus(args[0]);

    }
}
