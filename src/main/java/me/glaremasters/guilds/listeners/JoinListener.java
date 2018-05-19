package me.glaremasters.guilds.listeners;

import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private Guilds guilds;

    public JoinListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (Guild.getGuild(player.getUniqueId()) != null) {
            return;
        }

        List<String> guildList = new ArrayList<>();
        for (Guild guild : guilds.getGuildHandler().getGuilds().values()) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                continue;
            }

            guildList.add(guild.getName());
        }

        if (guildList.size() > 0) {
            Message.sendMessage(player, Message.EVENT_JOIN_PENDING_INVITES.replace("{number}", String.valueOf(guildList.size()), "{guilds}", String.join(",", guildList)));
        }
    }
}
