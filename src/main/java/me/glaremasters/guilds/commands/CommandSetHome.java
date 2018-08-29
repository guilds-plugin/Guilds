package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 10:51 PM
 */
public class CommandSetHome extends CommandBase {

    private Guilds guilds;

    public CommandSetHome(Guilds guilds) {
        super("sethome", "",
                "guilds.command.sethome", false, null, null, 0,
                0);
        this.guilds = guilds;
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        String world = player.getWorld().getName();
        double xloc = player.getLocation().getX();
        double yloc = player.getLocation().getY();
        double zloc = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        guild.updateHome(world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":" + pitch);
    }


}
