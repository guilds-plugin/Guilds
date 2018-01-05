package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 1/4/2018.
 */
public class TierJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return;
        }

        if (player.hasPermission("guilds.tier." + guild.getTier())) {
            return;
        }

        Guilds.getPermissions().playerAdd(null, player, "guilds.tier." + guild.getTier());

    }

}
