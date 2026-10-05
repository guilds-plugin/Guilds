package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild's name changes.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property name the new name of the guild
 *
 * @constructor Creates a new [GuildRenameEvent].
 */
class GuildRenameEvent(player: Player, guild: Guild, val name: String) : GuildEvent(player, guild)
