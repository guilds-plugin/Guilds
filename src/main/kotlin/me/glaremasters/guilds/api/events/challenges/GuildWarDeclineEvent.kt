package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * An event that is called when a guild war challenge request is declined by the defending guild.
 *
 * @property player the player who declined the request
 * @property challenger the guild who initiated the request
 * @property defender the guild who declined the request
 *
 * @constructor Creates a new GuildWarDeclineEvent with the given player, challenger and defender.
 */
class GuildWarDeclineEvent(player: Player, val challenger: Guild, val defender: Guild) : GuildEvent(player, defender)
