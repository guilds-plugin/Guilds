/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
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

    private GuildHandler guildHandler;
    private final WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = guild.getMember(player.getUniqueId()).getRole();

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equals(guild.getId().toString())) {
                event.setCancelled(!role.isPlace());
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = guild.getMember(player.getUniqueId()).getRole();

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.isDestroy());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getPlayer().getLocation();

        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;

        GuildRole role = guild.getMember(player.getUniqueId()).getRole();

        for (IWrappedRegion region : wrapper.getRegions(location)) {
            if (region.getId().equalsIgnoreCase(guild.getName())) {
                event.setCancelled(!role.isInteract());
            }
        }
    }
}
