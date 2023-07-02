package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * An event that is called when a player joins a guild war.
 *
 * @property challenger the guild who initiated the request
 * @property defender the guild who the request is sent to
 * @property player the player who joined the war
 * @property side the side the player joined
 */
class GuildWarPlayerJoinEvent(val challenger: Guild, val defender: Guild, val player: Player, val side: String): Event() {

    /**
     * The handlers for the event.
     *
     * @return the handlers for the event
     */
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        /**
         * The static list of event handlers for this event type.
         */
        @JvmStatic
        val handlerList = HandlerList()
    }
}