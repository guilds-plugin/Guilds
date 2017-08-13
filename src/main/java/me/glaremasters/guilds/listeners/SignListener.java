package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by GlareMasters on 7/20/2017.
 */
public class SignListener implements Listener {

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    Player p = event.getPlayer();
    Guild guild = Guild.getGuild(p.getUniqueId());
    if (guild == null) {
      return;
    }
    if (event.getLine(0).endsWith("[Guild]")) {
      event.setLine(0, ChatColor.translateAlternateColorCodes('&',
          event.getLine(0).substring(0, event.getLine(0).length() - "[Guild]".length())
              + "Join Guild"));
      event.setLine(1, guild.getName());
      event.setLine(2, "Members");
      event.setLine(3, String.valueOf(
          guild.getMembers().size() + " / " + Main.getInstance().getConfig()
              .getInt("members.max-members")));
    }
  }

  @EventHandler
  public void onPlayerClickSign(PlayerInteractEvent event) {
    Player p = event.getPlayer();
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (event.getClickedBlock().getType() == Material.SIGN
          || event.getClickedBlock().getType() == Material.SIGN_POST
          || event.getClickedBlock().getType() == Material.WALL_SIGN) {
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Join Guild")) {
          p.chat("/guild join " + sign.getLine(1));
          sign.update();
        }
      }
    }
  }
}
