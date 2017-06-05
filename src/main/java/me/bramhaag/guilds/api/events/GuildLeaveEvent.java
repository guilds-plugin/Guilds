package me.bramhaag.guilds.api.events;

import me.bramhaag.guilds.api.events.base.GuildEvent;
import me.bramhaag.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildLeaveEvent extends GuildEvent {

    public GuildLeaveEvent(Player player, Guild guild) {
        super(player, guild);
    }
}
