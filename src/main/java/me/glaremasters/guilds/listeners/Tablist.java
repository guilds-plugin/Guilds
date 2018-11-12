package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters
 * Date: 11/12/2018
 * Time: 12:33 AM
 */
public class Tablist implements Listener {

    private Guilds guilds;

    public Tablist(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * This will check if the server uses Guild's Tablist and will add a prefix to their name
     * @param event
     */
    @EventHandler
    public void onTablist(PlayerJoinEvent event) {
        me.glaremasters.guilds.handlers.Tablist tablist = new me.glaremasters.guilds.handlers.Tablist(guilds);
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild != null) guilds.getServer().getScheduler().scheduleAsyncDelayedTask(guilds, () -> tablist.add(player), 30L);
    }

}
