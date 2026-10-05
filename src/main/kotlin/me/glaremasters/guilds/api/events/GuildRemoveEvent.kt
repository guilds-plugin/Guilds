package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player

/**
 * Class representing an event that occurs when a player leaves a guild.
 *
 * @property player the player who left the guild
 * @property guild the guild the player left
 * @property cause the reason for the player leaving the guild
 *
 * @constructor Creates a new [GuildRemoveEvent].
 */
class GuildRemoveEvent(player: Player, guild: Guild, val cause: Cause) : GuildEvent(player, guild) {

    /**
     * Enumeration class representing the possible reasons a player may leave a guild.
     */
    enum class Cause {
        /** The master of the guild has left. */
        MASTER_LEFT,
        /** The player was deleted. */
        PLAYER_DELETED,
        /** The player was deleted by an administrator. */
        ADMIN_DELETED
    }
}
