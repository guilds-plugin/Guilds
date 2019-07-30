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

package me.glaremasters.guilds.guis;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings;
import me.glaremasters.guilds.configuration.sections.GuildListSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Glare
 * Date: 4/13/2019
 * Time: 3:51 PM
 */
public class ListGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;

    public ListGUI(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
    }

    public Gui getListGUI() {

        // Create the base GUI
        Gui gui = new Gui(guilds, 6, ACFBukkitUtil.color(settingsManager.getProperty(GuildListSettings.GUILD_LIST_NAME)));

        // Prevent players from being able to items into the GUIs
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        // Prepare a paginated pane
        OutlinePane paginatedPane = new OutlinePane(0, 0, 9, 5);

        // Add the items to the pane
        createListItems(paginatedPane);

        // Add the pane to the GUI
        gui.addPane(paginatedPane);

        // Return the GUI
        return gui;
    }

    /**
     * Create all the items for the GUI
     * @param pane the pane to add the items to
     */
    private void createListItems(OutlinePane pane) {
        List<Guild> guilds = guildHandler.getGuilds();
        String sortOrder = settingsManager.getProperty(GuildListSettings.GUILD_LIST_SORT);

        // Check if it's supposed to be sorted by tier
        if (sortOrder.equalsIgnoreCase("TIER")) {
            guilds.sort(Comparator.<Guild>comparingInt(g -> g.getTier().getLevel()).reversed());
        }

        // Check if it's supposed to be sorted by members
        if (sortOrder.equalsIgnoreCase("MEMBERS")) {
            guilds.sort(Comparator.<Guild>comparingInt(g -> g.getMembers().size()).reversed());
        }

        // Check if it's supposed to be sorted by bank balance
        if (sortOrder.equalsIgnoreCase("BALANCE")) {
            guilds.sort(Comparator.comparingDouble(Guild::getBalance).reversed());
        }

        // Loop through each guild to create the item
        guilds.forEach(g -> setListItem(pane, g));
    }

    /**
     * Set the item to the list
     * @param pane the pane to add to
     * @param guild the guild of the pane
     */
    private void setListItem(OutlinePane pane, Guild guild) {
        GuiItem listItem = new GuiItem(guild.getSkull(), event -> {
            guilds.getGuiHandler().getInfoMembersGUI().getInfoMembersGUI(guild).show(event.getWhoClicked());
            event.setCancelled(true);
        });
        ItemMeta meta = listItem.getItem().getItemMeta();
        meta.setDisplayName(ACFBukkitUtil.color(settingsManager.getProperty(GuildListSettings.GUILD_LIST_ITEM_NAME).replace("{player}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName()).replace("{guild}", guild.getName())));
        meta.setLore(updatedLore(guild, settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_LORE)));
        listItem.getItem().setItemMeta(meta);
        pane.addItem(listItem);
    }

    /**
     * Update lore with replacements
     * @param guild the guild being edited
     * @param lore the lore to change
     * @return updated lore
     */
    private List<String> updatedLore(Guild guild, List<String> lore) {
        boolean status = guild.isPrivate();
        String statusString;
        if (status) {
            statusString = settingsManager.getProperty(GuildInfoSettings.STATUS_PRIVATE);
        } else {
            statusString = settingsManager.getProperty(GuildInfoSettings.STATUS_PUBLIC);
        }
        List<String> updated = new ArrayList<>();
        lore.forEach(line -> updated.add(ACFBukkitUtil.color(line
                    .replace("{guild-name}", guild.getName())
                    .replace("{guild-prefix}", guild.getPrefix())
                    .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName())
                    .replace("{guild-status}", statusString)
                    .replace("{guild-tier}", String.valueOf(guild.getTier().getLevel()))
                    .replace("{guild-balance}", String.valueOf(guild.getBalance()))
                    .replace("{guild-member-count}", String.valueOf(guild.getSize())
                    .replace("{guild-tier-name}", guildHandler.getGuildTier(guild.getTier().getLevel()).getName())))));
        return updated;
    }

}
