package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;


/**
 * Created by GlareMasters on 8/29/2017.
 */
public class TicketListener implements Listener {

    @EventHandler
    public void upgradeTicket(PlayerInteractEvent event) {
        FileConfiguration config = Main.getInstance().getConfig();
        String ticketName = ChatColor
                .translateAlternateColorCodes('&', config.getString("upgrade-ticket.name"));
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
    }

}
