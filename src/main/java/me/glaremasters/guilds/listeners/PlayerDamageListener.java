package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class PlayerDamageListener implements Listener {

  @EventHandler
  public void onDamageOfPlayer(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();
    Player damager = (Player) event.getDamager();
    Guild guild = Guild.getGuild(player.getUniqueId());
    Guild guild2 = Guild.getGuild(damager.getUniqueId());
    if (guild == null || guild2 == null) {
      return;
    }
    if (guild.equals(guild2)) {
      event.setCancelled(!Main.getInstance().getConfig().getBoolean("allow-guild-damage"));
    }
    if (Guild.areAllies(player.getUniqueId(), damager.getUniqueId())) {
      event.setCancelled(!Main.getInstance().getConfig().getBoolean("allow-ally-damage"));
    }
  }
}
