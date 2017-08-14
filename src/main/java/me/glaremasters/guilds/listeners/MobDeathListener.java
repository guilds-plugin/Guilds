package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;


/**
 * Created by GlareMasters on 8/7/2017.
 */
public class MobDeathListener implements Listener {

  @EventHandler
  public void onMobDeathEvent(EntityDeathEvent event) {
    if (event.getEntity() instanceof Monster) {
      Monster monster = (Monster) event.getEntity();
      Player killer = monster.getKiller();
      if (killer == null) {
        return;
      }

      Guild guild = Guild.getGuild(killer.getUniqueId());

      if (guild == null) {
        return;
      }

      event.setDroppedExp((int) (event.getDroppedExp() * guild.getExpMultiplier()));


    }
  }


}
