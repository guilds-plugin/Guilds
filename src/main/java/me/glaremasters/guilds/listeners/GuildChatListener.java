package me.glaremasters.guilds.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by GlareMasters on 7/25/2017.
 */
public class GuildChatListener implements Listener {

    public GuildChatListener(Guilds guilds) {
        this.guilds = guilds;
    }

    private Guilds guilds;

    public static final Set<UUID> GUILD_CHAT_PLAYERS = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return;
        }

        if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {
            event.getRecipients().removeIf(r -> guild.getMember(r.getUniqueId()) == null);
            for (Player p2 : event.getRecipients()) {
                if (player.hasPermission("guilds.chat.color")) {
                    p2.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            (guilds.getConfig().getString("guild-chat-format"))
                                    .replace("{role}", GuildRole
                                            .getRole(
                                                    guild.getMember(player.getUniqueId()).getRole())
                                            .getName())
                                    .replace("{player}", event.getPlayer().getName())
                                    .replace("{message}", event.getMessage())));
                } else {
                    p2.sendMessage(guilds.getConfig().getString("guild-chat-format")
                            .replace("{role}", GuildRole
                                    .getRole(guild.getMember(player.getUniqueId()).getRole())
                                    .getName()));
                }
            }

            event.setCancelled(true);
        }

    }
}
