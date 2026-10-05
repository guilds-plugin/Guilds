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

class WorldGuardListener(private val guildHandler: GuildHandler, private val wrapper: WorldGuardWrapper) : Listener {

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
        val block = clickedBlock ?: return
        val region = wrapper.getRegions(block.location).firstOrNull { region -> region.id == guild.id.toString() } ?: return

        if (!guild.memberHasPermission(player, GuildRolePerm.INTERACT)) {
            setUseInteractedBlock(Event.Result.DENY)
        }
    }
}
