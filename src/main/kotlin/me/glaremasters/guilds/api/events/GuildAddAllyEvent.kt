package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild adds an ally.
 *
 * @property player the player who performed the action
 * @property guild the guild that added the ally
 * @property ally the guild that was added as an ally
 *
 * @constructor Creates a new [GuildAddAllyEvent].
 */
class GuildAddAllyEvent(player: Player, guild: Guild, val ally: Guild) : GuildEvent(player, guild)
