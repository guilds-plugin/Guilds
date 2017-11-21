package me.glaremasters.guilds.handlers;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class TablistHandler {

    private final Main plugin;

    public TablistHandler(Main plugin) {
        this.plugin = plugin;
    }

    public void addTablist(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (plugin.getConfig().getBoolean("tablist-guilds")) {
            String name =
                    plugin.getConfig()
                            .getBoolean("tablist-use-display-name") ? player
                            .getDisplayName() : player.getName();
            player.setPlayerListName(
                    ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("tablist")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())
                                    + name));
        }
    }

    public void leaveTablist(Player player) {
        if (plugin.getConfig().getBoolean("tablist-guilds")) {
            String name =
                    plugin.getConfig().getBoolean("tablist-use-display-name")
                            ? player
                            .getDisplayName() : player.getName();
            player.setPlayerListName(
                    ChatColor.translateAlternateColorCodes('&',
                            name));
        }
    }


}
