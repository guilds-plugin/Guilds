package me.glaremasters.guilds.listeners;

import static me.glaremasters.guilds.util.ConfigUtil.getString;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        UUID uuid = player.getUniqueId();
        String title = e.getInventory().getTitle();
        if (title.equalsIgnoreCase(getString("gui-name.info"))) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
        if (title.equalsIgnoreCase(getString("gui-name.list.name"))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + guilds.getConfig()
                                .getString("gui-name.list.previous-page"))) {
                    if (!(CommandList.playerPages.get(uuid) == 1)) {
                        int newPage =
                                CommandList.playerPages.get(uuid) - 1;

                        CommandList.playerPages.remove(uuid);
                        CommandList.playerPages.put(uuid, newPage);
                        Inventory guildList = CommandList.getSkullsPage(newPage);
                        e.getWhoClicked().openInventory(guildList);
                    }
                }
                if (guilds.getGuildHandler().getGuilds().values().size() < 45) {
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                    return;
                }
                if (e.getCurrentItem().getItemMeta().getDisplayName()
                        .equals(ChatColor.GOLD + guilds.getConfig()
                                .getString("gui-name.list.next-page"))) {

                    int newPage = CommandList.playerPages.get(uuid) + 1;

                    CommandList.playerPages.remove(uuid);
                    CommandList.playerPages.put(uuid, newPage);
                    Inventory guildList = CommandList.getSkullsPage(newPage);
                    player.openInventory(guildList);
                }
            }

            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onClick2(InventoryInteractEvent e) {
        if (e.getInventory().getTitle()
                .equalsIgnoreCase(guilds.getConfig().getString("gui-name.info"))) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
        if (e.getInventory().getTitle()
                .equalsIgnoreCase(guilds.getConfig().getString("gui-name.list.name"))) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }
}
