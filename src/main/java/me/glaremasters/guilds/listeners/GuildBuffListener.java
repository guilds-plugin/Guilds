package me.glaremasters.guilds.listeners;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by GlareMasters on 7/24/2017.
 */
public class GuildBuffListener implements Listener {

  double hasteCost = Main.getInstance().getConfig().getDouble("buff.price.haste");
  double speedCost = Main.getInstance().getConfig().getDouble("buff.price.speed");
  double fireResistanceCost = Main.getInstance().getConfig().getDouble("buff.price.fire-resistance");
  double nightVisionCost = Main.getInstance().getConfig().getDouble("buff.price.night-vision");
  double invisibilityCost = Main.getInstance().getConfig().getDouble("buff.price.invisibility");
  int hasteLength = Main.getInstance().getConfig().getInt("buff.time.haste") * 20;
  @EventHandler
  public void onHasteBuy(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (event.getInventory().getTitle().equals("Guild Buffs")) {
      if (event.getCurrentItem().getType() == Material.FEATHER) {
        if (Main.vault && hasteCost != -1) {
          if (Main.getInstance().getEconomy().getBalance(player) < hasteCost) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
            return;
          }
          if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            return;
          }
          EconomyResponse response =
              Main.getInstance().getEconomy().withdrawPlayer(player, hasteCost);
          if (!response.transactionSuccess()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
            return;
          }
player.addPotionEffect(
              new PotionEffect(PotionEffectType.FAST_DIGGING, hasteLength, 3));
          Bukkit.dispatchCommand(player,"guild chat " + "I just activated "
              + Main.getInstance().getConfig().getString("buff.name.haste") + " for " + Main.getInstance().getConfig().getInt("buff.time.haste") + " seconds!");
        }
      }
    }
  }

  @EventHandler
  public void onClick(InventoryInteractEvent event) {
    if (event.getInventory().getTitle().equals("Guild Buffs")) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onClick2(InventoryClickEvent event) {
    if (event.getInventory().getTitle().equals("Guild Buffs")) {
      event.setCancelled(true);
    }
  }

}
