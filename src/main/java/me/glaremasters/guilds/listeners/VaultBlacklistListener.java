package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 5/7/2019
 * Time: 4:50 PM
 */
@AllArgsConstructor
public class VaultBlacklistListener implements Listener {

    private Guilds guilds;
    private GuildHandler guildHandler;
    private SettingsManager settingsManager;

    /**
     * Helps determine if a player has a Guild vault open
     * @param event the close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            guildHandler.getOpenedVault().remove(player);
        }
    }

    /**
     * Check if their item is on the vault blacklist
     * @param event the click event
     */
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {

        // get the player who is clicking
        Player player = (Player) event.getWhoClicked();

        // check if they are in the list of open vaults
        if (!guildHandler.getOpenedVault().contains(player))
            return;

        // get the item clicked
        ItemStack item = event.getCurrentItem();

        // check if null
        if (item == null)
            return;

        // set cancelled if it contains material name
        event.setCancelled(settingsManager.getProperty(GuildVaultSettings.BLACKLIST_MATERIALS).stream().anyMatch(m ->
                m.equalsIgnoreCase(item.getType().name())));

        // check if event is cancelled, if not, check name
        if (event.isCancelled()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.VAULTS__BLACKLISTED);
            return;
        }

        // Make sure item has item meta
        if (!item.hasItemMeta())
            return;

        // set cancelled if contains name
        event.setCancelled(settingsManager.getProperty(GuildVaultSettings.BLACKLIST_NAMES).stream().anyMatch(m ->
                m.equalsIgnoreCase(ACFBukkitUtil.removeColors(item.getItemMeta().getDisplayName()))));

        // check if event is cancelled
        if (event.isCancelled()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.VAULTS__BLACKLISTED);
            return;
        }

        // check if item has lore
        if (!item.getItemMeta().hasLore())
            return;

        // set cancelled if contains lore
        List<String> lore = item.getItemMeta().getLore().stream().map(String::toLowerCase).map(ACFBukkitUtil::removeColors).collect(Collectors.toList());

        // loop through string list
        for (String check : settingsManager.getProperty(GuildVaultSettings.BLACKLIST_LORES)) {
            // check if the lore contains it
            if (lore.contains(check.toLowerCase())) {
                // cancel the event
                event.setCancelled(true);
                break;
            }
        }

        // check if event is cancelled, if not, check name
        if (event.isCancelled()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.VAULTS__BLACKLISTED);
        }
    }
}
