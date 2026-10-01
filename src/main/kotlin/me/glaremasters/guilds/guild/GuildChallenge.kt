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

import com.google.gson.annotations.JsonAdapter
import java.util.UUID
import me.glaremasters.guilds.arena.Arena
import me.glaremasters.guilds.challenges.adapters.WarArenaChallengeAdapter
import me.glaremasters.guilds.challenges.adapters.WarGuildChallengeAdapter

data class GuildChallenge(
    val id: UUID,
    val initiateTime: Long,
    @JsonAdapter(WarGuildChallengeAdapter::class) var challenger: Guild,
    @JsonAdapter(WarGuildChallengeAdapter::class) var defender: Guild,
    @Transient var isAccepted: Boolean,
    @Transient var isJoinble: Boolean,
    @Transient var isStarted: Boolean,
    var isCompleted: Boolean,
    @Transient val minPlayersPerSide: Int,
    @Transient val maxPlayersPerSide: Int,
    val challengePlayers: MutableList<UUID>,
    val defendPlayers: MutableList<UUID>,
    @JsonAdapter(WarArenaChallengeAdapter::class) val arena: Arena,
    @JsonAdapter(WarGuildChallengeAdapter::class) var winner: Guild?,
    @JsonAdapter(WarGuildChallengeAdapter::class) var loser: Guild?,
    @Transient var aliveChallengers: MutableMap<UUID, String>?,
    @Transient var aliveDefenders: MutableMap<UUID, String>?
) {

    /**
     * Identity is the challenge id and nothing else.
     *
     * A challenge is mutated constantly while a war runs: the joinable and started flags flip, the
     * rosters grow, the alive maps are swapped and trimmed, a winner and loser are recorded, and the
     * arena it holds has its own `inUse` flag toggled. The data class generated `equals`/`hashCode`
     * hash all of those fields, so mutating a challenge that was already stored changed the hash it
     * was filed under. Once that happened the challenge could no longer be found or removed through
     * a hash based lookup, even though it was still physically in the collection: a war that was
     * denied, or abandoned while players were joining, left its challenge behind and the two guilds
     * could not challenge each other again for the rest of the session.
     *
     * The id is generated once and never reassigned, so it is the only field that can carry identity.
     */
    override fun equals(other: Any?): Boolean = this === other || (other is GuildChallenge && id == other.id)

    /**
     * Must stay in step with [equals]. Only the immutable id contributes.
     *
     * Deliberately not defensive about a missing id. A challenge record whose id was lost cannot be
     * written, looked up or removed by id anyway, and treating every such record as one and the same
     * challenge would quietly drop one of them. Failing here is the louder, more useful outcome.
     */
    override fun hashCode(): Int = id.hashCode()
}
