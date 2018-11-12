package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildCreateEvent extends GuildEvent {

    public GuildCreateEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
