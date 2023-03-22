/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.WrappedState

class WorldGuardListener(private val guildHandler: GuildHandler) : Listener {

    private val wrapper = WorldGuardWrapper.getInstance()

    @EventHandler
    fun BlockPlaceEvent.onPlace() {
        val guild = guildHandler.getGuild(player) ?: return
        val region = wrapper.getRegions(block.location).firstOrNull { region -> region.id == guild.id.toString() } ?: return
        val flagToCheck = wrapper.getFlag("block-place", WrappedState::class.java)
        val blockPlace = flagToCheck.flatMap { region.getFlag(it) }

        if (blockPlace.isPresent && blockPlace.get() == WrappedState.DENY) {
            return
        }

        if (!guild.memberHasPermission(player, GuildRolePerm.PLACE)) {
            isCancelled = true
        }
    }

    @EventHandler
    fun BlockBreakEvent.onBreak() {
        val guild = guildHandler.getGuild(player) ?: return
        val region = wrapper.getRegions(block.location).firstOrNull { region -> region.id == guild.id.toString() } ?: return
        val flagToCheck = wrapper.getFlag("block-break", WrappedState::class.java)
        val blockBreak = flagToCheck.flatMap { region.getFlag(it) }

        if (blockBreak.isPresent && blockBreak.get() == WrappedState.DENY) {
            return
        }

        if (!guild.memberHasPermission(player, GuildRolePerm.DESTROY)) {
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onInteract() {

        if (useInteractedBlock() == Event.Result.DENY) {
            return
        }

        val guild = guildHandler.getGuild(player) ?: return
        val region = wrapper.getRegions(player.location).firstOrNull { region -> region.id == guild.id.toString() } ?: return

        if (!guild.memberHasPermission(player, GuildRolePerm.INTERACT)) {
            setUseInteractedBlock(Event.Result.DENY)
        }
    }
}
