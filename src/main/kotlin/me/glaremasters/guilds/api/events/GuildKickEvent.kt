package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player is kicked from a guild.
 *
 * @property player the player who performed the kick
 * @property guild the guild the player was kicked from
 * @property kicked the player who was kicked
 * @property cause the reason for the player being kicked
 *
 * @constructor Creates a new [GuildKickEvent].
 */
class GuildKickEvent(player: Player, guild: Guild, val kicked: OfflinePlayer, val cause: Cause) : GuildEvent(player, guild) {

    /**
     * Enumeration class representing the possible reasons a player may be kicked from a guild.
     */
    enum class Cause {
        /** The player was kicked by another player. */
        PLAYER_KICKED,
        /** The player was kicked by an administrator. */
        ADMIN_KICKED
    }
}
