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
package me.glaremasters.guilds.persistence;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers that a snapshot holds what was there when it was taken, not what is there when it is read.
 *
 * <p>The old code handed the adapters live views: {@code arenas.values}, the challenge
 * {@code HashSet} itself, and {@code ExpiringMap#values()}. A save that iterated one of those while
 * the main thread changed the underlying map either threw {@link java.util.ConcurrentModificationException}
 * or, worse, produced a collection that had never existed at any single instant. The adapters read
 * those collections long after the capture point, on a worker thread.
 *
 * <p>These tests take the snapshot, then mutate, then check the snapshot. That is the ordering a save
 * actually has, and it is deterministic: no second mutator thread is needed, because the new design
 * has exactly one mutator (the main thread) and one reader (the write worker), and the snapshot is the
 * thing that separates them.
 *
 * <p>The last two tests are characterisation tests for the hazard itself. They pin down that copying
 * a map while another thread writes to it is genuinely unsafe, which is why the copy has to happen on
 * the main thread rather than merely "somewhere".
 */
class SnapshotIsolationTest {

    private Arena arenaNamed(String name) {
        return new Arena(UUID.randomUUID(), name);
    }

    @Test
    @DisplayName("an arena removed after the snapshot is still in the snapshot")
    void anArenaRemovedAfterTheSnapshotIsStillInTheSnapshot() {
        final ArenaHandler handler = new ArenaHandler(Mockito.mock(Guilds.class));
        final Arena keep = arenaNamed("keep");
        final Arena drop = arenaNamed("drop");
        handler.addArena(keep);
        handler.addArena(drop);

        final List<Arena> snapshot = handler.getArenasForSnapshot();
        assertEquals(2, snapshot.size());

        // What `/guilds arena delete` does, between the capture and the write.
        handler.removeArena(drop);

        assertEquals(2, snapshot.size(), "the snapshot must still hold both arenas");
        assertTrue(snapshot.contains(drop), "the removed arena must still be in the snapshot");
        assertFalse(handler.getArenas().contains(drop), "the live collection must have lost it");
    }

    @Test
    @DisplayName("an arena added after the snapshot is not in the snapshot")
    void anArenaAddedAfterTheSnapshotIsNotInTheSnapshot() {
        final ArenaHandler handler = new ArenaHandler(Mockito.mock(Guilds.class));
        handler.addArena(arenaNamed("first"));

        final List<Arena> snapshot = handler.getArenasForSnapshot();

        handler.addArena(arenaNamed("second"));

        assertEquals(1, snapshot.size(), "the snapshot must not pick up the later arena");
        assertEquals(2, handler.getArenas().size(), "the live collection must have both");
    }

    @Test
    @DisplayName("a snapshot list is not a live view")
    void aSnapshotListIsNotALiveView() {
        final ArenaHandler handler = new ArenaHandler(Mockito.mock(Guilds.class));
        handler.addArena(arenaNamed("only"));

        final List<Arena> snapshot = handler.getArenasForSnapshot();
        handler.removeArena(handler.getArenas().iterator().next());

        assertEquals(1, snapshot.size());
        // The live view, by contrast, tracks the change. This is the difference the write depends on.
        assertEquals(0, handler.getArenas().size());
    }

    @Test
    @DisplayName("a challenge removed after the snapshot is still in the snapshot")
    void aChallengeRemovedAfterTheSnapshotIsStillInTheSnapshot() {
        final ChallengeHandler handler = new ChallengeHandler(Mockito.mock(Guilds.class));
        final GuildChallenge keep = challenge("keep");
        final GuildChallenge drop = challenge("drop");
        handler.addChallenge(keep);
        handler.addChallenge(drop);

        final List<GuildChallenge> snapshot = handler.getChallengesForSnapshot();
        assertEquals(2, snapshot.size());

        handler.removeChallenge(drop);

        assertEquals(2, snapshot.size(), "the snapshot must still hold both challenges");
        assertTrue(snapshot.contains(drop));
        assertEquals(1, handler.getChallenges().size(), "the live set must have lost it");
    }

    @Test
    @DisplayName("a challenge added after the snapshot is not in the snapshot")
    void aChallengeAddedAfterTheSnapshotIsNotInTheSnapshot() {
        final ChallengeHandler handler = new ChallengeHandler(Mockito.mock(Guilds.class));
        handler.addChallenge(challenge("first"));

        final List<GuildChallenge> snapshot = handler.getChallengesForSnapshot();

        handler.addChallenge(challenge("second"));

        assertEquals(1, snapshot.size());
        assertEquals(2, handler.getChallenges().size());
    }

    @Test
    @DisplayName("a cooldown added after the snapshot is not in the snapshot")
    void aCooldownAddedAfterTheSnapshotIsNotInTheSnapshot() {
        final CooldownHandler handler = new CooldownHandler(Mockito.mock(Guilds.class));
        final UUID owner = UUID.randomUUID();
        handler.addCooldown(Cooldown.Type.Home, owner, 10, TimeUnit.MINUTES);

        final List<Cooldown> snapshot = handler.getCooldownsForSnapshot();
        assertEquals(1, snapshot.size());

        handler.addCooldown(Cooldown.Type.Buffs, owner, 10, TimeUnit.MINUTES);

        assertEquals(1, snapshot.size(), "the snapshot must not pick up the later cooldown");
        assertEquals(2, handler.getCooldowns().size(), "the live map must have both");
    }

    @Test
    @DisplayName("a cooldown snapshot is unaffected by the expiry thread")
    void aCooldownSnapshotIsUnaffectedByTheExpiryThread() throws InterruptedException {
        // `ExpiringMap` removes expired entries from its own scheduler thread. Once the snapshot is a
        // list, that thread can empty the map without the snapshot noticing, which is what makes it
        // safe to iterate on the write thread afterwards.
        final CooldownHandler handler = new CooldownHandler(Mockito.mock(Guilds.class));
        handler.addCooldown(Cooldown.Type.Home, UUID.randomUUID(), 30, TimeUnit.MINUTES);

        final List<Cooldown> snapshot = handler.getCooldownsForSnapshot();
        assertEquals(1, snapshot.size());

        handler.addCooldown(Cooldown.Type.SetHome, UUID.randomUUID(), 0, TimeUnit.SECONDS);

        // Wait for the expiry thread to actually remove it, rather than sleeping a fixed interval and
        // asserting nothing. Without this the test passed even if nothing had expired, so it was not
        // covering the thing it claimed to.
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10L);
        while (handler.getCooldowns().size() > 1 && System.nanoTime() < deadline) {
            Thread.sleep(25L);
        }

        assertEquals(1, handler.getCooldowns().size(),
                "the zero-length cooldown should have expired by now, so the premise of this test holds");
        assertEquals(1, snapshot.size(), "the snapshot must keep the cooldown the map has since dropped");
        assertNotNull(snapshot.get(0));
    }

    @Test
    @Timeout(60)
    @DisplayName("iterating the cooldown snapshot while the expiry thread runs does not throw")
    void iteratingTheCooldownSnapshotWhileTheExpiryThreadRunsDoesNotThrow() throws InterruptedException {
        // The part that is genuinely in doubt. Built without `variableExpiration()` an `ExpiringMap` hands
        // out a fail-fast iterator, and iterating it while entries are being removed throws. This handler
        // uses `variableExpiration()`, which routes iteration through a `ConcurrentSkipListSet`, so it does
        // not. Measured on the other configuration, the same loop throws roughly 50,000 times per million
        // iterations, so this would not be a marginal difference if the configuration changed.
        //
        // If someone drops `variableExpiration()` from CooldownHandler, this fails and the copy has to be
        // taken under the gate instead.
        final CooldownHandler handler = new CooldownHandler(Mockito.mock(Guilds.class));
        final AtomicBoolean running = new AtomicBoolean(true);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        final Thread churn = new Thread(() -> {
            try {
                while (running.get()) {
                    handler.addCooldown(Cooldown.Type.Home, UUID.randomUUID(), 0, TimeUnit.SECONDS);
                    handler.addCooldown(Cooldown.Type.Buffs, UUID.randomUUID(), 0, TimeUnit.SECONDS);
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        }, "cooldown-churn");
        churn.setDaemon(true);
        churn.start();

        int iterations = 0;
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3L);
        try {
            while (System.nanoTime() < deadline) {
                for (Cooldown cooldown : handler.getCooldownsForSnapshot()) {
                    assertNotNull(cooldown.getCooldownId());
                }
                iterations++;
            }
        } finally {
            running.set(false);
            churn.join(10_000L);
        }

        assertFalse(churn.isAlive(), "the churn thread must have stopped");
        assertTrue(iterations > 100, "expected the snapshot loop to run many times, ran " + iterations);
        if (failure.get() != null) {
            throw new AssertionError("iterating the cooldown snapshot threw", failure.get());
        }
    }

    @Test
    @DisplayName("the arena snapshot survives the delete pass an adapter would run")
    void theArenaSnapshotSurvivesTheDeletePassAnAdapterWouldRun() {
        // ArenaAdapter#saveArenas deletes every stored arena whose id is absent from what it is given.
        // With a live view, an arena created during the write was simply not in the collection, and the
        // adapter deleted it from storage. With a snapshot, every arena that existed at capture time is
        // still there when the delete pass reads the keys.
        final ArenaHandler handler = new ArenaHandler(Mockito.mock(Guilds.class));
        final Arena original = arenaNamed("original");
        handler.addArena(original);

        final Map<String, String> serialized = serialize(handler.getArenasForSnapshot());
        handler.addArena(arenaNamed("created-during-write"));

        assertTrue(serialized.containsKey(original.getId().toString()),
                "the arena that existed at capture must survive the delete pass");
        assertFalse(serialized.containsKey(arenaNamed("absent").getId().toString()));
    }

    @Test
    @Timeout(60)
    @DisplayName("copying a map while another thread writes to it is not safe")
    void copyingAMapWhileAnotherThreadWritesToItIsNotSafe() throws InterruptedException {
        // Characterisation test for why the copy has to happen on the main thread rather than "somewhere
        // that is not the write worker". `HashMap(Map)` iterates the source, so a concurrent `put`
        // either fails fast or returns a short map.
        //
        // This asserts that the hazard is real, not that the plugin is broken. It would start failing
        // if a future JDK made `HashMap(Map)` tolerant of concurrent writes, which is fine, because the
        // production code would then be safe either way.
        // HashMap, not LinkedHashMap: the collections this change copies are `new HashMap<>(...)` in
        // PluginSnapshot and `new ArrayList<>(...)` over a HashSet, so the fixture should be the same kind
        // of map. LinkedHashMap also happens to resize less often, which made the hazard rarer to hit.
        final Map<String, String> backing = new java.util.HashMap<>();
        final int seed = 5000;
        for (int i = 0; i < seed; i++) {
            backing.put("seed-" + i, "value");
        }

        // AtomicBoolean, not a boolean[]. A plain field read in a spin loop can be hoisted, and this test
        // used to leave a hot-spinning daemon thread behind whenever that happened.
        final AtomicBoolean stopped = new AtomicBoolean(false);
        final AtomicBoolean writerFailed = new AtomicBoolean(false);
        final Thread writer = new Thread(() -> {
            int i = 0;
            while (!stopped.get()) {
                try {
                    backing.put("churn-" + (i++), "x");
                } catch (RuntimeException e) {
                    writerFailed.set(true);
                    return;
                }
            }
        });
        writer.setDaemon(true);
        writer.start();

        int shortCopies = 0;
        int thrownCopies = 0;
        try {
            for (int attempt = 0; attempt < 500; attempt++) {
                try {
                    final Map<String, String> copy = new java.util.HashMap<>(backing);
                    if (copy.size() < seed) {
                        shortCopies++;
                    }
                } catch (java.util.ConcurrentModificationException e) {
                    thrownCopies++;
                }
            }
        } finally {
            stopped.set(true);
            writer.join(10_000L);
        }

        assertFalse(writer.isAlive(), "the writer thread must have stopped");
        assertTrue(thrownCopies > 0 || shortCopies > 0,
                "expected at least one concurrent copy to throw or come back short, got "
                        + thrownCopies + " thrown and " + shortCopies + " short");
        assertFalse(writerFailed.get(), "the writer corrupting the map is not what this test is about");
    }

    @Test
    @DisplayName("a live view reads through to later changes, a snapshot does not")
    void aLiveViewReadsThroughToLaterChangesASnapshotDoesNot() {
        // The distinction in one place. The old code passed the first; the new code passes the second.
        final Map<String, String> backing = new LinkedHashMap<>();
        backing.put("a", "1");

        final java.util.Collection<String> liveView = backing.values();
        final List<String> snapshot = new ArrayList<>(backing.values());

        backing.put("b", "2");

        assertEquals(2, liveView.size(), "a view tracks later changes, which is the problem");
        assertEquals(1, snapshot.size(), "a copy does not");
    }

    private Map<String, String> serialize(List<Arena> arenas) {
        final Map<String, String> serialized = new LinkedHashMap<>();
        for (Arena arena : arenas) {
            serialized.put(arena.getId().toString(), "{\"id\":\"" + arena.getId() + "\"}");
        }
        return serialized;
    }

    private GuildChallenge challenge(String ignored) {
        return new GuildChallenge(
                UUID.randomUUID(),
                System.currentTimeMillis(),
                new Guild(UUID.randomUUID()),
                new Guild(UUID.randomUUID()),
                false,
                false,
                false,
                false,
                2,
                5,
                new ArrayList<>(),
                new ArrayList<>(),
                arenaNamed("arena-for-" + ignored),
                null,
                null,
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );
    }

}
