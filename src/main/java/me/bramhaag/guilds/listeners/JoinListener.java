package me.bramhaag.guilds.listeners;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.getName().equalsIgnoreCase("blockslayer22")) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "[Guilds] " + ChatColor.WHITE + "My creator, " + ChatColor.GREEN + "blockslayer22 " + ChatColor.WHITE + "has joined the server.");
        }
    }

}

