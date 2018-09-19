package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.Serialization;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.glaremasters.guilds.utils.ConfigUtils.getBoolean;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:31 PM
 */
public class Players implements Listener {

    private Guilds guilds;

    private Set<UUID> ALREADY_INFORMED = new HashSet<>();

    public Players(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Guild / Ally damage handler
     * @param event handles when damage is done between two players that might be in the same guild or are allies
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        Guild playerGuild = Guild.getGuild(player.getUniqueId());
        Guild damagerGuild = Guild.getGuild(damager.getUniqueId());
        if (playerGuild == null || damagerGuild == null) return;
        if (playerGuild.equals(damagerGuild)) event.setCancelled(!getBoolean("allow-guild-damage"));
        if (Guild.areAllies(player.getUniqueId(), damager.getUniqueId())) event.setCancelled(!getBoolean("allow-ally-damage"));
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase(guild.getName() + "'s Guild Vault")) return;
        guild.updateInventory(Serialization.serializeInventory(event.getInventory()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        guilds.getServer().getScheduler().scheduleSyncDelayedTask(guilds, () -> {
            if (player.isOnline()) {
                if (!ALREADY_INFORMED.contains(player.getUniqueId())) {
                    JSONMessage.create("Announcements").tooltip(guilds.getAnnouncements()).openURL(guilds.getDescription().getWebsite()).send(player);
                    ALREADY_INFORMED.add(player.getUniqueId());
                }
            }
        }, 70L);
    }
}
