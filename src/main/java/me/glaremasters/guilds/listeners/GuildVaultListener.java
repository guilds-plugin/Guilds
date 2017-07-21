package me.glaremasters.guilds.listeners;

import static org.bukkit.Bukkit.getName;

import java.io.File;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Created by GlareMasters on 7/21/2017.
 */
public class GuildVaultListener implements Listener {

  @EventHandler
  public void onGuildVaultClose(InventoryCloseEvent event) {

    Inventory inventory =  event.getInventory();

    InventoryHolder holder = inventory.getHolder();

    if (holder != null && holder instanceof Guild) {
      File vaultf = new File(Main.getInstance().getDataFolder(),
          "data/vaults/" + getName() + ".yml");

      if (!vaultf.exists()) {
        vaultf.getParentFile().mkdirs();
      }
      FileConfiguration vault = new YamlConfiguration();
      for (int i = 0; i < inventory.getSize(); i++) {
        if (inventory.getItem(i) != null) {
          vault.set("slot" +i, inventory.getItem(i));
        }
      }
    }


  }

}
