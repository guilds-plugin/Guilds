package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * An event that is called when a guild war challenge request is initiated.
 *
 * @property player the player who initiated the request
 * @property challenger the guild who initiated the request
 * @property defender the guild who the request is sent to
 *
 * @constructor Creates a new GuildWarChallengeEvent with the given player, challenger and defender.
 */
class GuildWarChallengeEvent(player: Player, val challenger: Guild, val defender: Guild) : GuildEvent(player, challenger)
