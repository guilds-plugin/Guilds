package me.bramhaag.guilds.api.events;

import me.bramhaag.guilds.api.events.base.GuildEvent;
import me.bramhaag.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildRemoveAllyEvent extends GuildEvent {

    private Guild ally;

    public GuildRemoveAllyEvent(Player player, Guild guild, Guild ally) {
        super(player, guild);

        this.ally = ally;
    }

    public Guild getAlly() {
        return ally;
    }
}
