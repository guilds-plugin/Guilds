package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 7/20/2017.
 */
public class TablistListener implements Listener {


	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		Guild guild = Guild.getGuild(player.getUniqueId());

		if (guild == null) {
			return;
		} else {
			Main.getInstance().getServer().getScheduler()
					.scheduleSyncDelayedTask(Main.getInstance(), () -> {
						String name =
								Main.getInstance().getConfig()
										.getBoolean("tablist-use-display-name") ? player
										.getDisplayName() : player.getName();
						player.setPlayerListName(
								ChatColor.translateAlternateColorCodes('&',
										Main.getInstance().getConfig().getString("tablist")
												.replace("{guild}", guild.getName())
												.replace("{prefix}", guild.getPrefix())
												+ name));
					}, 30L);

		}
	}

}
