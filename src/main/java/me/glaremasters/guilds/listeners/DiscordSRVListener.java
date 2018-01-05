package me.glaremasters.guilds.listeners;

import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by GlareMasters on 1/4/2018.
 */
public class DiscordSRVListener implements Listener {

    @EventHandler
    public void discordChatEvent(GameChatMessagePreProcessEvent event) {
        if (GuildChatListener.GUILD_CHAT_PLAYERS.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
