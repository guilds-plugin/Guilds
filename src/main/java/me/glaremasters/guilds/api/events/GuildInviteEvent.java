package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildInviteEvent extends GuildEvent {

    private Player invitedPlayer;

    /**
     * Called when a player gets invited to a guild
     * @param player the player inviting other to guild
     * @param guild the guild player will be joining
     * @param invitedPlayer the player being invited
     */
    public GuildInviteEvent(Player player, Guild guild, Player invitedPlayer) {
        super(player, guild);

        this.invitedPlayer = invitedPlayer;
    }

    public Player getInvitedPlayer() {
        return invitedPlayer;
    }
}