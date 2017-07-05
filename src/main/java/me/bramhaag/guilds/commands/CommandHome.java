package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by GlareMasters on 6/12/2017.
 */
public class CommandHome extends CommandBase {
    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    public CommandHome() {
        super("home", "Teleport to your guild home", "guilds.command.home", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        int cooldownTime = Main.getInstance().getConfig().getInt("home.cool-down");

        if (Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()) == null)
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_HOME_SET);

        if (cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                // Still cooling down
                Message.sendMessage(player, Message.COMMAND_ERROR_HOME_COOLDOWN.replace("{time}", String.valueOf(secondsLeft)));
                return;
            }
        } else {
            String[] data = Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()).split(":");
            World w = Bukkit.getWorld(data[0]);
            double x = Double.parseDouble(data[1]);
            double y = Double.parseDouble(data[2]);
            double z = Double.parseDouble(data[3]);

            Location guildhome = new Location(w, x, y, z);
            guildhome.setYaw(Float.parseFloat(data[4]));
            guildhome.setPitch(Float.parseFloat(data[5]));

            player.teleport(guildhome);
            Message.sendMessage(player, Message.COMMAND_HOME_TELEPORTED);
            cooldowns.put(player.getName(), System.currentTimeMillis());
        }

    }
}
