package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildTier
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild is upgraded to a new tier.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property tier the new tier of the guild
 *
 * @constructor Creates a new [GuildUpgradeEvent].
 */
class GuildUpgradeEvent(player: Player, guild: Guild, val tier: GuildTier) : GuildEvent(player, guild)
