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
import me.glaremasters.guilds.guild.GuildHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers a capture that spans several ticks.
 *
 * <p>Spreading the capture is what keeps the save off the tick budget, but it introduces hazards a
 * single-pass capture cannot have: the guild map can change between one tick and the next, and the
 * snapshot is only written once every guild has been visited. Each test below mutates the live state
 * while a capture is part-way through and checks what the finished snapshot contains.
 *
 * <p>The one that matters most is {@code ArenaAdapter}'s delete-by-absence. If an arena is created or
 * removed while the capture is spread out, the key set the adapter reconciles against decides whether a
 * live arena is deleted from storage or a dead one is written back.
 */
class CaptureSessionTest {

    private static final long TINY_BUDGET_NANOS = 1L;

    private Guilds plugin;
    private ArenaHandler arenaHandler;
    private GuildHandler guildHandler;
    private CaptureSession session;

    @TempDir
    Path dataFolder;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Guilds.class);
        arenaHandler = new ArenaHandler(plugin);
        installGson();
    }

    /**
     * Publishes the real Gson instance, which {@code capture()} needs and {@code onEnable} normally sets.
     */
    private static void installGson() {
        try {
            final java.lang.reflect.Field field = Guilds.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(null, new com.google.gson.GsonBuilder().setPrettyPrinting().create());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("could not install the test Gson instance", e);
        }
    }

    private CaptureSession begin() {
        session = new CaptureSession(guildHandler, arenaHandler, null, null, null);
        return session;
    }

    @Nested
    @DisplayName("progress")
    class Progress {

        @Test
        @DisplayName("a capture with no guilds finishes on the first step")
        void aCaptureWithNoGuildsFinishesOnTheFirstStep() {
            final CaptureSession started = begin();

            assertFalse(started.hasWork(), "there is nothing to do");
            assertTrue(started.step(Long.MAX_VALUE / 4L), "so the first step completes it");
        }

        @Test
        @DisplayName("a capture reports progress and finishes exactly once")
        void aCaptureReportsProgressAndFinishesExactlyOnce() throws Exception {
            final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(8, dataFolder);
            final CaptureSession started = new CaptureSession(handler, arenaHandler, null, null, null);

            int steps = 0;
            while (!started.step(Long.MAX_VALUE / 4L)) {
                steps++;
                assertTrue(steps < 100, "an unbounded budget must finish in one step");
            }

            assertEquals(0, steps, "an unbounded budget needs no extra steps");
            assertFalse(started.hasWork());
            assertEquals(0, started.remaining());
            assertTrue(started.step(Long.MAX_VALUE / 4L), "and stays finished");
            assertEquals(8, started.finish().getGuilds().size(), "with everything captured");
        }

        @Test
        @Timeout(30)
        @DisplayName("a one-nanosecond budget still finishes, one guild per step")
        void aOneNanosecondBudgetStillFinishesOneGuildPerStep() throws Exception {
            // The property the budget relies on. `step` checks the deadline after each guild, so a budget
            // that has already expired still advances by one and the loop always terminates. A check placed
            // before the work instead would make a small enough budget loop forever, and a check that
            // returned early would leave the capture unable to finish.
            //
            // Needs a real handler: with no guilds the loop body never runs and any deadline handling passes.
            final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(32, dataFolder);
            final CaptureSession started = new CaptureSession(handler, arenaHandler, null, null, null);

            int steps = 0;
            while (!started.step(TINY_BUDGET_NANOS)) {
                steps++;
                assertTrue(steps < 1000, "a one-nanosecond budget must still terminate, took " + steps + " steps");
            }

            assertEquals(32, steps, "one guild per step: every step but the last returns false");
            assertEquals(32, started.finish().getGuilds().size());
        }
    }

    @Nested
    @DisplayName("arena reconciliation")
    class ArenaReconciliation {

        @Test
        @DisplayName("an arena created during the capture is written, not deleted")
        void anArenaCreatedDuringTheCaptureIsWrittenNotDeleted() {
            // The safe direction. Because arenas are read in `finish()`, an arena created while the
            // guilds were being serialised is included, so it is written rather than deleted.
            //
            // Had arenas been read in the constructor, this arena would have been absent from the key
            // set. That is harmless on its own, because the delete pass only removes ids storage already
            // holds and a new arena is not in storage yet, so it would simply have been saved a minute
            // later. Including it is better than that, and it is what falls out of reading arenas last.
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "before"));
            final CaptureSession started = begin();

            arenaHandler.addArena(new Arena(UUID.randomUUID(), "during"));

            final PluginSnapshot snapshot = started.finish();

            assertEquals(2, snapshot.getArenas().size(), "both arenas should be in the key set");
            for (Map.Entry<String, String> entry : snapshot.getArenas().entrySet()) {
                assertTrue(entry.getValue().contains("\"name\""), "each arena should be serialised in full");
            }
        }

        @Test
        @DisplayName("an arena removed during the capture is not written back")
        void anArenaRemovedDuringTheCaptureIsNotWrittenBack() {
            // This is the dangerous direction. ArenaAdapter deletes stored arenas missing from the map,
            // so a snapshot that still lists a removed arena does not delete it, and the removed arena
            // stays in storage until something else removes it. Capturing arenas in `finish()`, after the
            // guilds, is what keeps this window to a tick rather than the whole capture.
            final Arena doomed = new Arena(UUID.randomUUID(), "doomed");
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "survivor"));
            arenaHandler.addArena(doomed);

            final CaptureSession started = begin();
            arenaHandler.removeArena(doomed);

            final PluginSnapshot snapshot = started.finish();

            assertEquals(1, snapshot.getArenas().size(), "only the surviving arena should be in the snapshot");
            assertFalse(snapshot.getArenas().containsKey(doomed.getId().toString()),
                    "a removed arena must not be written back, or it is resurrected in storage");
        }

        @Test
        @DisplayName("the arena key set is built in one pass")
        void theArenaKeySetIsBuiltInOnePass() {
            // The property that makes the previous test hold: every arena is read at the same instant,
            // so there is no tick during which the key set is half old and half new.
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "a"));
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "b"));
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "c"));

            final PluginSnapshot snapshot = begin().finish();

            assertEquals(3, snapshot.getArenas().size());
            for (Map.Entry<String, String> entry : snapshot.getArenas().entrySet()) {
                assertTrue(entry.getValue().contains("\"name\""), "each arena should be serialised in full");
            }
        }

        @Test
        @DisplayName("removing every arena gives an empty key set, not a stale one")
        void removingEveryArenaGivesAnEmptyKeySetNotAStaleOne() throws Exception {
            // With real guilds to spread over, so the arena removal happens part-way through a capture
            // rather than before it starts. That is the window the "captured last" design exists for.
            final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(16, dataFolder);
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "a"));
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "b"));

            final CaptureSession started = new CaptureSession(handler, arenaHandler, null, null, null);
            started.step(TINY_BUDGET_NANOS);
            assertTrue(started.hasWork(), "the capture should still be running");

            arenaHandler.getArenas().clear();

            while (!started.step(TINY_BUDGET_NANOS)) {
                // Drain the guilds.
            }

            final PluginSnapshot snapshot = started.finish();

            assertTrue(snapshot.getArenas().isEmpty(),
                    "an empty key set makes the adapter delete every stored arena, which is what should"
                            + " happen when every arena has been removed");
            assertEquals(16, snapshot.getGuilds().size(), "the guilds were still captured in full");
        }
    }

    @Nested
    @DisplayName("guild removal")
    class GuildRemoval {

        @Test
        @DisplayName("a handler with no guilds yields no pending work")
        void aHandlerWithNoGuildsYieldsNoPendingWork() {
            // A guild vanishing mid-capture needs a real GuildHandler, which is not constructible without
            // a data folder and a database. Those cases live in GuildSnapshotRemovalTest, which builds
            // one for real. What is checked here is the empty shape.
            final CaptureSession started = begin();

            assertFalse(started.hasWork());
            assertEquals(0, started.remaining());
            assertTrue(started.step(Long.MAX_VALUE / 4L));
            assertTrue(started.finish().getGuilds().isEmpty());
        }
    }

    @Nested
    @DisplayName("ordering")
    class Ordering {

        @Test
        @DisplayName("arenas are captured when finish is called, not in the constructor")
        void arenasAreCapturedWhenFinishIsCalledNotInTheConstructor() {
            // Pins the design decision. If arenas were captured in the constructor, an arena created or
            // removed during a spread capture would be reconciled against a stale key set.
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "before"));
            final CaptureSession started = begin();

            arenaHandler.addArena(new Arena(UUID.randomUUID(), "after"));

            final PluginSnapshot snapshot = started.finish();
            final PluginSnapshot snapshotAgain = started.finish();

            assertEquals(2, snapshot.getArenas().size(), "finish reads the arenas as they are when called");
            assertEquals(2, snapshotAgain.getArenas().size(), "and stays consistent if called twice");
        }

        @Test
        @DisplayName("finishing twice yields two independent snapshots")
        void finishingTwiceYieldsTwoIndependentSnapshots() {
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "one"));
            final CaptureSession started = begin();

            final PluginSnapshot first = started.finish();
            arenaHandler.addArena(new Arena(UUID.randomUUID(), "two"));
            final PluginSnapshot second = started.finish();

            assertEquals(1, first.getArenas().size(), "the first snapshot is unaffected by the later change");
            assertEquals(2, second.getArenas().size());
        }
    }

    @Test
    @Timeout(30)
    @DisplayName("a spread capture of a real guild set produces a complete snapshot")
    void aSpreadCaptureOfARealGuildSetProducesACompleteSnapshot() throws Exception {
        // The end-to-end shape: a real GuildHandler, a one-nanosecond budget so every guild takes its own
        // tick, and a check that every guild ends up in the snapshot exactly once.
        final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(64, dataFolder);

        final CaptureSession started = new CaptureSession(handler, arenaHandler, null, null, null);

        int ticks = 0;
        while (!started.step(TINY_BUDGET_NANOS)) {
            ticks++;
            assertTrue(ticks < 10_000, "a one-nanosecond budget over 64 guilds should take tens of ticks");
        }

        final PluginSnapshot snapshot = started.finish();

        assertEquals(64, snapshot.getGuilds().size(), "every guild should appear exactly once");
        assertEquals(64, handler.getGuilds().size());
        // One guild per step. Every step returns false, including the one that serialises the last guild,
        // because the deadline is checked after the work rather than before it, so the count equals the
        // guild count. That is the guarantee the budget rests on: a check before the work would allow a
        // small enough budget to never advance at all.
        assertEquals(64, ticks, "a one-nanosecond budget must not do more than one guild per tick");
        for (Map.Entry<String, String> entry : snapshot.getGuilds().entrySet()) {
            assertTrue(entry.getValue().contains("\"id\""), "each guild should be serialised in full");
        }
    }
}
