package me.glaremasters.guilds.listeners;

import com.nametagedit.plugin.NametagEdit;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 8/21/2017.
 */
public class NameTagListener implements Listener {

	@EventHandler
	public void nameTagJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Guild guild = Guild.getGuild(player.getUniqueId());

		if (guild == null) {
			return;
		} else {
			NametagEdit.getApi()
					.setPrefix(player, ChatColor.translateAlternateColorCodes('&',
							Main.getInstance().getConfig()
									.getString("nametagedit.name")
									.replace("{guild}", guild.getName())
									.replace("{prefix}", guild.getPrefix())));
		}
	}


}



