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
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.GuildBuffSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.EconomyUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 4/12/2019
 * Time: 10:01 PM
 */
@AllArgsConstructor
public class BuffGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;
    private CommandManager commandManager;
    private CooldownHandler cooldownHandler;

    public Gui getBuffGUI() {


        Gui gui = new Gui(guilds, 1, ACFBukkitUtil.color(settingsManager.getProperty(GuildBuffSettings.GUILD_BUFF_NAME)));

        // Prevent players from being able to items into the GUIs
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane pane = new StaticPane(0, 0, 9, 1);

        // Haste
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.HASTE_TYPE), settingsManager.getProperty(GuildBuffSettings.HASTE_TIME),
                settingsManager.getProperty(GuildBuffSettings.HASTE_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.HASTE_PRICE),
                settingsManager.getProperty(GuildBuffSettings.HASTE_ICON), settingsManager.getProperty(GuildBuffSettings.HASTE_NAME),
                settingsManager.getProperty(GuildBuffSettings.HASTE_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.HASTE_SLOT),
                settingsManager.getProperty(GuildBuffSettings.HASTE_DISPLAY));
        // Speed
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.SPEED_TYPE), settingsManager.getProperty(GuildBuffSettings.SPEED_TIME),
                settingsManager.getProperty(GuildBuffSettings.SPEED_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.SPEED_PRICE),
                settingsManager.getProperty(GuildBuffSettings.SPEED_ICON), settingsManager.getProperty(GuildBuffSettings.SPEED_NAME),
                settingsManager.getProperty(GuildBuffSettings.SPEED_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.SPEED_SLOT),
                settingsManager.getProperty(GuildBuffSettings.SPEED_DISPLAY));
        // Fire
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.FR_TYPE), settingsManager.getProperty(GuildBuffSettings.FR_TIME),
                settingsManager.getProperty(GuildBuffSettings.FR_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.FR_PRICE),
                settingsManager.getProperty(GuildBuffSettings.FR_ICON), settingsManager.getProperty(GuildBuffSettings.FR_NAME),
                settingsManager.getProperty(GuildBuffSettings.FR_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.FR_SLOT),
                settingsManager.getProperty(GuildBuffSettings.FR_DISPLAY));
        // Night
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.NV_TYPE), settingsManager.getProperty(GuildBuffSettings.NV_TIME),
                settingsManager.getProperty(GuildBuffSettings.NV_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.NV_PRICE),
                settingsManager.getProperty(GuildBuffSettings.NV_ICON), settingsManager.getProperty(GuildBuffSettings.NV_NAME),
                settingsManager.getProperty(GuildBuffSettings.NV_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.NV_SLOT),
                settingsManager.getProperty(GuildBuffSettings.NV_DISPLAY));
        // Invisibility
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_TYPE), settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_TIME),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_PRICE),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_ICON), settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_NAME),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_SLOT),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_DISPLAY));
        // Strength
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.STRENGTH_TYPE), settingsManager.getProperty(GuildBuffSettings.STRENGTH_TIME),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.STRENGTH_PRICE),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_ICON), settingsManager.getProperty(GuildBuffSettings.STRENGTH_NAME),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.STRENGTH_SLOT),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_DISPLAY));
        // Jump
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.JUMP_TYPE), settingsManager.getProperty(GuildBuffSettings.JUMP_TIME),
                settingsManager.getProperty(GuildBuffSettings.JUMP_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.JUMP_PRICE),
                settingsManager.getProperty(GuildBuffSettings.JUMP_ICON), settingsManager.getProperty(GuildBuffSettings.JUMP_NAME),
                settingsManager.getProperty(GuildBuffSettings.JUMP_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.JUMP_SLOT),
                settingsManager.getProperty(GuildBuffSettings.JUMP_DISPLAY));
        // Water
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.WB_TYPE), settingsManager.getProperty(GuildBuffSettings.WB_TIME),
                settingsManager.getProperty(GuildBuffSettings.WB_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.WB_PRICE),
                settingsManager.getProperty(GuildBuffSettings.WB_ICON), settingsManager.getProperty(GuildBuffSettings.WB_NAME),
                settingsManager.getProperty(GuildBuffSettings.WB_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.WB_SLOT),
                settingsManager.getProperty(GuildBuffSettings.WB_DISPLAY));
        // Regen
        setBuffItem(commandManager, settingsManager.getProperty(GuildBuffSettings.REGENERATION_TYPE), settingsManager.getProperty(GuildBuffSettings.REGENERATION_TIME),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_AMPLIFIER), settingsManager.getProperty(GuildBuffSettings.REGENERATION_PRICE),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_ICON), settingsManager.getProperty(GuildBuffSettings.REGENERATION_NAME),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.REGENERATION_SLOT),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_DISPLAY));

        gui.addPane(pane);

        return gui;
    }

    /**
     * Set the buff item to the GUI
     * @param commandManager command manage
     * @param type the type of potion
     * @param length the length of the potion
     * @param amplifier the strength of the potion
     * @param cost the cost of the potion
     * @param icon the icon of the potion
     * @param name the name of the potion
     * @param lore the lore of the potion
     * @param pane the pane to add to
     * @param x The location to add to
     * @param check check if this should be displayed
     */
    private void setBuffItem(CommandManager commandManager, String type, int length, int amplifier, double cost, String icon, String name, List<String> lore, StaticPane pane, int x, boolean check) {
        GuiItem buffItem = new GuiItem(new ItemStack(Material.valueOf(icon)), event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (guildHandler.getGuild(player) == null) return;
            Guild guild = guildHandler.getGuild(player);
            if (cooldownHandler.hasCooldown(Cooldown.TYPES.Buffs.name(), guild.getId())) {
                commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_COOLDOWN, "{amount}", String.valueOf(cooldownHandler.getRemaining(Cooldown.TYPES.Buffs.name(), guild.getId())));
                return;
            }
            if (!EconomyUtils.hasEnough(guild.getBalance(), cost)) {
                commandManager.getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
                return;
            }
            if (!settingsManager.getProperty(GuildBuffSettings.BUFF_STACKING) && !player.getActivePotionEffects().isEmpty()) return;
            guild.setBalance(guild.getBalance() - cost);
            guild.addPotion(type, (length * 20), amplifier);
            cooldownHandler.addCooldown(guild, Cooldown.TYPES.Buffs.name(), settingsManager.getProperty(CooldownSettings.BUFF), TimeUnit.SECONDS);
        });
        ItemMeta meta = buffItem.getItem().getItemMeta();
        meta.setDisplayName(ACFBukkitUtil.color(name));
        meta.setLore(lore.stream().map(ACFBukkitUtil::color).collect(Collectors.toList()));
        buffItem.getItem().setItemMeta(meta);
        if (check)
            pane.addItem(buffItem, x, 0);
    }


}
