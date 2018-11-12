package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

import static me.glaremasters.guilds.commands.CommandGuilds.getSkullsPage;
import static me.glaremasters.guilds.commands.CommandGuilds.playerPages;

/**
 * Created by GlareMasters
 * Date: 11/8/2018
 * Time: 11:51 PM
 */
public class InventoryListener implements Listener {

    private Guilds guilds;

    public InventoryListener(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * This event just checks if a player is clicking on the next or back page and making sure you can't dupe from the GUIs
     * @param event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        String title = event.getInventory().getTitle();
        if (title.equalsIgnoreCase(guilds.getConfig().getString("gui-name.info"))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            return;
        }
        if (title.equalsIgnoreCase(guilds.getConfig().getString("guild-list.gui-name"))) {
            if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(guilds.getConfig().getString("guild-list.previous-page-item-name"))) {
                    if (!(playerPages.get(uuid) == 1)) {
                        int newPage = playerPages.get(uuid) - 1;
                        playerPages.remove(uuid);
                        playerPages.put(uuid, newPage);
                        Inventory guildList = getSkullsPage(newPage);
                        player.openInventory(guildList);
                    }
                }
                if (guilds.getGuildHandler().getGuilds().values().size() < 45) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(guilds.getConfig().getString("guild-list.next-page-item-name"))) {
                    int newPage = playerPages.get(uuid) + 1;
                    playerPages.remove(uuid);
                    playerPages.put(uuid, newPage);
                    Inventory guildList = getSkullsPage(newPage);
                    player.openInventory(guildList);
                }
            }
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * This event is a double check to make sure you can't dupe from the GUIs
     * @param event
     */
    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        String title = event.getInventory().getTitle();
        if (title.equals(guilds.getConfig().getString("guild-list.gui-name"))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
        if (title.equals(guilds.getConfig().getString("gui-name.info"))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }
}
