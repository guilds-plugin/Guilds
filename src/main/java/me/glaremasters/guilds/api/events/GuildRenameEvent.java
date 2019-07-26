package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildRenameEvent extends GuildEvent {
    /**
     * Base guild event
     *
     * @param player player in event
     * @param guild  guild in the event
     */
    public GuildRenameEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
