package me.glaremasters.guilds.handlers;

import com.nametagedit.plugin.NametagEdit;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class NameTagEditHandler {

    private final Main plugin;

    public NameTagEditHandler(Main plugin) {
        this.plugin = plugin;
    }

    public void setTag(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (plugin.getConfig().getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi()
                    .setPrefix(player, ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig()
                                    .getString("nametagedit.name")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())));
        }
    }

    public void removeTag(Player player) {
        if (plugin.getConfig().getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi()
                    .setPrefix(player, "");
        }
    }


}
