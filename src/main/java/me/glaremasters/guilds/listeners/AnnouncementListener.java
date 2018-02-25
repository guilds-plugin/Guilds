package me.glaremasters.guilds.listeners;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.rayzr522.jsonmessage.JSONMessage;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 9/22/2017.
 */
public class AnnouncementListener implements Listener {

    public static Set<UUID> ALREADY_INFORMED = new HashSet<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Guilds.getInstance().getConfig().getBoolean("announcements.in-game")) {
            Guilds.getInstance().getServer().getScheduler()
                    .scheduleAsyncDelayedTask(Guilds.getInstance(), () -> {
                        if (player.isOp()) {
                            if (!ALREADY_INFORMED.contains(player.getUniqueId())) {
                                try {
                                    URL url = new URL(
                                            "https://glaremasters.me/guilds/announcements/" + Guilds
                                                    .getInstance().getDescription()
                                                    .getVersion());
                                    HttpURLConnection con = (HttpURLConnection) url
                                            .openConnection();
                                    con.setRequestProperty("User-Agent",
                                            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                                    try (InputStream in = con.getInputStream()) {
                                        String encoding = con.getContentEncoding();
                                        encoding = encoding == null ? "UTF-8" : encoding;
                                        String body = IOUtils.toString(in, encoding);
                                        JSONMessage.create(ChatColor
                                                .translateAlternateColorCodes('&',
                                                        Guilds.PREFIX + "&bAnnouncement")).tooltip(
                                                ChatColor.translateAlternateColorCodes('&', body)).openURL("https://glaremasters.me/discord").send(player);
                                        con.disconnect();
                                    }
                                } catch (Exception exception) {
                                    Bukkit.getConsoleSender()
                                            .sendMessage("Could not fetch announcements!");
                                }
                                ALREADY_INFORMED.add(player.getUniqueId());
                            }
                        }

                    }, 70L);
        }

    }

}
