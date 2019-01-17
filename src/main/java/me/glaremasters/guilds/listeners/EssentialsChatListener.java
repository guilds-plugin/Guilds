package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
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
public class EssentialsChatListener implements Listener {

    private Guilds guilds;
    private GuildUtils utils;

    public EssentialsChatListener(Guilds guilds, GuildUtils utils) {
        this.guilds = guilds;
        this.utils = utils;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = utils.getGuild((player.getUniqueId()));

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
                .replace("{ESSENTIALS_GUILD_MEMBER_COUNT}",  guilds.getApi().getGuildMemberCount(event.getPlayer()))
                .replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}", guilds.getApi().getGuildMembersOnline(event.getPlayer()))
                .replace("{ESSENTIALS_GUILD_STATUS}", guild.getStatus())
                .replace("{ESSENTIALS_GUILD_ROLE}", guilds.getApi().getGuildRole(event.getPlayer()));

        event.setFormat(message);
    }
}
