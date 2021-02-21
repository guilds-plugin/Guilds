package me.glaremasters.guilds.listeners;

import co.aikar.commands.BukkitCommandIssuer;
import com.google.common.collect.Maps;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
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
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final ChatType chatType = playerChatMap.get(player.getUniqueId());
        final String message = event.getMessage();

        if (chatType == null) {
            return;
        }

        event.setCancelled(true);

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
                issuer.sendInfo(Messages.CHAT__TOGGLED_OFF, "{type}", chatType.name());
                return;
            }

            issuer.sendInfo(Messages.CHAT__TOGGLED_OFF, "{type}", chatType.name());
            issuer.sendInfo(Messages.CHAT__TOGGLED_ON, "{type}", chatType.name());
            playerChatMap.put(player.getUniqueId(), chatType);
            return;
        }

        issuer.sendInfo(Messages.CHAT__TOGGLED_ON, "{type}", chatType.name());
        playerChatMap.put(player.getUniqueId(), chatType);
    }


    public enum ChatType {
        GUILD,
        ALLY
    }

    public Map<UUID, ChatType> getPlayerChatMap() {
        return playerChatMap;
    }
}
