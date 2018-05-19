package me.glaremasters.guilds.handlers;

import static me.glaremasters.guilds.util.ColorUtil.color;
import static me.glaremasters.guilds.util.ConfigUtil.getBoolean;
import static me.glaremasters.guilds.util.ConfigUtil.getString;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class TablistHandler {

    private final Guilds plugin;

    public TablistHandler(Guilds plugin) {
        this.plugin = plugin;
    }

    public void addTablist(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (getBoolean("tablist-guilds")) {
            String name = getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
            player.setPlayerListName(getString("tablist").replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix()) + name);
        }
    }

    public void leaveTablist(Player player) {
        if (getBoolean("tablist-guilds")) {
            String name = getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
            player.setPlayerListName(color(name));
        }
    }


}
