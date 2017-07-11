package me.bramhaag.guilds.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by GlareMasters on 7/10/2017.
 */
public class ClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild Info")) {
            e.setCancelled(true);
        }
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild List")) {
            e.setCancelled(true);
        }
    }
}

