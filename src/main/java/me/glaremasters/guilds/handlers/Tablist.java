package me.glaremasters.guilds.handlers;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static co.aikar.commands.ACFBukkitUtil.color;

/**
 * Created by GlareMasters
 * Date: 9/26/2018
 * Time: 8:10 PM
 */
public class Tablist {

    //todo

    private Guilds guilds;

    public Tablist(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles adding a prefix to the player's tablist
     *
     * @param player the player being modified
     */
    public void add(Player player) {
        Guild guild = guilds.getGuildUtils().getGuild(player.getUniqueId());
        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
        player.setPlayerListName(color(guilds.getConfig().getString("tablist")
                .replace("{guild}", guild.getName())
                .replace("{prefix}", guild.getPrefix()) + name));
    }

    /**
     * Handles removing a prefix from the player's tablist
     *
     * @param player the player being modified
     */
    public void remove(Player player) {
        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
        player.setPlayerListName(color(name));
    }
}
