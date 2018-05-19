package me.glaremasters.guilds.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters on 9/22/2017.
 */
public class AnnouncementListener implements Listener {

    private Set<UUID> ALREADY_INFORMED = new HashSet<>();

    private Guilds guilds;

    public AnnouncementListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Guilds.getInstance().getConfig().getBoolean("announcements.in-game")) {
            Guilds.getInstance().getServer().getScheduler()
                    .scheduleAsyncDelayedTask(Guilds.getInstance(), () -> {
                        if (player.isOp()) {
                            if (!ALREADY_INFORMED.contains(player.getUniqueId())) {
                                JSONMessage.create(Guilds.PREFIX + "Announcements").tooltip(guilds.getAnnouncements()).openURL(guilds.getDescription().getWebsite()).send(player);
                                ALREADY_INFORMED.add(player.getUniqueId());
                            }
                        }
                    }, 70L);
        }

    }

}
