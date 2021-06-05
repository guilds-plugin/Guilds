package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.guild.Guild
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GuildWarEndEvent(val challenger: Guild, val defender: Guild, val winner: Guild) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}
