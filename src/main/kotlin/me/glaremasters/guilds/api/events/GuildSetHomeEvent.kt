package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Location
import org.bukkit.entity.Player

class GuildSetHomeEvent(player: Player, guild: Guild, val location: Location) : GuildEvent(player, guild)
