/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
