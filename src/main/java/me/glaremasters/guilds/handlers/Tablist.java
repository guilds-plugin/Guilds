package me.glaremasters.guilds.handlers;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 9/26/2018
 * Time: 8:10 PM
 */
public class Tablist {

    private Guilds guilds;

    public Tablist(Guilds guilds) {
        this.guilds = guilds;
    }

    public void add(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guilds.getConfig().getBoolean("tablist-guilds")) {
            String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
            player.setPlayerListName(color(guilds.getConfig().getString("tablist")
                    .replace("{guild}", guild.getName())
                    .replace("{prefix}", guild.getPrefix()) + name));
        }
    }

    public void remove(Player player) {
        if (guilds.getConfig().getBoolean("tablist-guilds")) {
            String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
            player.setPlayerListName(color(name));
        }
    }
}
