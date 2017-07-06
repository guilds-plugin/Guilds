package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by GlareMasters on 6/12/2017.
 */
public class CommandHome
        extends CommandBase implements Listener {
    public HashMap<String, Long> cooldowns = new HashMap();
    public HashMap<UUID, BukkitTask> countdown = new HashMap();
    public static int taskID;
    public CommandHome() {
        super("home", "Teleport to your guild home", "guilds.command.home", false, null, null, 0, 0);
    }

    public void execute(final Player player, String[] args) {
        int cooldownTime = Main.getInstance().getConfig().getInt("home.cool-down");
        if (Main.getInstance().guildhomesconfig.getString(Guild.getGuild(player.getUniqueId()).getName()) == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_HOME_SET);
        }
        if (this.cooldowns.containsKey(player.getName())) {
            long secondsLeft = this.cooldowns.get(player.getName()).longValue() / 1000 + cooldownTime - System.currentTimeMillis() / 1000;
            if (secondsLeft > 0) {
                Message.sendMessage(player, Message.COMMAND_ERROR_HOME_COOLDOWN.replace(new String[]{"{time}", String.valueOf(secondsLeft)}));
                return;
            }
        }

        BukkitTask taskID = this.countdown.put(player.getUniqueId(), new BukkitRunnable() {
            int count = Main.getInstance().getConfig().getInt("home.teleport-delay");

            public void run() {

                if (count > 0) {
                    Message.sendMessage(player, Message.COMMAND_HOME_TELEPORTING.replace(new String[]{"{count}", String.valueOf(count)}));
                    count--;
                    if (Main.getInstance().getConfig().getBoolean("home.freeze-player")) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, cooldownTime, 100));
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
                    CommandHome.this.cooldowns.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
                    CommandHome.this.countdown.remove(player.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L));
        return;
    }

}

