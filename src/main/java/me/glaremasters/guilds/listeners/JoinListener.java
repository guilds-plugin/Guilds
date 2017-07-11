package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Main.getInstance().getScoreboardHandler().show(player);

        if (Guild.getGuild(player.getUniqueId()) != null) {
            return;
        }

        List<String> guilds = new ArrayList<>();
        for (Guild guild : Main.getInstance().getGuildHandler().getGuilds().values()) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                continue;
            }

            guilds.add(guild.getName());
        }

        if (guilds.size() > 0) {
            Message.sendMessage(player, Message.EVENT_JOIN_PENDING_INVITES.replace("{number}", String.valueOf(guilds.size()), "{guilds}", String.join(",", guilds)));
        }
    }
}

