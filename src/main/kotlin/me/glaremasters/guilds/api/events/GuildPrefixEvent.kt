package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild's prefix changes.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property prefix the new prefix of the guild
 *
 * @constructor Creates a new [GuildPrefixEvent].
 */
class GuildPrefixEvent(player: Player, guild: Guild, val prefix: String) : GuildEvent(player, guild)
