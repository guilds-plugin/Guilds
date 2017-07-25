package me.glaremasters.guilds.listeners;


import java.util.stream.Stream;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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


  public enum GuildBuff {

    HASTE(PotionEffectType.FAST_DIGGING, Material.FEATHER, "haste"),
    SPEED(PotionEffectType.SPEED, Material.SUGAR, "speed"),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, Material.BLAZE_POWDER, "fire-resistance"),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION, Material.REDSTONE_TORCH_ON, "night-vision"),
    INVISIBILITY(PotionEffectType.INVISIBILITY, Material.EYE_OF_ENDER, "invisibility");


    public final PotionEffectType potion;
    public final Material itemType;
    public final int time;
    public final double cost;
    public final String name;

    GuildBuff(PotionEffectType potion, Material itemType, String configValueName) {
      this.time = Main.getInstance().getConfig().getInt("buff.time." + configValueName) * 20;
      this.cost = Main.getInstance().getConfig().getDouble("buff.price." + configValueName);
      this.itemType = itemType;
      this.potion = potion;
      this.name = Main.getInstance().getConfig().getString("buff.name." + configValueName);
    }

    public static GuildBuff get(Material itemType) {

      return Stream.of(values()).filter(it -> it.itemType == itemType).findAny().orElse(null);
    }

  }

  @EventHandler
  public void onHasteBuy(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (event.getInventory().getTitle().equals("Guild Buffs")) {
      GuildBuff buff = GuildBuff.get(event.getCurrentItem().getType());
      if (buff != null) {
        if (Main.vault && buff.cost != -1) {
          if (Main.getInstance().getEconomy().getBalance(player) < buff.cost) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
            return;
          }
          if (player.hasPotionEffect(buff.potion)) {
            return;
          }
          EconomyResponse response =
              Main.getInstance().getEconomy().withdrawPlayer(player, buff.cost);
          if (!response.transactionSuccess()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
            return;
          }

          guild.getMembers().stream()
              .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
              .filter(OfflinePlayer::isOnline)
              .forEach(member -> {

                ((Player) member).addPotionEffect(
                    new PotionEffect(buff.potion, buff.time, 2));

              });
          Bukkit.dispatchCommand(player, "guild chat " + "I just activated "
              + buff.name + " for " + buff.time/20 + " seconds!");
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
