package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild's home location is set.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property location the new home location of the guild
 *
 * @constructor Creates a new [GuildSetHomeEvent].
 */
class GuildSetHomeEvent(player: Player, guild: Guild, val location: Location) : GuildEvent(player, guild)
