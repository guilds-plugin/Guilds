package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildAddAllyEvent extends GuildEvent {

    private Guild ally;

    /**
     * This event takes place when two guilds ally each other
     * @param player player who accepted
     * @param guild guild one
     * @param ally guild two
     */
    public GuildAddAllyEvent(Player player, Guild guild, Guild ally) {
        super(player, guild);

        this.ally = ally;
    }

    public Guild getAlly() {
        return ally;
    }
}