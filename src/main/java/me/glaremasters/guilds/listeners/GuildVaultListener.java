package me.glaremasters.guilds.listeners;


import java.io.File;
import java.io.IOException;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Created by GlareMasters on 7/21/2017.
 */
public class GuildVaultListener implements Listener {

  @EventHandler
  public void onGuildVaultClose(InventoryCloseEvent event) {
    Player player = (Player) event.getPlayer();
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (event.getInventory().getTitle().equalsIgnoreCase(guild.getName() + "'s Guild Vault")) {
      Inventory inventory = event.getInventory();
      InventoryHolder holder = inventory.getHolder();

      File vaultf = new File(Main.getInstance().getDataFolder(),
          "data/vaults/" + guild.getName() + ".yml");
      if (!vaultf.exists()) {
        vaultf.getParentFile().mkdirs();
      }
      FileConfiguration vault = new YamlConfiguration();
      try {
        vault.load(vaultf);

      } catch (IOException | InvalidConfigurationException e) {
        e.printStackTrace();
      }
      for (int i = 0; i < inventory.getSize(); i++) {
        ItemStack item = new ItemStack(0, 0);
        if (inventory.getItem(i) != null) {
          vault.set("items.slot" + i, inventory.getItem(i));
          event.getPlayer().getInventory().setContents(inventory.getContents());
        }
      }
      try {
        vault.save(vaultf);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}


