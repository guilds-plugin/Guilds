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

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the add, find and remove behaviour of a tracked challenge.
 *
 * <p>Every war path mutates the challenge it is holding and then, on the way out, asks the handler
 * to drop it: {@code /guild war deny} flips the arena flag and then removes, and the join and ready
 * timers set the joinable flag before removing. With the challenge hashing its own mutable state the
 * removals returned false and left the challenge in the handler. Because a leftover challenge is not
 * marked completed, {@link ChallengeHandler#getChallenge(Guild)} kept returning it, so both guilds
 * were told they were already challenging for the rest of the session.
 *
 * <p>The arena is released on those same paths, so the leak showed up as a war that could never be
 * started again rather than as an exhausted arena. These tests replay the ordering and check both
 * halves.
 */
class ChallengeHandlerTest {

    @Test
    @DisplayName("a denied challenge is really removed and no longer blocks a new challenge")
    void deniedChallengeIsRemoved() {
        final ChallengeHandler handler = handler();
        final Guild challenger = new Guild(UUID.randomUUID());
        final Guild defender = new Guild(UUID.randomUUID());
        final Arena arena = arena();
        final GuildChallenge challenge = challenge(challenger, defender, arena);

        handler.addChallenge(challenge);
        assertEquals(challenge, handler.getChallenge(defender), "the pending challenge is not visible");

        // `/guild war challenge` reserves the arena right after the challenge is stored.
        arena.setInUse(true);
        // The defender accepted, players signed up, and then the challenge was denied.
        challenge.setAccepted(true);
        challenge.setJoinble(true);
        challenge.getDefendPlayers().add(UUID.randomUUID());
        // `/guild war deny` releases the arena and then removes the challenge.
        arena.setInUse(false);
        handler.removeChallenge(challenge);

        assertNull(handler.getChallenge(challenge.getId()), "the denied challenge was still tracked");
        assertNull(handler.getChallenge(defender), "the denied challenge still blocks a new challenge");
    }

    @Test
    @DisplayName("a challenge abandoned after players joined is really removed")
    void abandonedChallengeIsRemoved() {
        final ChallengeHandler handler = handler();
        final Arena arena = arena();
        final GuildChallenge challenge = challenge(new Guild(UUID.randomUUID()), new Guild(UUID.randomUUID()), arena);

        handler.addChallenge(challenge);

        // The challenge was accepted, players joined, then the ready timer gave up.
        challenge.setAccepted(true);
        challenge.setJoinble(true);
        challenge.getChallengePlayers().add(UUID.randomUUID());
        challenge.getDefendPlayers().add(UUID.randomUUID());
        challenge.setAliveChallengers(new LinkedHashMap<>());
        challenge.setAliveDefenders(new LinkedHashMap<>());
        arena.setInUse(false);
        handler.removeChallenge(challenge);

        assertNull(handler.getChallenge(challenge.getId()), "the abandoned challenge was still tracked");
    }

    @Test
    @DisplayName("a finished challenge is kept for its history but no longer offered as pending")
    void completedChallengeIsKept() {
        final ChallengeHandler handler = handler();
        final Guild challenger = new Guild(UUID.randomUUID());
        final Guild defender = new Guild(UUID.randomUUID());
        final GuildChallenge challenge = challenge(challenger, defender, arena());

        handler.addChallenge(challenge);
        challenge.setCompleted(true);
        challenge.setWinner(challenger);
        challenge.setLoser(defender);

        assertTrue(handler.getChallenges().contains(challenge), "a completed challenge is no longer tracked");
        assertNull(handler.getChallenge(defender), "a completed challenge is still offered as pending");
    }

    @Test
    @DisplayName("the tracked challenges cannot be modified through the getter")
    void trackedChallengesAreReadOnly() {
        final ChallengeHandler handler = handler();
        final GuildChallenge challenge = challenge(new Guild(UUID.randomUUID()), new Guild(UUID.randomUUID()), arena());
        handler.addChallenge(challenge);

        assertThrows(UnsupportedOperationException.class, () -> handler.getChallenges().clear());
        assertThrows(UnsupportedOperationException.class, () -> handler.getChallenges().remove(challenge));
        assertFalse(handler.getChallenges().isEmpty(), "the collection is still mutable through the getter");
    }

    @Test
    @DisplayName("removing by id still finds a challenge that has been mutated")
    void removalByIdStillWorks() {
        final ChallengeHandler handler = handler();
        final GuildChallenge challenge = challenge(new Guild(UUID.randomUUID()), new Guild(UUID.randomUUID()), arena());

        handler.addChallenge(challenge);
        challenge.setStarted(true);
        handler.removeChallenge(challenge.getId());

        assertNull(handler.getChallenge(challenge.getId()));
    }

    private static ChallengeHandler handler() {
        // Only loadChallenges and saveData reach for the plugin, and neither is exercised here.
        return new ChallengeHandler(Mockito.mock(Guilds.class));
    }

    private static Arena arena() {
        return new Arena(UUID.randomUUID(), "Coliseum");
    }

    private static GuildChallenge challenge(Guild challenger, Guild defender, Arena arena) {
        return new GuildChallenge(UUID.randomUUID(), System.currentTimeMillis(), challenger, defender,
                false, false, false, false, 1, 2,
                new ArrayList<>(), new ArrayList<>(), arena,
                null, null, new LinkedHashMap<>(), new LinkedHashMap<>());
    }
}
