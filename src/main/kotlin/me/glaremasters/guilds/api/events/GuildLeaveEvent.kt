package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player leaves a guild.
 *
 * @property player the player who left the guild
 * @property guild the guild that the player left
 *
 * @constructor Creates a new [GuildLeaveEvent].
 */
class GuildLeaveEvent(player: Player, guild: Guild) : GuildEvent(player, guild)
