package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 10:08 AM
 */
public class CommandDecline extends CommandBase {

    private Guilds guilds;

    public CommandDecline(Guilds guilds) {
        super("decline", "", "guilds.command.decline", false, null, null, 1 ,1);
        this.guilds = guilds;
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(args[0]);
        if (Guild.getGuild(player.getUniqueId()) != null) return;
        if (guild == null) return;
        if (!guild.getInvitedMembers().contains(player.getUniqueId())) return;
        guild.removeInvitedPlayer(player.getUniqueId());
    }

}
