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

    public void setTag(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi().setPrefix(player, getString("nametagedit.name").replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix()));
        }
    }

    public void removeTag(Player player) {
        if (getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi().setPrefix(player, "");
        }
    }


}
