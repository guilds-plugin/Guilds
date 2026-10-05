package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * An event that is called when a guild war accept request is accepted by the defending guild.
 *
 * @property player the player who accepted the request
 * @property challenger the guild who initiated the request
 * @property defender the guild who accepted the request
 *
 * @constructor Creates a new GuildWarAcceptEvent with the given player, challenger and defender.
 */
class GuildWarAcceptEvent(player: Player, val challenger: Guild, val defender: Guild) : GuildEvent(player, defender)
