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
)
