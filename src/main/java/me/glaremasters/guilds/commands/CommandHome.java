package me.glaremasters.guilds.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Created by GlareMasters on 6/12/2017.
 */
public class CommandHome extends CommandBase implements Listener {

    private static Map<UUID, BukkitTask> countdown = new HashMap<>();
    private static Listener listener = null;
    public HashMap<String, Long> cooldowns = new HashMap();
    int count = Guilds.getInstance().getConfig().getInt("home.teleport-delay");
    int cooldownTime = Guilds.getInstance().getConfig().getInt("home.cool-down");

    public CommandHome() {
        super("home", Guilds.getInstance().getConfig().getString("commands.description.home"),
                "guilds.command.home", false, null, null, 0,
                0);
    }

    public void execute(final Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }


        int cooldownTime = Guilds.getInstance().getConfig().getInt("home.cool-down");
        if (Guilds.getInstance().guildHomesConfig
                .getString(Guild.getGuild(player.getUniqueId()).getName()) == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_HOME_SET);
            return;
        }
        if (this.cooldowns.containsKey(player.getName())) {
            long secondsLeft =
                    this.cooldowns.get(player.getName()).longValue() / 1000 + cooldownTime
                            - System.currentTimeMillis() / 1000;
            if (secondsLeft > 0) {
                Message.sendMessage(player, Message.COMMAND_ERROR_HOME_COOLDOWN
                        .replace("{time}", String.valueOf(secondsLeft)));
                return;
            }
        }
        start(player);
        if (listener == null) {
            listener = new Listener() {

                @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
                public void onDamage(EntityDamageByEntityEvent e) {
                    if (!(e.getEntity() instanceof Player)) {
                        return;
                    }

                    Player damaged = (Player) e.getEntity();

                    if (!countdown.containsKey(damaged.getUniqueId())) {
                        return;
                    }

                    countdown.get(damaged.getUniqueId()).cancel();
                    Message.sendMessage(player, Message.COMMAND_HOME_DAMAGE_TAKEN);
                    player.removePotionEffect(PotionEffectType.SLOW);
                    countdown.remove(damaged.getUniqueId());
                }

            };
            Bukkit.getPluginManager().registerEvents(listener, /* plugin */Guilds.getInstance());
        }
    }

    public void start(Player player) {
        countdown.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                if (count > 0) {
                    Message.sendMessage(player, Message.COMMAND_HOME_TELEPORTING
                            .replace("{count}", String.valueOf(count)));
                    count--;
                    if (Guilds.getInstance().getConfig().getBoolean("home.freeze-player")) {
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.SLOW, cooldownTime, 100));
                    }

                } else {

                    Location guildhome = (Location) Guilds.getInstance().guildHomesConfig.get(Guild.getGuild(player.getUniqueId()).getName());

                    player.teleport(guildhome);
                    Message.sendMessage(player, Message.COMMAND_HOME_TELEPORTED);
                    CommandHome.this.cooldowns
                            .put(player.getName(), Long.valueOf(System.currentTimeMillis()));
                    countdown.remove(player.getUniqueId());
                    this.cancel();
                }
            }

        }.runTaskTimer(Guilds.getInstance(), 0L, 20L));
    }

}