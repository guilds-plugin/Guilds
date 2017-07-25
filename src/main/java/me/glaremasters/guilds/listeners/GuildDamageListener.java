package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by GlareMasters on 7/20/2017.
 */
public class GuildDamageListener implements Listener {

  @EventHandler
  public void onPlayerDamage2(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
      return;
    }
    Player player = (Player) e.getEntity();
    Player damager = (Player) e.getDamager();
    Guild guild = Guild.getGuild(player.getUniqueId());
    Guild guild2 = Guild.getGuild(damager.getUniqueId());

    if (guild == null || guild2 == null) {
      return;
    }

    if (guild.equals(guild2)) {
      e.setCancelled(true);
    }
  }

}
