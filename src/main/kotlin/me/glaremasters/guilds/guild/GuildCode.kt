package me.glaremasters.guilds.guild

import java.util.UUID
import org.bukkit.entity.Player

/**
 * A class representing a code used to join a guild.
 *
 * @param id The unique identifier of the code.
 * @param uses The number of times the code can be used.
 * @param creator The UUID of the player who created the code.
 * @param redeemers The list of UUIDs of players who have redeemed the code.
 */
class GuildCode(
    val id: String,
    var uses: Int,
    val creator: UUID,
    val redeemers: MutableList<UUID>
) {

    /**
     * Adds a player to the list of redeemers and reduces the number of uses by 1.
     *
     * @param player The player to add to the list of redeemers.
     */
    fun addRedeemer(player: Player) {
        uses -= 1
        redeemers.add(player.uniqueId)
    }
}
