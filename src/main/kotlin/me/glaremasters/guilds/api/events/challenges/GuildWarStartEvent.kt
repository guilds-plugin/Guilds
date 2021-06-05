package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.guild.Guild
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GuildWarStartEvent(val challenger: Guild, val defender: Guild) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}
