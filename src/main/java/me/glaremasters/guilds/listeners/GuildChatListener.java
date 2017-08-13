package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by GlareMasters on 7/25/2017.
 */
public class GuildChatListener implements Listener {

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
      event.setFormat(ChatColor.translateAlternateColorCodes('&',
          (Main.getInstance().getConfig().getString("guild-chat-format"))
              .replace("{role}", GuildRole
                  .getRole(guild.getMember(player.getUniqueId()).getRole()).getName())));
    }

  }
}
