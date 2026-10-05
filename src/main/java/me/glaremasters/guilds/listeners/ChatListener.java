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

        // AsyncPlayerChatEvent is fired on the chat thread, and everything the fan-out touches
        // belongs to the main thread: Guild#sendMessage resolves Bukkit.getPlayer, chatGenerator
        // calls Player#getDisplayName and PlaceholderAPI, and the spy list is an ArrayList that
        // addSpy mutates from the main thread. Iterating it from here can throw
        // ConcurrentModificationException — and because the event is already cancelled by
        // onChatLowest, that swallows the player's message with nothing logged.
        //
        // Only the delivery hops. Cancelling stays where it is, on the chat thread: the event reads
        // its cancelled state when it returns, so a cancelled event that has not been cancelled yet
        // would also be broadcast to the server. The guild lookup moves across too, since
        // getGuild reads an unsynchronised HashMap.
        Bukkit.getScheduler().runTask(guilds, () -> {
            final Guild guild = guildHandler.getGuild(player);

            if (guild == null) {
                return;
            }

            if (chatType.equals(ChatType.GUILD)) {
                guildHandler.handleGuildChat(guild, player, message);
            } else if (chatType.equals(ChatType.ALLY)) {
                guildHandler.handleAllyChat(guild, player, message);
            }
        });
    }

    @EventHandler
    public void onLogout(final PlayerQuitEvent event) {
        guildHandler.chatLogout(event.getPlayer());
    }

    /**
     * Handle toggling the chat type for a player.
     *
     * @param player   The player to toggle the chat type for.
     * @param chatType The chat type to toggle.
     */
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


    /**
     * Enum representing the different types of chat available in the game.
     */
    public enum ChatType {
        /**
         * Represents the guild chat type.
         */
        GUILD(Messages.CHAT__TYPE_GUILD),
        /**
         * Represents the ally chat type.
         */
        ALLY(Messages.CHAT__TYPE_ALLY);

        private final Messages messageKey;

        /**
         * Constructs a new instance of the `ChatType` enum.
         *
         * @param messageKey the message key used to translate the chat type
         */
        ChatType(Messages messageKey) {
            this.messageKey = messageKey;
        }

        /**
         * Translates the chat type to a string.
         *
         * @param player the player for which the chat type should be translated
         * @param guilds the guilds object used for translation
         * @return the translated string for the chat type
         */
        public String translate(final Player player, final Guilds guilds) {
            return MessageUtils.asString(player, guilds.getCommandManager(), messageKey);
        }
    }

    public Map<UUID, ChatType> getPlayerChatMap() {
        return playerChatMap;
    }
}
