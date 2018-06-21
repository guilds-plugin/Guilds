package me.glaremasters.guilds.listeners;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.handlers.WorldGuardHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by GlareMasters on 6/20/2018.
 */
public class WorldGuardListener implements Listener {

    private Guilds guilds;

    WorldGuardHandler wg = new WorldGuardHandler();

    public WorldGuardListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!guilds.getConfig().getBoolean("hooks.worldguard")) return;
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();

        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (ProtectedRegion r : wg.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location)) {
            if (r.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canPlace());
            }
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!guilds.getConfig().getBoolean("hooks.worldguard")) return;
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (ProtectedRegion r : wg.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location)) {
            if (r.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canDestroy());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!guilds.getConfig().getBoolean("hooks.worldguard")) return;
        Player player = event.getPlayer();
        Location location = event.getPlayer().getLocation();

        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (ProtectedRegion r : wg.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location)) {
            if (r.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canInteract());
            }
        }
    }

}
