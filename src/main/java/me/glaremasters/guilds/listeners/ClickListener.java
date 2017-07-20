package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.CommandList;
import me.glaremasters.guilds.guild.Guild;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;


/**
 * Created by GlareMasters on 7/10/2017.
 */
public class ClickListener implements Listener {

    @EventHandler public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild Info")) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild List")) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (e.getCurrentItem().getItemMeta().getDisplayName()
                    .equals(ChatColor.GOLD + "Previous page")) {

                    if (!(CommandList.playerPages.get(e.getWhoClicked().getUniqueId()) == 1)) {
                        int newPage =
                            CommandList.playerPages.get(e.getWhoClicked().getUniqueId()) - 1;

                        CommandList.playerPages.remove(e.getWhoClicked().getUniqueId());
                        CommandList.playerPages.put(e.getWhoClicked().getUniqueId(), newPage);
                        Inventory guildList = CommandList.getSkullsPage(newPage);
                        e.getWhoClicked().openInventory(guildList);
                    }
                }

                if (e.getCurrentItem().getItemMeta().getDisplayName()
                    .equals(ChatColor.GOLD + "Next page")) {

                    int newPage = CommandList.playerPages.get(e.getWhoClicked().getUniqueId()) + 1;

                    CommandList.playerPages.remove(e.getWhoClicked().getUniqueId());
                    CommandList.playerPages.put(e.getWhoClicked().getUniqueId(), newPage);
                    Inventory guildList = CommandList.getSkullsPage(newPage);
                    e.getWhoClicked().openInventory(guildList);
                }
            }
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }

    @EventHandler public void onClick2(InventoryInteractEvent e) {
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild Info")) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
        if (e.getInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Guild List")) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }
}


