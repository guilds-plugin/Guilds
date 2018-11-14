package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildCreateEvent extends GuildEvent {

    /**
     * Called when people create a guild
     * @param player player creating the guild
     * @param guild the guild being created
     */
    public GuildCreateEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
