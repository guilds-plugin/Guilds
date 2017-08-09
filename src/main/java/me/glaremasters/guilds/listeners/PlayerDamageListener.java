package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class PlayerDamageListener implements Listener {

  @EventHandler
  public void onPlayerDamage(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
      return;
    }

    Player player = (Player) e.getEntity();
    Player damager = (Player) e.getDamager();

    if (Guild.areAllies(player.getUniqueId(), damager.getUniqueId())) {
      e.setCancelled(false);
    }
  }
}
