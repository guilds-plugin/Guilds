package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.guild.Guild
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * An event that is called when a guild war starts.
 *
 * @property challenger the guild who initiated the war
 * @property defender the guild who was challenged
 *
 * @constructor Creates a new GuildWarStartEvent with the given challenger and defender.
 */
class GuildWarStartEvent(val challenger: Guild, val defender: Guild) : Event() {

    /**
     * Gets a list of all registered handlers for this event.
     *
     * @return the handlers list
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
