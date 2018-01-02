package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/26/2017.
 */
public class CommandDecline extends CommandBase {

    public CommandDecline() {
        super("decline", Guilds.getInstance().getConfig().getString("commands.description.decline"),
                "guilds.command.decline", false, null, null, 1, 1);
    }

    public void execute(Player player, String[] args) {

        Guild guild = Guild.getGuild(args[0]);

        guild.removeInvitedPlayer(player.getUniqueId());
        Message.sendMessage(player, Message.COMMAND_DECLINE_SUCCESS);
    }

}
