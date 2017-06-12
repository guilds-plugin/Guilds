package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandSetHome extends CommandBase {

    public CommandSetHome() {
        super("sethome", "Set your guild's home!", "guilds.command.sethome", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {

        String world = player.getWorld().getName();
        double xloc = player.getLocation().getX();
        double yloc = player.getLocation().getY();
        double zloc = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        Main.getInstance().guildhomesconfig.set(Guild.getGuild(player.getUniqueId()).getName(), world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":" + pitch);
        Main.getInstance().saveGuildhomes();
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);

        /*
        * Code here for the /guild home
        */

        if (Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()) == null)
            player.sendMessage("NO HOME SET USE /SETHOME TO SET A HOME");
        else {
            String[] data = Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()).split(":");
            World w = Bukkit.getWorld(data[0]);
            double x = Double.parseDouble(data[1]);
            double y = Double.parseDouble(data[2]);
            double z = Double.parseDouble(data[3]);

            Location guildhome = new Location(w, x, y, z);
            guildhome.setYaw(Float.parseFloat(data[4]));
            guildhome.setPitch(Float.parseFloat(data[5]));

            player.teleport(guildhome);
            /*
             * Send a message that the player has been teleported to their home.
             */
        }
    }
}
