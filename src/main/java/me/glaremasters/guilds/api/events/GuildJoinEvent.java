package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildJoinEvent extends GuildEvent {

    /**
     * Called when a player joins a guild
     * @param player the player joining a guild
     * @param guild the guild the player will be joining
     */
    public GuildJoinEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
