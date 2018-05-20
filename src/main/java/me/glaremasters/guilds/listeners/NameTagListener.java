package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.handlers.NameTagEditHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 8/21/2017.
 */
public class NameTagListener implements Listener {

    NameTagEditHandler NTEHandler = new NameTagEditHandler(Guilds.getInstance());

    @EventHandler
    public void nameTagJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild != null) {
            NTEHandler.setPrefix(player);
            NTEHandler.setSuffix(player);
        }
    }


}



