package me.glaremasters.guilds.listeners;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import me.glaremasters.guilds.Main;
import org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 9/22/2017.
 */
public class AnnouncementListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Main.getInstance().getServer().getScheduler()
                .scheduleSyncDelayedTask(Main.getInstance(), () -> {
                    if (Main.getInstance().getConfig().getBoolean("announcements.in-game")) {
                        if (player.isOp()) {
                            try {
                                URL url = new URL("https://glaremasters.me/guilds/announcements/1.9.0/");
                                URLConnection con = url.openConnection();
                                con.setRequestProperty("User-Agent",
                                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                                InputStream in = con.getInputStream();
                                String encoding = con.getContentEncoding();
                                encoding = encoding == null ? "UTF-8" : encoding;
                                String body = IOUtils.toString(in, encoding);
                                player.sendMessage(body);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                }, 30L);


    }

}
