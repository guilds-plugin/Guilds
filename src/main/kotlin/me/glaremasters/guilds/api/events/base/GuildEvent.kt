package me.glaremasters.guilds.api.events.base

import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * A base class for guild related events that occur in the game.
 *
 * @property player the player who triggered the event
 * @property guild the guild that is involved in the event
 *
 * @constructor Creates a new GuildEvent with the given player and guild.
 */
open class GuildEvent(player: Player, val guild: Guild) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    /**
     * Returns the list of handlers, through which this event is passed.
     *
     * @return the list of handlers
     */
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    /**
     * Returns whether this event is cancelled or not.
     *
     * @return true if the event is cancelled, false otherwise
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Sets whether this event is cancelled or not.
     *
     * @param cancelled the value to set the event cancelled status to
     */
    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
