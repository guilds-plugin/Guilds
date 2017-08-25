package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildAddAllyEvent extends GuildEvent {

	private Guild ally;

	public GuildAddAllyEvent(Player player, Guild guild, Guild ally) {
		super(player, guild);

		this.ally = ally;
	}

	public Guild getAlly() {
		return ally;
	}
}
