package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when money is withdrawn from a guild's bank.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property amount the amount of money withdrawn from the guild's bank
 *
 * @constructor Creates a new [GuildWithdrawMoneyEvent].
 */
class GuildWithdrawMoneyEvent(player: Player, guild: Guild, val amount: Double) : GuildEvent(player, guild)
