/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

package me.glaremasters.guilds.listeners

import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildRolePerm
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.codemc.worldguardwrapper.WorldGuardWrapper

class WorldGuardListener(private val guildHandler: GuildHandler) : Listener {

    private val wrapper = WorldGuardWrapper.getInstance()

    @EventHandler
    fun BlockPlaceEvent.onPlace() {
        val player = player
        val loc = blockPlaced.location
        val guild = guildHandler.getGuild(player) ?: return

        for (region in wrapper.getRegions(loc)) {
            if (region.id == guild.id.toString()) {
                isCancelled = !guild.memberHasPermission(player, GuildRolePerm.PLACE)
            }
        }
    }

    @EventHandler
    fun BlockBreakEvent.onBreak() {
        val player = player
        val loc = block.location
        val guild = guildHandler.getGuild(player) ?: return

        for (region in wrapper.getRegions(loc)) {
            if (region.id == guild.id.toString()) {
                isCancelled = !guild.memberHasPermission(player, GuildRolePerm.DESTROY)
            }
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onInteract() {
        val player = player
        val loc = player.location
        val guild = guildHandler.getGuild(player) ?: return

        for (region in wrapper.getRegions(loc)) {
            if (region.id == guild.id.toString()) {
                isCancelled = !guild.memberHasPermission(player, GuildRolePerm.INTERACT)
            }
        }
    }
}