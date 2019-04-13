/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Glare
 * Date: 4/13/2019
 * Time: 3:51 PM
 */
@AllArgsConstructor
public class ListGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;

    public Gui getListGUI() {

        // Get the page count
        int pages = (int) Math.ceil((double) guildHandler.getGuildsSize() / 45);
        int guildAmount = guildHandler.getGuildsSize();


        // Create the base GUI
        Gui gui = new Gui(guilds, 6, ACFBukkitUtil.color(settingsManager.getProperty(GuiSettings.GUILD_LIST_NAME)));

        // Prepare a paginated pane
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 5);

        // Loop through the pages
        for (int page = 0; page < pages; page++) {
            // Make an outline pane for each one
            OutlinePane outlinePane = new OutlinePane(0, 0, 9 ,5);

            // Loop through all the items in the pane
            for (int i = 0; i < guildAmount; i++) {
                // Create the item here
                Guild guild = guildHandler.getGuilds().get(i);
                if (guild != null) {
                    setListItem(outlinePane, guild);
                    paginatedPane.addPane(page, outlinePane);
                }

                // Add the created item to the pane

            }

            // Add the outline to the paginated pane
            gui.addPane(paginatedPane);
        }

        // Return the GUI
        return gui;
    }

    private void setListItem(OutlinePane pane, Guild guild) {
        GuiItem listItem = new GuiItem(guild.getSkull(), event -> event.setCancelled(true));
        ItemMeta meta = listItem.getItem().getItemMeta();
        meta.setDisplayName(ACFBukkitUtil.color(settingsManager.getProperty(GuiSettings.GUILD_LIST_ITEM_NAME).replace("{player}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName())));
        meta.setLore(updatedLore(guild, settingsManager.getProperty(GuiSettings.GUILD_LIST_HEAD_LORE)));
        listItem.getItem().setItemMeta(meta);
        pane.addItem(listItem);
    }

    private List<String> updatedLore(Guild guild, List<String> lore) {
        List<String> updated = new ArrayList<>();
        lore.forEach(line -> updated.add(ACFBukkitUtil.color(line
                    .replace("{guild-name}", guild.getName())
                    .replace("{guild-prefix}", guild.getPrefix())
                    .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName())
                    .replace("{guild-status}", guild.getStatus().name())
                    .replace("{guild-tier}", String.valueOf(guild.getTier().getLevel()))
                    .replace("{guild-balance}", String.valueOf(guild.getBalance()))
                    .replace("{guild-member-count}", String.valueOf(guild.getSize())))));
        return updated;
    }

}
