package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild is created.
 *
 * @property player the player who performed the action
 * @property guild the guild that was created
 *
 * @constructor Creates a new [GuildCreateEvent].
 */
class GuildCreateEvent(player: Player, guild: Guild) : GuildEvent(player, guild)
