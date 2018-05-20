package me.glaremasters.guilds.handlers;

import static me.glaremasters.guilds.util.ConfigUtil.getBoolean;
import static me.glaremasters.guilds.util.ConfigUtil.getString;
import com.nametagedit.plugin.NametagEdit;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class NameTagEditHandler {

    private final Guilds plugin;

    public NameTagEditHandler(Guilds plugin) {
        this.plugin = plugin;
    }

    public void setPrefix(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (getBoolean("hooks.nametagedit")) {
            if (getBoolean("nametagedit.prefix.enabled")) {
                NametagEdit.getApi().setPrefix(player, getString("nametagedit.prefix.prefix").replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix()));
            }
        }
    }

    public void removePrefix(Player player) {
        if (getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi().setPrefix(player, "");
        }
    }

    public void setSuffix(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (getBoolean("hooks.nametagedit")) {
            if (getBoolean("nametagedit.suffix.enabled")) {
                NametagEdit.getApi().setSuffix(player, getString("nametagedit.suffix.suffix").replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix()));
            }
        }
    }

    public void removeSuffix(Player player) {
        if (getBoolean("hooks.nametagedit")) {
            if (getBoolean("nametagedit.suffix.enabled"))
            NametagEdit.getApi().setSuffix(player, "");
        }
    }


}
