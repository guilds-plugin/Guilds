package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 9/12/2017.
 */
public class PlayerSyncListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return;
        }


        Guild guild1 = Guild.getGuild(player.getUniqueId());
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            Main.getInstance().getGuildHandler().disable();
            Main.getInstance().getGuildHandler().enable();
            guild1.updateGuild("");
        }, 20L);

    }

}
