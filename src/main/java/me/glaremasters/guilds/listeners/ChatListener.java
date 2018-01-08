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
        if (guild == null) {
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_PREFIX}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MASTER}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBER_COUNT}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_STATUS}", ""));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_ROLE}", ""));
        } else {
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD}", guild.getName()));
            event.setFormat(
                    event.getFormat().replace("{ESSENTIALS_GUILD_PREFIX}", guild.getPrefix()));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MASTER}",
                    Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBER_COUNT}",
                    getGuildMemberCount(event.getPlayer())));
            event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}",
                    getGuildMembersOnline(event.getPlayer())));
            event.setFormat(
                    event.getFormat().replace("{ESSENTIALS_GUILD_STATUS}", guild.getStatus()));
            event.setFormat(
                    event.getFormat()
                            .replace("{ESSENTIALS_GUILD_ROLE}", getGuildRole(event.getPlayer())));

        }
    }
}
