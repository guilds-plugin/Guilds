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
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.GuiUtils;
import me.glaremasters.guilds.utils.ItemBuilder;
import me.glaremasters.guilds.utils.XMaterial;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:17 PM
 */
public class InfoGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;
    private CooldownHandler cooldownHandler;
    private CommandManager commandManager;

    public InfoGUI(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler, CooldownHandler cooldownHandler, CommandManager commandManager) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
        this.cooldownHandler = cooldownHandler;
        this.commandManager = commandManager;
    }

    public Gui getInfoGUI(Guild guild, Player player) {

        // Create the GUI with the desired name from the config
        Gui gui = new Gui(guilds, 3, ACFBukkitUtil.color(settingsManager.getProperty(GuildInfoSettings.GUI_NAME).replace("{name}",
                guild.getName()).replace("{prefix}", guild.getPrefix())));

        // Prevent players from being able to items into the GUIs
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        // Create the background pane which will just be stained glass
        OutlinePane backgroundPane = new OutlinePane(0, 0, 9, 3, Pane.Priority.LOW);

        // Create the pane for the main items
        OutlinePane foregroundPane = new OutlinePane(2, 1, 5, 1, Pane.Priority.NORMAL);

        // Create the pane for the guild vault item
        OutlinePane vaultPane = new OutlinePane(4, 2, 1, 1, Pane.Priority.HIGH);

        // Creat the mane for the motd item
        OutlinePane motdPane = new OutlinePane(4, 0, 1, 1, Pane.Priority.HIGH);

        // Add the items to the background pane
        createBackgroundItems(backgroundPane);

        // Add the items to the foreground pane
        createForegroundItems(foregroundPane, guild, player);

        // Add the vault item to the vault pane
        createVaultItem(vaultPane, guild, player);

        // Add the motd item to the info pane
        createMotdItem(motdPane, guild);

        // Add the glass panes to the main GUI background pane
        gui.addPane(backgroundPane);

        // Add the foreground pane to the GUI
        gui.addPane(foregroundPane);

        // Add the vault pane to the GUI
        gui.addPane(vaultPane);

        // Add the motd pane to the gui
        gui.addPane(motdPane);

        // Return the new info GUI object
        return gui;
    }

    /**
     * Create the background panes
     *
     * @param pane the pane to add to
     */
    private void createBackgroundItems(OutlinePane pane) {
        // Start the itembuilder with stained glass
        ItemBuilder builder = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
        // Set the name to be empty
        builder.setName(ACFBukkitUtil.color("&r"));
        // Loop through 27 (three rows)
        for (int i = 0; i < 27; i++) {
            // Add the pane item to the GUI and cancel the click event on it
            pane.addItem(new GuiItem(builder.build(), event -> event.setCancelled(true)));
        }
    }

    /**
     * Create the regular items that will be on the GUI
     *
     * @param pane  the pane to be added to
     * @param guild the guild of the player
     */
    private void createForegroundItems(OutlinePane pane, Guild guild, Player player) {

        // Create an easy way to access the guild tier
        GuildTier tier = guildHandler.getGuildTier(guild.getTier().getLevel());

        // Add the tier button to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.TIER_DISPLAY)) {
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.TIER_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.TIER_NAME),
                    settingsManager.getProperty(GuildInfoSettings.TIER_LORE).stream().map(l ->
                            l.replace("{tier}", tier.getName())).collect(Collectors.toList())),
                    event -> event.setCancelled(true)));
        }
        // Add the bank button to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.BANK_DISPLAY)) {
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.BANK_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.BANK_NAME),
                    settingsManager.getProperty(GuildInfoSettings.BANK_LORE).stream().map(l ->
                            l.replace("{current}", String.valueOf(guild.getBalance())).replace("{max}",
                                    String.valueOf(tier.getMaxBankBalance()))).collect(Collectors.toList())),
                    event -> event.setCancelled(true)));
        }
        // Add the members button to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.MEMBERS_DISPLAY)) {
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.MEMBERS_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.MEMBERS_NAME),
                    settingsManager.getProperty(GuildInfoSettings.MEMBERS_LORE).stream().map(l ->
                            l.replace("{current}", String.valueOf(guild.getMembers().size()))
                                    .replace("{max}", String.valueOf(tier.getMaxMembers()))
                                    .replace("{online}", String.valueOf(guild.getOnlineMembers().size()))).collect(Collectors.toList())),

                    event -> {
                        // Cancel the event
                        event.setCancelled(true);
                        // Open the new GUI
                        guilds.getGuiHandler().getInfoMembersGUI().getInfoMembersGUI(guild).show(event.getWhoClicked());
                    }));
        }
        // Add the status button to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.STATUS_DISPLAY)) {
            boolean status = guild.isPrivate();
            String material;
            String statusString;
            if (status) {
                material = settingsManager.getProperty(GuildInfoSettings.STATUS_MATERIAL_PRIVATE);
                statusString = settingsManager.getProperty(GuildInfoSettings.STATUS_PRIVATE);
            } else {
                material = settingsManager.getProperty(GuildInfoSettings.STATUS_MATERIAL_PUBLIC);
                statusString = settingsManager.getProperty(GuildInfoSettings.STATUS_PUBLIC);
            }
            pane.addItem(new GuiItem(GuiUtils.createItem(material,
                    settingsManager.getProperty(GuildInfoSettings.STATUS_NAME),
                    settingsManager.getProperty(GuildInfoSettings.STATUS_LORE).stream().map(l ->
                            l.replace("{status}", statusString)).collect(Collectors.toList())),
                    event -> event.setCancelled(true)));
        }
        if (settingsManager.getProperty(GuildInfoSettings.HOME_DISPLAY)) {
            // Create a variable for the home
            String home;
            // Check if the home is null or not
            if (guild.getHome() == null) {
                // If null, take the empty string
                home = settingsManager.getProperty(GuildInfoSettings.HOME_EMPTY);
            } else {
                // If not, list the location
                home = ACFBukkitUtil.blockLocationToString(guild.getHome().getAsLocation());
            }
            // Add the home button to the GUI
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.HOME_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.HOME_NAME),
                    settingsManager.getProperty(GuildInfoSettings.HOME_LORE).stream().map(l ->
                            l.replace("{coords}", home)).collect(Collectors.toList())),
                    event -> {
                        event.setCancelled(true);
                        if (cooldownHandler.hasCooldown(Cooldown.Type.Home.name(), player.getUniqueId())) {
                            commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__COOLDOWN, "{amount}",
                                    String.valueOf(cooldownHandler.getRemaining(Cooldown.Type.Home.name(), player.getUniqueId())));
                            return;
                        }

                        if (settingsManager.getProperty(GuildInfoSettings.HOME_TELEPORT)) {
                            if (guild.getHome() == null) {
                                commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__NO_HOME_SET);
                                return;
                            }

                            if (settingsManager.getProperty(CooldownSettings.WU_HOME_ENABLED)) {
                                Location initial = player.getLocation();
                                commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__WARMUP, "{amount}", String.valueOf(settingsManager.getProperty(CooldownSettings.WU_HOME)));
                                Guilds.newChain().delay(settingsManager.getProperty(CooldownSettings.WU_HOME), TimeUnit.SECONDS).sync(() -> {
                                    Location curr = player.getLocation();
                                    if (initial.distance(curr) > 1) {
                                        guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.HOME__CANCELLED);
                                    } else {
                                        player.teleport(guild.getHome().getAsLocation());
                                        guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED);
                                    }
                                }).execute();
                            } else {
                                player.teleport(guild.getHome().getAsLocation());
                                commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED);
                            }
                            cooldownHandler.addCooldown(player, Cooldown.Type.Home.name(), settingsManager.getProperty(CooldownSettings.HOME), TimeUnit.SECONDS);
                        }
                    }));
        }
    }

    /**
     * Create the vault item
     *
     * @param pane  the pane to be added to
     * @param guild the guild of the player
     */
    private void createVaultItem(OutlinePane pane, Guild guild, Player player) {
        // Add the vault item to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.VAULT_DISPLAY)) {
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.VAULT_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.VAULT_NAME),
                    settingsManager.getProperty(GuildInfoSettings.VAULT_LORE)),
                    event -> {
                        // Cancel the event
                        event.setCancelled(true);
                        // Check if they have the perms to open the vaults
                        if (!guild.getMember(player.getUniqueId()).getRole().isOpenVault())
                            return;
                        // Open the new GUI
                        guilds.getGuiHandler().getVaultGUI().getVaultGUI(guild, player, guilds.getCommandManager()).show(event.getWhoClicked());
                    }));
        }
    }

    /**
     * Create the motd itmestack for the GUI
     *
     * @param pane  the pane to add it to
     * @param guild the guild that you're getting the motd of
     */
    private void createMotdItem(OutlinePane pane, Guild guild) {
        // Add the MOTD item to the GUI
        if (settingsManager.getProperty(GuildInfoSettings.MOTD_DISPLAY)) {
            String motd = guild.getMotd() == null ? "" : guild.getMotd();
            pane.addItem(new GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.MOTD_MATERIAL),
                    settingsManager.getProperty(GuildInfoSettings.MOTD_NAME),
                    settingsManager.getProperty(GuildInfoSettings.MOTD_LORE).stream().map(l -> l.replace("{motd}", motd)).collect(Collectors.toList())),
                    event -> event.setCancelled(true)));
        }
    }
}
