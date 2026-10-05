package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player joins a guild.
 *
 * @property player the player who joined the guild
 * @property guild the guild that the player joined
 *
 * @constructor Creates a new [GuildJoinEvent].
 */
class GuildJoinEvent(player: Player, guild: Guild) : GuildEvent(player, guild)
