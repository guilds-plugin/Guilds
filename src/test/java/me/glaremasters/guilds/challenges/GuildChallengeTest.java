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
package me.glaremasters.guilds.challenges;

import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the identity of a {@link GuildChallenge}.
 *
 * <p>{@code ChallengeHandler} keeps its challenges in a {@link HashSet}, and a challenge is mutated
 * throughout a war rather than being immutable. The data class generated {@code equals} and
 * {@code hashCode} hashed all seventeen constructor properties, so every one of those mutations
 * rehashed a challenge that was already filed in the set. From that point on the set could no
 * longer find it or take it out again, even though it was still physically a member: the size stayed
 * correct and iteration still returned it, which is why the reads kept looking fine while removals
 * quietly did nothing.
 *
 * <p>Identity is now the challenge id, which is generated once and never reassigned. The tests below
 * mutate a challenge the way the war commands do and then assert the set still answers for it.
 */
class GuildChallengeTest {

    @Test
    @DisplayName("a challenge stays findable in a set after its flags change")
    void survivesFlagMutation() {
        final GuildChallenge challenge = challenge();
        final Set<GuildChallenge> challenges = new HashSet<>();
        challenges.add(challenge);

        challenge.setAccepted(true);
        challenge.setJoinble(true);
        challenge.setStarted(true);

        assertTrue(challenges.contains(challenge), "a challenge that changed flags is no longer findable");

        challenge.setStarted(false);
        challenge.setCompleted(true);

        assertTrue(challenges.contains(challenge), "a challenge that finished is no longer findable");
        assertEquals(1, challenges.size());
    }

    @Test
    @DisplayName("a challenge stays removable from a set after its flags change")
    void survivesFlagMutationOnRemoval() {
        final GuildChallenge challenge = challenge();
        final Set<GuildChallenge> challenges = new HashSet<>();
        challenges.add(challenge);

        challenge.setAccepted(true);
        challenge.setJoinble(true);

        assertTrue(challenges.remove(challenge), "the removal silently did nothing");
        assertTrue(challenges.isEmpty(), "the challenge was still physically in the set");
    }

    @Test
    @DisplayName("reserving the arena after the challenge is stored does not lose it")
    void survivesArenaReservation() {
        final Arena arena = arena();
        final GuildChallenge challenge = challenge(UUID.randomUUID(), arena);
        final Set<GuildChallenge> challenges = new HashSet<>();
        challenges.add(challenge);

        // `/guild war challenge` stores the challenge first and marks the arena second.
        arena.setInUse(true);

        assertTrue(challenges.contains(challenge), "reserving the arena rehashed the challenge out of the set");

        arena.setInUse(false);

        assertTrue(challenges.remove(challenge), "releasing the arena left the challenge unreachable");
        assertTrue(challenges.isEmpty());
    }

    @Test
    @DisplayName("a challenge stays removable after the rosters and alive maps change")
    void survivesRosterMutation() {
        final GuildChallenge challenge = challenge();
        final Set<GuildChallenge> challenges = new HashSet<>();
        challenges.add(challenge);

        challenge.getDefendPlayers().add(UUID.randomUUID());
        challenge.getChallengePlayers().add(UUID.randomUUID());
        challenge.setAliveChallengers(new LinkedHashMap<>());
        challenge.getAliveChallengers().put(UUID.randomUUID(), "somewhere");
        challenge.setWinner(challenge.getChallenger());
        challenge.setLoser(challenge.getDefender());

        assertTrue(challenges.contains(challenge), "a challenge that took players is no longer findable");
        assertTrue(challenges.remove(challenge));
        assertTrue(challenges.isEmpty());
    }

    @Test
    @DisplayName("a challenge cannot be inserted twice by way of a stale reference")
    void cannotBeDuplicatedByMutation() {
        final GuildChallenge challenge = challenge();
        final Set<GuildChallenge> challenges = new HashSet<>();
        challenges.add(challenge);

        challenge.setStarted(true);
        challenges.add(challenge);

        assertEquals(1, challenges.size(), "the same challenge was stored under two buckets");
    }

    @Test
    @DisplayName("two records sharing an id are the same challenge")
    void sameIdIsTheSameChallenge() {
        final UUID id = UUID.randomUUID();
        final GuildChallenge original = challenge(id);
        final GuildChallenge reloaded = challenge(id);

        assertNotSame(original, reloaded, "the fixtures must be distinct objects");
        assertNotSame(original.getArena(), reloaded.getArena(), "the fixtures must hold distinct arenas");
        assertEquals(original, reloaded, "one id describes one challenge");
        assertEquals(original.hashCode(), reloaded.hashCode(), "equal challenges must hash alike");
    }

    @Test
    @DisplayName("challenges with different ids are never the same challenge")
    void differentIdsAreDifferentChallenges() {
        assertNotEquals(challenge(UUID.randomUUID()), challenge(UUID.randomUUID()));
    }

    private static Arena arena() {
        return new Arena(UUID.randomUUID(), "Coliseum");
    }

    private static GuildChallenge challenge() {
        return challenge(UUID.randomUUID(), arena());
    }

    private static GuildChallenge challenge(UUID id) {
        return challenge(id, arena());
    }

    private static GuildChallenge challenge(UUID id, Arena arena) {
        final Guild challenger = new Guild(UUID.randomUUID());
        final Guild defender = new Guild(UUID.randomUUID());
        return new GuildChallenge(id, System.currentTimeMillis(), challenger, defender,
                false, false, false, false, 1, 2,
                new ArrayList<>(), new ArrayList<>(), arena,
                null, null, new LinkedHashMap<>(), new LinkedHashMap<>());
    }
}
