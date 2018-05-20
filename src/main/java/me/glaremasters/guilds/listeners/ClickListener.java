package me.glaremasters.guilds.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.CommandList;

import static me.glaremasters.guilds.util.ConfigUtil.getString;

/**
 * Created by GlareMasters on 7/10/2017.
 */
public class ClickListener implements Listener {

    private Guilds guilds;

    public ClickListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getTitle().equalsIgnoreCase(getString("gui-name.info"))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }

        Inventory inventory = event.getInventory();
        ItemStack itemStack = event.getCurrentItem();
        if (!inventory.getTitle().equalsIgnoreCase(getString("gui-name.list.name")) || event.getAction() == InventoryAction.PICKUP_ALL) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemStack.hasItemMeta() && itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            if (displayName.equals(ChatColor.GOLD + getString("gui-name.list.previous-page"))
                    && CommandList.playerPages.get(player.getUniqueId()) == 1) {
                int newPage = CommandList.playerPages.get(player.getUniqueId()) - 1;

                CommandList.playerPages.replace(player.getUniqueId(), newPage);
                player.openInventory(CommandList.getSkullsPage(newPage));
            } else if (displayName.equals(ChatColor.GOLD + getString("gui-name.list.next-page"))) {
                int newPage = CommandList.playerPages.get(player.getUniqueId()) + 1;

                CommandList.playerPages.replace(player.getUniqueId(), newPage);
                player.openInventory(CommandList.getSkullsPage(newPage));
            }

            if (guilds.getGuildHandler().getGuilds().values().size() < 45) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                return;
            }

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onClick2(InventoryInteractEvent event) {
        String title = event.getInventory().getTitle();
        if (title.equalsIgnoreCase(getString("gui-name.info")) || title.equalsIgnoreCase(getString("gui-name.list.name"))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

}


