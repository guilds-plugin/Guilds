package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildRemoveEvent extends GuildEvent {

    private RemoveCause cause;

    /**
     * Called when a guild is removed
     * @param player the player removing the guild
     * @param guild the guild getting removed
     * @param cause the reason for the guild being removed
     */
    public GuildRemoveEvent(Player player, Guild guild, RemoveCause cause) {
        super(player, guild);
    }

    public RemoveCause getCause() {
        return cause;
    }

    public enum RemoveCause {
        MASTER_LEFT, REMOVED,
    }
}