package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/12/2017.
 */
public class CommandHome extends CommandBase {

    public CommandHome() {
        super("home", "Teleport to your guild home", "guilds.command.home", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()) == null)
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_HOME_SET);
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
            player.sendMessage(ChatColor.GREEN + "[Guilds] You've teleported to your guild home!");
        }
    }
}
