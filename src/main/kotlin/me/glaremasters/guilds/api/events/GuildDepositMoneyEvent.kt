package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player deposits money into a guild.
 *
 * @property player the player who performed the deposit
 * @property guild the guild that received the deposit
 * @property amount the amount of money deposited
 *
 * @constructor Creates a new [GuildDepositMoneyEvent].
 */
class GuildDepositMoneyEvent(player: Player, guild: Guild, val amount: Double) : GuildEvent(player, guild)
