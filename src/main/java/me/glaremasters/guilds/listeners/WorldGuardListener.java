package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

@AllArgsConstructor
public class WorldGuardListener implements Listener {

    //todo

    private GuildHandler guildHandler;
    private WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = guildHandler.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canPlace());
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canDestroy());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getPlayer().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.canInteract());
            }
        }
    }
}
