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

package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 4/12/2019
 * Time: 10:01 PM
 */
@AllArgsConstructor
public class GuildBuffManager {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;

    public Gui BuffGUI() {


        Gui gui = new Gui(guilds, 1, ACFBukkitUtil.color(settingsManager.getProperty(GuiSettings.GUILD_BUFF_NAME)));

        StaticPane pane = new StaticPane(0, 0, 9, 1);

        // Haste
        setBuffItem(settingsManager.getProperty(GuiSettings.HASTE_TYPE), settingsManager.getProperty(GuiSettings.HASTE_TIME),
                settingsManager.getProperty(GuiSettings.HASTE_AMPLIFIER), settingsManager.getProperty(GuiSettings.HASTE_PRICE),
                settingsManager.getProperty(GuiSettings.HASTE_ICON), settingsManager.getProperty(GuiSettings.HASTE_NAME),
                settingsManager.getProperty(GuiSettings.HASTE_LORE), 
                pane, 0, 0,
                settingsManager.getProperty(GuiSettings.HASTE_DISPLAY));
        // Speed
        setBuffItem(settingsManager.getProperty(GuiSettings.SPEED_TYPE), settingsManager.getProperty(GuiSettings.SPEED_TIME),
                settingsManager.getProperty(GuiSettings.SPEED_AMPLIFIER), settingsManager.getProperty(GuiSettings.SPEED_PRICE),
                settingsManager.getProperty(GuiSettings.SPEED_ICON), settingsManager.getProperty(GuiSettings.SPEED_NAME),
                settingsManager.getProperty(GuiSettings.SPEED_LORE),
                pane, 1, 0,
                settingsManager.getProperty(GuiSettings.SPEED_DISPLAY));
        // Fire
        setBuffItem(settingsManager.getProperty(GuiSettings.FR_TYPE), settingsManager.getProperty(GuiSettings.FR_TIME),
                settingsManager.getProperty(GuiSettings.FR_AMPLIFIER), settingsManager.getProperty(GuiSettings.FR_PRICE),
                settingsManager.getProperty(GuiSettings.FR_ICON), settingsManager.getProperty(GuiSettings.FR_NAME),
                settingsManager.getProperty(GuiSettings.FR_LORE),
                pane, 2, 0,
                settingsManager.getProperty(GuiSettings.FR_DISPLAY));
        // Night
        setBuffItem(settingsManager.getProperty(GuiSettings.NV_TYPE), settingsManager.getProperty(GuiSettings.NV_TIME),
                settingsManager.getProperty(GuiSettings.NV_AMPLIFIER), settingsManager.getProperty(GuiSettings.NV_PRICE),
                settingsManager.getProperty(GuiSettings.NV_ICON), settingsManager.getProperty(GuiSettings.NV_NAME),
                settingsManager.getProperty(GuiSettings.NV_LORE),
                pane, 3, 0,
                settingsManager.getProperty(GuiSettings.NV_DISPLAY));
        // Invisibility
        setBuffItem(settingsManager.getProperty(GuiSettings.INVISIBILITY_TYPE), settingsManager.getProperty(GuiSettings.INVISIBILITY_TIME),
                settingsManager.getProperty(GuiSettings.INVISIBILITY_AMPLIFIER), settingsManager.getProperty(GuiSettings.INVISIBILITY_PRICE),
                settingsManager.getProperty(GuiSettings.INVISIBILITY_ICON), settingsManager.getProperty(GuiSettings.INVISIBILITY_NAME),
                settingsManager.getProperty(GuiSettings.INVISIBILITY_LORE),
                pane, 4, 0,
                settingsManager.getProperty(GuiSettings.INVISIBILITY_DISPLAY));
        // Strength
        setBuffItem(settingsManager.getProperty(GuiSettings.STRENGTH_TYPE), settingsManager.getProperty(GuiSettings.STRENGTH_TIME),
                settingsManager.getProperty(GuiSettings.STRENGTH_AMPLIFIER), settingsManager.getProperty(GuiSettings.STRENGTH_PRICE),
                settingsManager.getProperty(GuiSettings.STRENGTH_ICON), settingsManager.getProperty(GuiSettings.STRENGTH_NAME),
                settingsManager.getProperty(GuiSettings.STRENGTH_LORE),
                pane, 5, 0,
                settingsManager.getProperty(GuiSettings.STRENGTH_DISPLAY));
        // Jump
        setBuffItem(settingsManager.getProperty(GuiSettings.JUMP_TYPE), settingsManager.getProperty(GuiSettings.JUMP_TIME),
                settingsManager.getProperty(GuiSettings.JUMP_AMPLIFIER), settingsManager.getProperty(GuiSettings.JUMP_PRICE),
                settingsManager.getProperty(GuiSettings.JUMP_ICON), settingsManager.getProperty(GuiSettings.JUMP_NAME),
                settingsManager.getProperty(GuiSettings.JUMP_LORE),
                pane, 6, 0,
                settingsManager.getProperty(GuiSettings.JUMP_DISPLAY));
        // Water
        setBuffItem(settingsManager.getProperty(GuiSettings.WB_TYPE), settingsManager.getProperty(GuiSettings.WB_TIME),
                settingsManager.getProperty(GuiSettings.WB_AMPLIFIER), settingsManager.getProperty(GuiSettings.WB_PRICE),
                settingsManager.getProperty(GuiSettings.WB_ICON), settingsManager.getProperty(GuiSettings.WB_NAME),
                settingsManager.getProperty(GuiSettings.WB_LORE),
                pane, 7, 0,
                settingsManager.getProperty(GuiSettings.WB_DISPLAY));
        // Regen
        setBuffItem(settingsManager.getProperty(GuiSettings.REGENERATION_TYPE), settingsManager.getProperty(GuiSettings.REGENERATION_TIME),
                settingsManager.getProperty(GuiSettings.REGENERATION_AMPLIFIER), settingsManager.getProperty(GuiSettings.REGENERATION_PRICE),
                settingsManager.getProperty(GuiSettings.REGENERATION_ICON), settingsManager.getProperty(GuiSettings.REGENERATION_NAME),
                settingsManager.getProperty(GuiSettings.REGENERATION_LORE),
                pane, 8, 0,
                settingsManager.getProperty(GuiSettings.REGENERATION_DISPLAY));

        gui.addPane(pane);

        return gui;
    }

    private void setBuffItem(String type, int length, int amplifier, double cost, String icon, String name, List<String> lore, StaticPane pane, int x, int y, boolean check) {
        GuiItem buffItem = new GuiItem(new ItemStack(Material.valueOf(icon)), event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (guildHandler.getGuild(player) == null) return;
            Guild guild = guildHandler.getGuild(player);
            if (!EconomyUtils.hasEnough(guild.getBalance(), cost)) return;
            if (settingsManager.getProperty(GuiSettings.BUFF_STACKING) && !player.getActivePotionEffects().isEmpty()) return;
            guild.setBalance(guild.getBalance() - cost);
            guild.addPotion(type, length, amplifier);
        });
        ItemMeta meta = buffItem.getItem().getItemMeta();
        meta.setDisplayName(ACFBukkitUtil.color(name));
        meta.setLore(lore.stream().map(ACFBukkitUtil::color).collect(Collectors.toList()));
        buffItem.getItem().setItemMeta(meta);
        if (check)
            pane.addItem(buffItem, x, y);
    }


}
