package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player is invited to join a guild.
 *
 * @property player the player who performed the invitation
 * @property guild the guild that the invitation was sent to
 * @property invitedPlayer the player who was invited to join the guild
 *
 * @constructor Creates a new [GuildInviteEvent].
 */
class GuildInviteEvent(player: Player, guild: Guild, val invitedPlayer: Player) : GuildEvent(player, guild)
