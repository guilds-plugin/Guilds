package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildLeaveEvent extends GuildEvent {

    /**
     * Called a when a player leaves the guild
     * @param player the player leaving the guild
     * @param guild the guild the player was leaving
     */
    public GuildLeaveEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
