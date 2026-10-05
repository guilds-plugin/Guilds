package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when the master of a guild is transferred to another player.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property newMaster the new master of the guild
 *
 * @constructor Creates a new [GuildTransferEvent].
 */
class GuildTransferEvent(player: Player, guild: Guild, val newMaster: OfflinePlayer) : GuildEvent(player, guild)
