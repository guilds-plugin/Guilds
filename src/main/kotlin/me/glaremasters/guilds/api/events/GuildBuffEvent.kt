package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.conf.objects.GuildBuff
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a guild's buff changes.
 *
 * @property player the player who performed the action
 * @property guild the guild that was affected
 * @property buff the new buff of the guild
 *
 * @constructor Creates a new [GuildBuffEvent].
 */
class GuildBuffEvent(player: Player, guild: Guild, var buff: GuildBuff) : GuildEvent(player, guild)
