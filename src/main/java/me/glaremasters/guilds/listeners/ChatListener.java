package me.glaremasters.guilds.listeners;

import static me.glaremasters.guilds.placeholders.PlaceholdersSRV.getGuildMemberCount;
import static me.glaremasters.guilds.placeholders.PlaceholdersSRV.getGuildMembersOnline;
import static me.glaremasters.guilds.placeholders.PlaceholdersSRV.getGuildRole;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    // TODO: Clean this all up

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());

        String message = event.getFormat();

        if (guild == null) {
            String regex = "(\\{ESSENTIALS_GUILD(?:.+)?})";

            event.setFormat(message.replaceAll(regex, ""));
            return;
        }

        message = message
                .replace("{ESSENTIALS_GUILD}", guild.getName())
                .replace("{ESSENTIALS_GUILD_PREFIX}", guild.getPrefix())
                .replace("{ESSENTIALS_GUILD_MASTER}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
                .replace("{ESSENTIALS_GUILD_MEMBER_COUNT}", getGuildMemberCount(event.getPlayer()))
                .replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}", getGuildMembersOnline(event.getPlayer()))
                .replace("{ESSENTIALS_GUILD_STATUS}", guild.getStatus())
                .replace("{ESSENTIALS_GUILD_ROLE}", getGuildRole(event.getPlayer()));

        event.setFormat(message);
    }

}
