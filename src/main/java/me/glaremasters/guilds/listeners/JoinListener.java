package me.glaremasters.guilds.listeners;

import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
            Message.sendMessage(player, Message.EVENT_JOIN_PENDING_INVITES
                    .replace("{number}", String.valueOf(guilds.size()), "{guilds}",
                            String.join(",", guilds)));
        }
    }

    @EventHandler
    public void onJoin2(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            Main.getInstance().getServer().getScheduler()
                    .scheduleSyncDelayedTask(Main.getInstance(), () -> {
                        player.sendMessage(ChatColor.AQUA
                                + "Guilds plugin page has been moved. If you are seeing this message. Please go to https://www.spigotmc.org/resources/guilds-premium.46962/ and read for more information.");
                        player.sendMessage(ChatColor.GREEN
                                + "You must update to the latest dev build to remove this message. Please follow the instructions in the link above to download the latest development build.");
                    }, 30L);


        }
    }

}
