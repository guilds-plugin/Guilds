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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.GuildBuffSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.EconomyUtils;
import me.glaremasters.guilds.utils.GuiBuilder;
import me.glaremasters.guilds.utils.GuiUtils;
import me.glaremasters.guilds.utils.StringUtils;
import org.bukkit.Bukkit;
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
public class BuffGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private SettingsManager mainConfig;
    private GuildHandler guildHandler;
    private CommandManager commandManager;
    private CooldownHandler cooldownHandler;

    public BuffGUI(Guilds guilds, SettingsManager settingsManager, SettingsManager mainConfig, GuildHandler guildHandler, CommandManager commandManager, CooldownHandler cooldownHandler) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.mainConfig = mainConfig;
        this.guildHandler = guildHandler;
        this.commandManager = commandManager;
        this.cooldownHandler = cooldownHandler;
    }

    public Gui getBuffGUI() {
        String name = settingsManager.getProperty(GuildBuffSettings.GUILD_BUFF_NAME);

        Gui gui = new GuiBuilder(guilds).setName(name).setRows(1).blockGlobalClick().build();

        StaticPane pane = new StaticPane(0, 0, 9, 1);

        // Haste
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.HASTE_TYPE),
                settingsManager.getProperty(GuildBuffSettings.HASTE_TIME),
                settingsManager.getProperty(GuildBuffSettings.HASTE_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.HASTE_PRICE),
                settingsManager.getProperty(GuildBuffSettings.HASTE_ICON),
                settingsManager.getProperty(GuildBuffSettings.HASTE_NAME),
                settingsManager.getProperty(GuildBuffSettings.HASTE_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.HASTE_SLOT),
                settingsManager.getProperty(GuildBuffSettings.HASTE_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.HASTE_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.HASTE_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.HASTE_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.HASTE_GUILD_COMMANDS
                ));
        // Speed
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.SPEED_TYPE),
                settingsManager.getProperty(GuildBuffSettings.SPEED_TIME),
                settingsManager.getProperty(GuildBuffSettings.SPEED_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.SPEED_PRICE),
                settingsManager.getProperty(GuildBuffSettings.SPEED_ICON),
                settingsManager.getProperty(GuildBuffSettings.SPEED_NAME),
                settingsManager.getProperty(GuildBuffSettings.SPEED_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.SPEED_SLOT),
                settingsManager.getProperty(GuildBuffSettings.SPEED_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.SPEED_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.SPEED_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.SPEED_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.SPEED_GUILD_COMMANDS
                ));
        // Fire
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.FR_TYPE),
                settingsManager.getProperty(GuildBuffSettings.FR_TIME),
                settingsManager.getProperty(GuildBuffSettings.FR_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.FR_PRICE),
                settingsManager.getProperty(GuildBuffSettings.FR_ICON),
                settingsManager.getProperty(GuildBuffSettings.FR_NAME),
                settingsManager.getProperty(GuildBuffSettings.FR_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.FR_SLOT),
                settingsManager.getProperty(GuildBuffSettings.FR_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.FR_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.FR_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.FR_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.FR_GUILD_COMMANDS
                ));
        // Night
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.NV_TYPE),
                settingsManager.getProperty(GuildBuffSettings.NV_TIME),
                settingsManager.getProperty(GuildBuffSettings.NV_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.NV_PRICE),
                settingsManager.getProperty(GuildBuffSettings.NV_ICON),
                settingsManager.getProperty(GuildBuffSettings.NV_NAME),
                settingsManager.getProperty(GuildBuffSettings.NV_LORE),
                pane, settingsManager.getProperty(GuildBuffSettings.NV_SLOT),
                settingsManager.getProperty(GuildBuffSettings.NV_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.NV_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.NV_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.NV_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.NV_GUILD_COMMANDS
                ));
        // Invisibility
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_TYPE),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_TIME),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_PRICE),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_ICON),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_NAME),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_SLOT),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.INVISIBILITY_GUILD_COMMANDS
                ));
        // Strength
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_TYPE),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_TIME),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_PRICE),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_ICON),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_NAME),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_SLOT),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.STRENGTH_GUILD_COMMANDS
                ));
        // Jump
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.JUMP_TYPE),
                settingsManager.getProperty(GuildBuffSettings.JUMP_TIME),
                settingsManager.getProperty(GuildBuffSettings.JUMP_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.JUMP_PRICE),
                settingsManager.getProperty(GuildBuffSettings.JUMP_ICON),
                settingsManager.getProperty(GuildBuffSettings.JUMP_NAME),
                settingsManager.getProperty(GuildBuffSettings.JUMP_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.JUMP_SLOT),
                settingsManager.getProperty(GuildBuffSettings.JUMP_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.JUMP_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.JUMP_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.JUMP_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.JUMP_GUILD_COMMANDS
                ));
        // Water
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.WB_TYPE),
                settingsManager.getProperty(GuildBuffSettings.WB_TIME),
                settingsManager.getProperty(GuildBuffSettings.WB_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.WB_PRICE),
                settingsManager.getProperty(GuildBuffSettings.WB_ICON),
                settingsManager.getProperty(GuildBuffSettings.WB_NAME),
                settingsManager.getProperty(GuildBuffSettings.WB_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.WB_SLOT),
                settingsManager.getProperty(GuildBuffSettings.WB_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.WB_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.WB_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.WB_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.WB_GUILD_COMMANDS
                ));
        // Regen
        setBuffItem(commandManager,
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_TYPE),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_TIME),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_AMPLIFIER),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_PRICE),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_ICON),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_NAME),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_LORE),
                pane,
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_SLOT),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_DISPLAY),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_CLICKER_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_CLICKER_COMMANDS),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_GUILD_COMMAND_CHECK),
                settingsManager.getProperty(GuildBuffSettings.REGENERATION_GUILD_COMMANDS
                ));

        gui.addPane(pane);

        return gui;
    }

    /**
     * Set the buff item to the GUI
     *
     * @param commandManager command manage
     * @param type           the type of potion
     * @param length         the length of the potion
     * @param amplifier      the strength of the potion
     * @param cost           the cost of the potion
     * @param icon           the icon of the potion
     * @param name           the name of the potion
     * @param lore           the lore of the potion
     * @param pane           the pane to add to
     * @param x              The location to add to
     * @param check          check if this should be displayed
     */
    private void setBuffItem(CommandManager commandManager, String type, int length, int amplifier,
                             double cost, String icon, String name, List<String> lore, StaticPane pane, int x,
                             boolean check, boolean clickerCheck, List<String> clickerCommands, boolean guildCheck, List<String> guildCommands) {
        ItemStack item = GuiUtils.createItem(icon, name, lore);
        GuiItem buffItem = new GuiItem(item, event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (guildHandler.getGuild(player) == null) return;
            Guild guild = guildHandler.getGuild(player);
            if (settingsManager.getProperty(GuildBuffSettings.PER_BUFF_PERMISSIONS)) {
                if (!player.hasPermission("guilds.buff." + type.toLowerCase())) {
                    commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_NO_PERMISSION);
                    return;
                }
            }
            if (cooldownHandler.hasCooldown(Cooldown.Type.Buffs.name(), guild.getId())) {
                commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_COOLDOWN, "{amount}", String.valueOf(cooldownHandler.getRemaining(Cooldown.Type.Buffs.name(), guild.getId())));
                return;
            }
            if (!EconomyUtils.hasEnough(guild.getBalance(), cost)) {
                commandManager.getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
                return;
            }
            if (!settingsManager.getProperty(GuildBuffSettings.BUFF_STACKING) && !player.getActivePotionEffects().isEmpty())
                return;
            guild.setBalance(guild.getBalance() - cost);
            guild.addPotion(type, (length * 20), amplifier);
            cooldownHandler.addCooldown(guild, Cooldown.Type.Buffs.name(), mainConfig.getProperty(CooldownSettings.BUFF), TimeUnit.SECONDS);

            executeClickerCommands(clickerCheck, clickerCommands, player);
            executeGuildCommands(guildCheck, guildCommands, guild);
        });
        ItemMeta meta = buffItem.getItem().getItemMeta();
        meta.setDisplayName(StringUtils.color(name));
        meta.setLore(lore.stream().map(StringUtils::color).collect(Collectors.toList()));
        buffItem.getItem().setItemMeta(meta);
        if (check)
            pane.addItem(buffItem, x, 0);
    }

    /**
     * Execute a list of commands on the player who bought the buff
     *
     * @param check    if this can run or not
     * @param commands the commands to run
     * @param player   the player to execute them on
     */
    private void executeClickerCommands(boolean check, List<String> commands, Player player) {
        if (check) {
            commands.forEach(c -> {
                String update = c.replace("{player}", player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), update);
            });
        }
    }

    /**
     * Execute a list of commands on all players online in a guild
     *
     * @param check    if this can run or not
     * @param commands the commands to run
     * @param guild    the guild of players to run on
     */
    private void executeGuildCommands(boolean check, List<String> commands, Guild guild) {
        if (check) {
            guild.getOnlineAsPlayers().forEach(p -> commands.forEach(c -> {
                String update = c.replace("{player}", p.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), update);
            }));
        }
    }


}
