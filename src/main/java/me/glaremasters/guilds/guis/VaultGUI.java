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
import co.aikar.commands.CommandManager;
import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.VaultPickerSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.ItemBuilder;
import me.glaremasters.guilds.utils.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 5/12/2019
 * Time: 1:57 PM
 */
public class VaultGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;
    private static int num = 0;

    public VaultGUI(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
    }

    public Gui getVaultGUI(Guild guild, Player player, CommandManager commandManager) {

        // Create the GUI with the desired name from the config
        Gui gui = new Gui(guilds, settingsManager.getProperty(VaultPickerSettings.GUI_SIZE),
                ACFBukkitUtil.color(settingsManager.getProperty(VaultPickerSettings.GUI_NAME).replace("{name}",
                guild.getName())));

        // Prevent players from being able to items into the GUIs
        gui.setOnOutsideClick(event -> {
            event.setCancelled(true);
            guilds.getGuiHandler().getInfoGUI().getInfoGUI(guild, player).show(event.getWhoClicked());
        });

        // Create the pane for the main items
        OutlinePane foregroundPane = new OutlinePane(0, 0, 9, settingsManager.getProperty(VaultPickerSettings.GUI_SIZE), Pane.Priority.NORMAL);

        // Add the items to the foreground pane
        createForegroundItems(foregroundPane, guild, player, commandManager);

        // Set it back to 0
        num = 0;

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
    private void createForegroundItems(OutlinePane pane, Guild guild, Player player, CommandManager commandManager) {
        int max = guildHandler.getGuildTier(guild.getTier().getLevel()).getVaultAmount();
        for (int i = 0; i < max; i++) {
            String status;
            if (guildHandler.hasVaultUnlocked((i + 1), guild)) {
                status = settingsManager.getProperty(VaultPickerSettings.PICKER_UNLOCKED);
            } else {
                status = settingsManager.getProperty(VaultPickerSettings.PICKER_LOCKED);
            }
            pane.addItem(new GuiItem(easyItem(settingsManager.getProperty(VaultPickerSettings.PICKER_MATERIAL),
                    settingsManager.getProperty(VaultPickerSettings.PICKER_NAME),
                    settingsManager.getProperty(VaultPickerSettings.PICKER_LORE).stream().map(l ->
                            l.replace("{number}", String.valueOf(num + 1))
                                    .replace("{status}", status)).collect(Collectors.toList())), event -> {
                event.setCancelled(true);
                try {
                    guildHandler.getGuildVault(guild, (event.getRawSlot() + 1));
                } catch (IndexOutOfBoundsException ex) {
                    guildHandler.getCachedVaults().get(guild).add(guildHandler.createNewVault(settingsManager));
                }
                player.openInventory(guildHandler.getGuildVault(guild, (event.getRawSlot() + 1)));
                guildHandler.getOpenedVault().add(player);
            }));
            num++;
        }
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
        ItemBuilder builder = new ItemBuilder(XMaterial.matchXMaterial(material).parseMaterial());
        // Sets the name of the item
        builder.setName(ACFBukkitUtil.color(name));
        // Sets the lore of the item
        builder.setLore(lore.stream().map(ACFBukkitUtil::color).collect(Collectors.toList()));
        // Return the created item
        return builder.build();
    }

}
