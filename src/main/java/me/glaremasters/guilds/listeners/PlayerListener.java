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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.JSONMessage;
import me.glaremasters.guilds.utils.StringUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:31 PM
 */
public class PlayerListener implements Listener {

    //todo

    private GuildHandler guildHandler;
    private SettingsManager settingsManager;
    private Guilds guilds;
    private Permission permission;

    private final Set<UUID> ALREADY_INFORMED = new HashSet<>();

    public PlayerListener(GuildHandler guildHandler, SettingsManager settingsManager, Guilds guilds, Permission permission) {
        this.guildHandler = guildHandler;
        this.settingsManager = settingsManager;
        this.guilds = guilds;
        this.permission = permission;
    }

    /**
     * This will check if a user is OP and will inform them of any important announcements from the Guild's Developer
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_IN_GAME)) {
            Guilds.newChain().delay(5, TimeUnit.SECONDS).sync(() -> {
                if (!player.isOp())
                    return;

                if (ALREADY_INFORMED.contains(player.getUniqueId()))
                    return;

                try {
                    JSONMessage.create(StringUtils.color("&f[&aGuilds&f]&r Announcements (Hover over me for more information)")).tooltip(StringUtils.getAnnouncements(guilds)).openURL(guilds.getDescription().getWebsite()).send(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ALREADY_INFORMED.add(player.getUniqueId());
            }).execute();
        }
    }

    /**
     * Send the player their guild's motd if they have one
     * @param event player join event
     */
    @EventHandler
    public void tryMotd(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        if (guild == null)
            return;

        if (guild.getMotd() == null)
            return;

        if (!settingsManager.getProperty(GuildSettings.MOTD_ON_LOGIN))
            return;

        Guilds.newChain().delay(5, TimeUnit.SECONDS).sync(() -> guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.MOTD__MOTD, "{motd}", guild.getMotd())).execute();
    }

    /**
     * Set the last login time of a player
     * @param event
     */
    @EventHandler
    public void updateLastLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        // Check if guild is null
        if (guild == null) {
            return;
        }

        // Create member object
        GuildMember member = guild.getMember(player.getUniqueId());

        // Set their join date to the first time they login after the update
        if (member.getJoinDate() == 0) {
            member.setJoinDate(System.currentTimeMillis());
        }

        // Set their last login
        member.setLastLogin(System.currentTimeMillis());
    }

    /**
     * Handles guild chat
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        if (guild == null) return;

        if (guildHandler.checkGuildChat(player)) {
            guildHandler.handleGuildChat(guild, player, event.getMessage());
            event.setCancelled(true);
        }
    }

    /**
     * Make sure the player is still not in these modes when logging out.
     * @param event
     */
    @EventHandler
    public void chatLeave(PlayerQuitEvent event) {
        guildHandler.chatLogout(event.getPlayer());
    }

    /**
     * Make sure the player has all the perms for their current tier.
     * @param event
     */
    @EventHandler
    public void permCheck(PlayerJoinEvent event) {
        guildHandler.addPerms(permission, event.getPlayer(), settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC));
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        if (guild == null) {
            return;
        }

        if (guild.getHome() == null) {
            return;
        }

        if (!settingsManager.getProperty(GuildSettings.REPSPAWN_AT_HOME)) {
            return;
        }

        event.setRespawnLocation(guild.getHome().getAsLocation());
    }
}
