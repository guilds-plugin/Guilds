/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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

import co.aikar.commands.BukkitCommandIssuer;
import com.google.common.collect.Maps;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Glare
 * Date: 2/20/2021
 * Time: 5:45 PM
 */
public class ChatListener implements Listener {
    private final Map<UUID, ChatType> playerChatMap = Maps.newConcurrentMap();
    private final Guilds guilds;
    private final GuildHandler guildHandler;

    public ChatListener(Guilds guilds) {
        this.guilds = guilds;
        this.guildHandler = guilds.getGuildHandler();

        Bukkit.getPluginManager().registerEvents(this, guilds);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatLowest(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final ChatType chatType = playerChatMap.get(player.getUniqueId());

        if (chatType == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatHighest(final AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) { //Event should already be cancelled in Lowest Priority
            return;
        }

        final Player player = event.getPlayer();
        final ChatType chatType = playerChatMap.get(player.getUniqueId());
        final String message = event.getMessage();

        if (chatType == null) {
            return;
        }

        final Guild guild = guildHandler.getGuild(player);

        if (guild == null) {
            return;
        }

        if (chatType.equals(ChatType.GUILD)) {
            guildHandler.handleGuildChat(guild, player, message);
            return;
        }

        if (chatType.equals(ChatType.ALLY)) {
            guildHandler.handleAllyChat(guild, player, message);
        }
    }

    @EventHandler
    public void onLogout(final PlayerQuitEvent event) {
        guildHandler.chatLogout(event.getPlayer());
    }

    public void handleToggle(final Player player, final ChatType chatType) {
        final BukkitCommandIssuer issuer = guilds.getCommandManager().getCommandIssuer(player);
        if (playerChatMap.containsKey(player.getUniqueId())) {
            final ChatType type = playerChatMap.remove(player.getUniqueId());
            if (type == chatType) {
                issuer.sendInfo(Messages.CHAT__TOGGLED_OFF, "{type}", chatType.translate(player, guilds));
                return;
            }

            issuer.sendInfo(Messages.CHAT__TOGGLED_OFF, "{type}", type.translate(player, guilds));
            issuer.sendInfo(Messages.CHAT__TOGGLED_ON, "{type}", chatType.translate(player, guilds));
            playerChatMap.put(player.getUniqueId(), chatType);
            return;
        }

        issuer.sendInfo(Messages.CHAT__TOGGLED_ON, "{type}", chatType.translate(player, guilds));
        playerChatMap.put(player.getUniqueId(), chatType);
    }


    public enum ChatType {
        GUILD(Messages.CHAT__TYPE_GUILD),
        ALLY(Messages.CHAT__TYPE_ALLY);

        private final Messages messageKey;

        ChatType(Messages messageKey) {
            this.messageKey = messageKey;
        }

        public String translate(final Player player, final Guilds guilds) {
            return MessageUtils.asString(player, guilds.getCommandManager(), messageKey);
        }
    }

    public Map<UUID, ChatType> getPlayerChatMap() {
        return playerChatMap;
    }
}
