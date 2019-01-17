package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.handlers.Tablist;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters
 * Date: 11/12/2018
 * Time: 12:33 AM
 */
public class TablistListener implements Listener {

    private Guilds guilds;
    private GuildUtils utils;

    public TablistListener(Guilds guilds, GuildUtils utils) {
        this.guilds = guilds;
        this.utils = utils;
    }

    /**
     * This will check if the server uses Guild's TablistListener and will add a prefix to their name
     * @param event
     */
    @EventHandler
    public void onTablist(PlayerJoinEvent event) {
        Tablist tablist = new Tablist(guilds);
        Player player = event.getPlayer();
        Guild guild = utils.getGuild(player.getUniqueId());
        if (guild != null) guilds.getServer().getScheduler().scheduleAsyncDelayedTask(guilds, () -> tablist.add(player), 30L);
    }

}
