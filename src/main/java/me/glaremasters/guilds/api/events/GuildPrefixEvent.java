package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildPrefixEvent extends GuildEvent {

    private String prefix;

    /**
     * Base guild event
     *  @param player player in event
     * @param guild  guild in the event
     * @param prefix
     */
    public GuildPrefixEvent(Player player, Guild guild, String prefix) {
        super(player, guild);
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
