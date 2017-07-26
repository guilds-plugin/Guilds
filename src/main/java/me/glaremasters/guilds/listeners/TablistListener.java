package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 7/20/2017.
 */
public class TablistListener implements Listener {


  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    Guild guild = Guild.getGuild(p.getUniqueId());
    if (guild == null) {
      return;
    } else {
      event.getPlayer().setPlayerListName(
          ChatColor.translateAlternateColorCodes('&',
              Main.getInstance().getConfig().getString("tablist")
                  .replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix()) + p
                  .getName()));
    }
  }

}
