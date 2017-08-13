package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by GlareMasters on 8/13/2017.
 */
public class DamageMultiplierListener implements Listener {

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      Player player = (Player) event.getDamager();
      Guild guild = Guild.getGuild(player.getUniqueId());

      if (guild == null) {
        return;
      }
      event.setDamage((int) (event.getDamage() * guild.getDamageMultiplier()));
    }

  }

}
