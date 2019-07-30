package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildWithdrawMoneyEvent extends GuildEvent {

    private final double amount;


    /**
     * Base guild event
     *  @param player player in event
     * @param guild  guild in the event
     * @param amount the amount to withdraw
     */
    public GuildWithdrawMoneyEvent(Player player, Guild guild, double amount) {
        super(player, guild);
        this.amount = amount;
    }

    public double getAmount() {
        return this.amount;
    }
}
