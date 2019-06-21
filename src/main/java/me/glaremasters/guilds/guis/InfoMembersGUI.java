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
import com.github.stefvanschie.inventoryframework.pane.Pane;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildInfoMemberSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 5/12/2019
 * Time: 1:57 PM
 */
@AllArgsConstructor
public class InfoMembersGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;

    public Gui getInfoMembersGUI(Guild guild) {

        // Create the GUI with the desired name from the config
        Gui gui = new Gui(guilds, 6, ACFBukkitUtil.color(settingsManager.getProperty(GuildInfoMemberSettings.GUI_NAME).replace("{name}",
                guild.getName())));

        // Prevent players from being able to items into the GUIs
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        // Create the pane for the main items
        OutlinePane foregroundPane = new OutlinePane(0, 0, 9, 6, Pane.Priority.NORMAL);

        // Add the items to the foreground pane
        createForegroundItems(foregroundPane, guild);

        // Add the foreground pane to the GUI
        gui.addPane(foregroundPane);

        // Return the create GUI object
        return gui;
    }

    /**
     * Create the regular items that will be on the GUI
     * @param pane the pane to be added to
     * @param guild the guild of the player
     */
    private void createForegroundItems(OutlinePane pane, Guild guild) {

        List<GuildMember> members = guild.getMembers();
        members.sort(Comparator.comparingInt(g -> g.getRole().getLevel()));

        members.forEach(m -> {

            // Create a variable for the status
            String status;

            // Create a variable for the role
            GuildRole role = guildHandler.getGuildRole(m.getRole().getLevel());

            // Create a variable for the name of the player
            String name = Bukkit.getOfflinePlayer(m.getUuid()).getName();

            // Check if they are online
            if (m.isOnline()) {
                // Use the online string
                status = settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_ONLINE);
            } else {
                // Use the offline string
                status = settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_OFFLINE);
            }

            pane.addItem(new GuiItem(easyItem(settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_MATERIAL),
                    settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_NAME).replace("{player}", name),
                    settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_LORE).stream().map(l ->
                            l.replace("{name}", name)
                                    .replace("{role}", role.getName())
                                    .replace("{status}", status)).collect(Collectors.toList())),
                    event -> event.setCancelled(true)));
        });
    }

    /**
     * Easily create an item for the GUI
     * @param material the material of the item
     * @param name the name of the item
     * @param lore the lore of the item
     * @return created itemstack
     */
    private ItemStack easyItem(String material, String name, List<String> lore) {
        // Start the itembuilder
        ItemBuilder builder = new ItemBuilder(Material.valueOf(material));
        // Sets the name of the item
        builder.setName(ACFBukkitUtil.color(name));
        // Sets the lore of the item
        builder.setLore(lore.stream().map(ACFBukkitUtil::color).collect(Collectors.toList()));
        // Return the created item
        return builder.build();
    }

}
