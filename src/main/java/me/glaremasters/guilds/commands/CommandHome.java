package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 10:55 PM
 */
public class CommandHome extends CommandBase {

    public CommandHome() {
        super("home", "",
                "guilds.command.home", false, null, null, 0,
                0);
    }

    public void execute(final Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        if (guild.getHome().equals("")) return;

        String[] data = guild.getHome().split(":");
        World w = Bukkit.getWorld(data[0]);
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);

        Location guildhome = new Location(w, x, y, z);
        guildhome.setYaw(Float.parseFloat(data[4]));
        guildhome.setPitch(Float.parseFloat(data[5]));
        player.teleport(guildhome);
    }

}
