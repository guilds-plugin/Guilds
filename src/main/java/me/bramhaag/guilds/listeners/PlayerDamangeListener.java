package me.bramhaag.guilds.listeners;

import me.bramhaag.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static me.bramhaag.guilds.commands.CommandHome.taskID;

public class PlayerDamangeListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        if (Guild.areAllies(player.getUniqueId(), damager.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage2(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) && !(e.getDamager() instanceof Player)) {
            Bukkit.getServer().getScheduler().cancelTask(taskID);
            return;
        }

    }
}
