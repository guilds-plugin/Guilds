/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import me.glaremasters.guilds.guild.Guild;
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
public class VaultBlacklistListener implements Listener {

    private final Guilds guilds;
    private final GuildHandler guildHandler;
    private final SettingsManager settingsManager;

    public VaultBlacklistListener(Guilds guilds, GuildHandler guildHandler, SettingsManager settingsManager) {
        this.guilds = guilds;
        this.guildHandler = guildHandler;
        this.settingsManager = settingsManager;
    }

    /**
     * Helps determine if a player has a Guild vault open
     *
     * @param event the close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            guildHandler.getOpened().remove(player);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Guild guild = guildHandler.getGuild(player);
        if (guild == null) {
            return;
        }
        if (!guildHandler.getOpened().contains(player)) {
            return;
        }
        if (event.getClickedInventory() != null) {
            return;
        }
        guilds.getGuiHandler().getVaults().get(guild, player).open(event.getWhoClicked());
        guildHandler.getOpened().remove(player);
    }

    /**
     * Check if their item is on the vault blacklist
     *
     * @param event the click event
     */
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {

        // get the player who is clicking
        Player player = (Player) event.getWhoClicked();

        // check if they are in the list of open vaults
        if (!guildHandler.getOpened().contains(player))
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

        // Check if it has a display name
        if (item.getItemMeta().hasDisplayName()) {
            // set cancelled if contains name
            event.setCancelled(settingsManager.getProperty(GuildVaultSettings.BLACKLIST_NAMES).stream().anyMatch(m ->
                    m.equalsIgnoreCase(ACFBukkitUtil.removeColors(item.getItemMeta().getDisplayName()))));
        }

        // check if event is cancelled
        if (event.isCancelled()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.VAULTS__BLACKLISTED);
            return;
        }

        // check if item has lore
        if (!item.getItemMeta().hasLore())
            return;

        // set cancelled if contains lore
        List<String> lore = item.getItemMeta().getLore().stream()
                .map(ACFBukkitUtil::removeColors).collect(Collectors.toList());

        // loop through string list
        for (String check : settingsManager.getProperty(GuildVaultSettings.BLACKLIST_LORES)) {
            // check if the lore contains it
            if (!check.equalsIgnoreCase("")) {
                if (lore.stream().anyMatch(l -> l.contains(check))) {
                    // cancel the event
                    event.setCancelled(true);
                    break;
                }
            }
        }

        // check if event is cancelled, if not, check name
        if (event.isCancelled()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.VAULTS__BLACKLISTED);
        }
    }
}
