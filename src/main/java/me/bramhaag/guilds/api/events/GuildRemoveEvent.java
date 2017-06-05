package me.bramhaag.guilds.api.events;

import me.bramhaag.guilds.api.events.base.GuildEvent;
import me.bramhaag.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildRemoveEvent extends GuildEvent {

    private RemoveCause cause;

    public GuildRemoveEvent(Player player, Guild guild, RemoveCause cause) {
        super(player, guild);
    }

    public RemoveCause getCause() {
        return cause;
    }

    public enum RemoveCause {
        MASTER_LEFT,
        REMOVED
    }
}
