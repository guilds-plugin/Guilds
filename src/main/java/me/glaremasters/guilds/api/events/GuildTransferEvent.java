package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildTransferEvent extends GuildEvent {

    private Player newMaster;

    /**
     * Base guild event
     *  @param player player in event
     * @param guild  guild in the event
     * @param newMaster
     */
    public GuildTransferEvent(Player player, Guild guild, Player newMaster) {
        super(player, guild);
        this.newMaster = newMaster;
    }

    public Player getNewMaster() {
        return newMaster;
    }
}
