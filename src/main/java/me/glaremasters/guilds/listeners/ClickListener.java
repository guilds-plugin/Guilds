package me.glaremasters.guilds.listeners;

import static me.glaremasters.guilds.util.ConfigUtil.getString;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;

/**
 * Created by GlareMasters on 7/10/2017.
 */
public class ClickListener implements Listener {

    public ClickListener(Guilds guilds) {
        this.guilds = guilds;
    }

    private Guilds guilds;



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        String title = event.getInventory().getTitle();
        if (title.equalsIgnoreCase(getString("gui-name.info"))) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            return;
        }
        if (title.equalsIgnoreCase(getString("guild-list.gui-name"))) {
            if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(getString("guild-list.previous-page-item-name"))) {
                    if (!(CommandList.playerPages.get(uuid) == 1)) {
                        int newPage = CommandList.playerPages.get(uuid) - 1;
                        CommandList.playerPages.remove(uuid);
                        CommandList.playerPages.put(uuid, newPage);
                        Inventory guildList = CommandList.getSkullsPage(newPage);
                        player.openInventory(guildList);
                    }
                }
                if (guilds.getGuildHandler().getGuilds().values().size() < 45) {
                    event.setCancelled(true);
                    event.setResult(Result.DENY);
                    return;
                }
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(getString("guild-list.next-page-item-name"))) {
                    int newPage = CommandList.playerPages.get(uuid) + 1;
                    CommandList.playerPages.remove(uuid);
                    CommandList.playerPages.put(uuid, newPage);
                    Inventory guildList = CommandList.getSkullsPage(newPage);
                    player.openInventory(guildList);
                }
            }
            event.setCancelled(true);
            event.setResult(Result.DENY);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        String title = event.getInventory().getTitle();
        if (title.equals(getString("guild-list.gui-name"))) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
        }
        if (title.equals(getString("gui-name.info"))) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
        }
    }
}
