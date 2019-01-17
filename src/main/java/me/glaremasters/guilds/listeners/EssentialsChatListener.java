package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by GlareMasters
 * Date: 11/12/2018
 * Time: 12:27 AM
 */
@AllArgsConstructor
public class EssentialsChatListener implements Listener {

    //todo

    private GuildHandler guildHandler;
    private GuildsAPI guildsAPI;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        String message = event.getFormat();

        if (guild == null) {
            String regex = "(\\{ESSENTIALS_GUILD(?:.+)?})";

            event.setFormat(message.replaceAll(regex, ""));
            return;
        }

        message = message
                .replace("{ESSENTIALS_GUILD}", guild.getName())
                .replace("{ESSENTIALS_GUILD_PREFIX}", guild.getPrefix())
                .replace("{ESSENTIALS_GUILD_MASTER}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName()
                .replace("{ESSENTIALS_GUILD_MEMBER_COUNT}",  String.valueOf(guild.getSize())))
                .replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}", String.valueOf(guildsAPI.getGuildMembersOnline(player))
                .replace("{ESSENTIALS_GUILD_STATUS}", guild.getStatus().name())
                .replace("{ESSENTIALS_GUILD_ROLE}", guildsAPI.getGuildRole(player).getName()));

        event.setFormat(message);
    }
}
