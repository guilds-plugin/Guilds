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
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.arenas.ArenaAdapter;
import me.glaremasters.guilds.database.challenges.ChallengeAdapter;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.GuildHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the coordinator's ordering, gating and failure handling.
 *
 * <p>These are the tests that matter for the bugs actually found in review. The gate and the snapshot
 * were straightforward; the defects were all in the sequencing around them:
 *
 * <ul>
 *   <li>a write scheduled onto a worker that never ran it left the permit held forever, disabling every
 *       later save including the shutdown flush;</li>
 *   <li>a failed adapter threw out of {@code writeTo} and skipped the collections sequenced after it;</li>
 *   <li>the shutdown flush closed the connection pool while a writer the drain had given up on was still
 *       using it;</li>
 *   <li>a save re-read the plugin's backend instead of using the one captured with the data, so a
 *       migration swapping the field mid-save split the records across two backends.</li>
 * </ul>
 */
class PersistenceCoordinatorTest {

    @TempDir
    java.nio.file.Path tempFolder;

    private Guilds plugin;
    private PersistenceGate gate;
    private DatabaseAdapter database;
    private GuildAdapter guildAdapter;
    private ArenaAdapter arenaAdapter;
    private ChallengeAdapter challengeAdapter;
    private CooldownAdapter cooldownAdapter;
    private PersistenceCoordinator coordinator;

    @BeforeEach
    void setUp() throws IOException {
        gate = new PersistenceGate();
        installGson();
        database = Mockito.mock(DatabaseAdapter.class);
        guildAdapter = Mockito.mock(GuildAdapter.class);
        arenaAdapter = Mockito.mock(ArenaAdapter.class);
        challengeAdapter = Mockito.mock(ChallengeAdapter.class);
        cooldownAdapter = Mockito.mock(CooldownAdapter.class);

        Mockito.when(database.getGuildAdapter()).thenReturn(guildAdapter);
        Mockito.when(database.getArenaAdapter()).thenReturn(arenaAdapter);
        Mockito.when(database.getChallengeAdapter()).thenReturn(challengeAdapter);
        Mockito.when(database.getCooldownAdapter()).thenReturn(cooldownAdapter);

        plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDatabase()).thenReturn(database);

        // Real handlers rather than mocks, so `capture()` goes through the production snapshot path. An
        // empty handler would make the snapshot empty, and an empty snapshot legitimately skips every
        // adapter, which is a different thing from what these tests are checking.
        //
        // `GuildHandler` is left null: its constructor reads roles.yml and tiers.yml off the plugin data
        // folder and queries the database, so it is not constructible here. Guild content is covered in
        // SnapshotIsolationTest and by the adapters receiving whatever the snapshot holds.
        final ArenaHandler arenaHandler = new ArenaHandler(plugin);
        arenaHandler.addArena(new me.glaremasters.guilds.arena.Arena(UUID.randomUUID(), "test-arena"));

        final ChallengeHandler challengeHandler = new ChallengeHandler(plugin);
        challengeHandler.addChallenge(newChallenge());

        final CooldownHandler cooldownHandler = new CooldownHandler(plugin);
        cooldownHandler.addCooldown(
                me.glaremasters.guilds.cooldowns.Cooldown.Type.Home,
                UUID.randomUUID(),
                10,
                TimeUnit.MINUTES
        );

        coordinator = new PersistenceCoordinator(
                plugin,
                gate,
                null,
                arenaHandler,
                challengeHandler,
                cooldownHandler
        );
    }

    private me.glaremasters.guilds.guild.GuildChallenge newChallenge() {
        return new me.glaremasters.guilds.guild.GuildChallenge(
                UUID.randomUUID(),
                System.currentTimeMillis(),
                new me.glaremasters.guilds.guild.Guild(UUID.randomUUID()),
                new me.glaremasters.guilds.guild.Guild(UUID.randomUUID()),
                false,
                false,
                false,
                false,
                2,
                5,
                new java.util.ArrayList<>(),
                new java.util.ArrayList<>(),
                new me.glaremasters.guilds.arena.Arena(UUID.randomUUID(), "challenge-arena"),
                null,
                null,
                new java.util.LinkedHashMap<>(),
                new java.util.LinkedHashMap<>()
        );
    }

    /**
     * Publishes the real Gson instance into {@code Guilds}'s private static field.
     *
     * <p>{@code Guilds#getGson()} is assigned in {@code onEnable} and there is no setter, so
     * {@code capture()} returns an empty snapshot without it. Built the same way production builds it,
     * pretty printing included, so what the tests serialise is what a server would write.
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

    private PluginSnapshot snapshotWith(String guildId, String arenaId, String challengeId) {
        final Map<String, String> guilds = Collections.singletonMap(guildId, "{\"guild\":true}");
        final Map<String, String> arenas = Collections.singletonMap(arenaId, "{\"arena\":true}");
        final Map<String, String> challenges = Collections.singletonMap(challengeId, "{\"challenge\":true}");
        return new PluginSnapshot(
                guilds,
                arenas,
                challenges,
                Collections.emptyList(),
                database
        );
    }

    @Nested
    @DisplayName("write")
    class Write {

        @Test
        @DisplayName("writes the serialised records to the captured backend")
        void writesTheSerialisedRecordsToTheCapturedBackend() throws IOException {
            final PluginSnapshot snapshot = snapshotWith("guild-1", "arena-1", "challenge-1");

            coordinator.write(snapshot);

            Mockito.verify(guildAdapter).saveSerialized(snapshot.getGuilds());
            Mockito.verify(arenaAdapter).saveSerialized(snapshot.getArenas());
            Mockito.verify(challengeAdapter).saveSerialized(snapshot.getChallenges());
        }

        @Test
        @DisplayName("writes to the captured backend even after the plugin's backend changes")
        void writesToTheCapturedBackendEvenAfterThePluginsBackendChanges() throws IOException {
            // A save resolves its backend from the snapshot, not from `plugin.getDatabase()`. Migration
            // replaces that field mid-save, and a writer that re-read it would put some guilds in the old
            // backend and the rest in the new one.
            final DatabaseAdapter other = Mockito.mock(DatabaseAdapter.class);
            final Guilds plugin = Mockito.mock(Guilds.class);
            Mockito.when(plugin.getDatabase()).thenReturn(database, other);

            final PersistenceCoordinator live = new PersistenceCoordinator(
                    plugin, gate, null, null, null, null);
            final PluginSnapshot snapshot = snapshotWith("guild-1", "arena-1", "challenge-1");

            // Simulate the swap happening between capture and write.
            Mockito.when(plugin.getDatabase()).thenReturn(other);
            live.write(snapshot);

            Mockito.verify(guildAdapter).saveSerialized(snapshot.getGuilds());
            Mockito.verifyNoInteractions(Mockito.mock(DatabaseAdapter.class));
            assertEquals(0, Mockito.mockingDetails(other).getInvocations().size(),
                    "the write must not touch the backend the plugin swapped in");
        }

        @Test
        @DisplayName("a snapshot with no backend is a no-op rather than a crash")
        void aSnapshotWithNoBackendIsANoOpRatherThanACrash() {
            final PluginSnapshot snapshot = new PluginSnapshot(
                    Collections.singletonMap("g", "{}"),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    null
            );

            coordinator.write(snapshot);

            Mockito.verifyNoInteractions(guildAdapter);
        }

        @Test
        @DisplayName("a failing adapter does not stop the other collections")
        void aFailingAdapterDoesNotStopTheOtherCollections() throws IOException {
            // The promise in the class javadoc: one bad record does not cost the operator every other kind
            // of data. Cooldowns are last and guarded precisely because their adapter throws unchecked
            // exceptions on a stale connection.
            Mockito.doThrow(new IOException("guild table is gone"))
                    .when(guildAdapter).saveSerialized(Mockito.anyMap());
            Mockito.doThrow(new IllegalStateException("HikariDataSource has been closed"))
                    .when(cooldownAdapter).saveCooldowns(Mockito.anyCollection());

            coordinator.write(snapshotWith("guild-1", "arena-1", "challenge-1"));

            Mockito.verify(arenaAdapter).saveSerialized(Mockito.anyMap());
            Mockito.verify(challengeAdapter).saveSerialized(Mockito.anyMap());
        }

        @Test
        @DisplayName("challenges are written before a cooldown failure")
        void challengesAreWrittenBeforeACooldownFailure() throws IOException {
            // Sequencing matters as much as guarding: a cooldown exception used to skip challenges,
            // because challenges were written after it.
            Mockito.doThrow(new IllegalStateException("pool closed"))
                    .when(cooldownAdapter).saveCooldowns(Mockito.anyCollection());

            final PluginSnapshot snapshot = new PluginSnapshot(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.singletonMap("challenge-1", "{}"),
                    Collections.singletonList(new me.glaremasters.guilds.cooldowns.Cooldown(
                            UUID.randomUUID(),
                            me.glaremasters.guilds.cooldowns.Cooldown.Type.Home,
                            UUID.randomUUID(),
                            System.currentTimeMillis() + 60000L
                    )),
                    database
            );

            coordinator.write(snapshot);

            final InOrder order = Mockito.inOrder(challengeAdapter, cooldownAdapter);
            order.verify(challengeAdapter).saveSerialized(Mockito.anyMap());
            order.verify(cooldownAdapter).saveCooldowns(Mockito.anyCollection());
        }

        @Test
        @DisplayName("an empty snapshot skips the upsert-only adapters")
        void anEmptySnapshotSkipsTheUpsertOnlyAdapters() throws IOException {
            // Guilds, challenges and cooldowns are written by upsert only, so an empty set is a genuine
            // no-op and calling the adapter would be a wasted existence check per record.
            coordinator.write(new PluginSnapshot(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    database
            ));

            Mockito.verifyNoInteractions(guildAdapter, challengeAdapter, cooldownAdapter);
        }

        @Test
        @DisplayName("reports a failed collection so migration can refuse to swap")
        void reportsAFailedCollectionSoMigrationCanRefuseToSwap() throws IOException {
            // Migration is about to close the old backend and point the plugin at this one. Each collection
            // is written inside its own try, so without an explicit signal a failure here is invisible: the
            // swap goes ahead, the old pool closes, and the operator is told it worked while the new backend
            // holds nothing.
            Mockito.doThrow(new IllegalStateException("HikariDataSource has been closed"))
                    .when(arenaAdapter).saveSerialized(Mockito.anyMap());

            final List<String> failures = new ArrayList<>();
            final boolean complete = coordinator.writeTo(database, snapshotWith("g", "a", "c"), failures);

            assertFalse(complete, "a failed collection must be reported, not swallowed");
            assertEquals(1, failures.size(), "and named");
            assertTrue(failures.get(0).startsWith("arena:"), failures.get(0));

            // The others still went, because one backend problem should not cost every other collection.
            Mockito.verify(guildAdapter).saveSerialized(Mockito.anyMap());
            Mockito.verify(challengeAdapter).saveSerialized(Mockito.anyMap());
        }

        @Test
        @DisplayName("reports success when every collection was written")
        void reportsSuccessWhenEveryCollectionWasWritten() throws IOException {
            final List<String> failures = new ArrayList<>();

            assertTrue(coordinator.writeTo(database, snapshotWith("g", "a", "c"), failures));
            assertTrue(failures.isEmpty(), failures.toString());
        }

        @Test
        @DisplayName("an empty arena set still runs the delete pass")
        void anEmptyArenaSetStillRunsTheDeletePass() throws IOException {
            // Not a no-op. `ArenaAdapter#saveSerialized` deletes every stored arena absent from the map it
            // is given, and that pass is the only thing that persists an arena deletion: `removeArena` only
            // touches the in-memory map. Skipping it on an empty set means the last arena an admin deletes
            // is never removed from storage, comes back on the next restart, and can never be deleted again
            // because the map is now permanently empty.
            coordinator.write(new PluginSnapshot(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    database
            ));

            Mockito.verify(arenaAdapter).saveSerialized(Collections.emptyMap());
        }
    }

    @Nested
    @DisplayName("autosave")
    class Autosave {

        /** One minute, so the first tick always starts a capture and later ones wait. */
        private final long intervalNanos = TimeUnit.MINUTES.toNanos(1L);

        private void tickUntilWriteHandedOff(AtomicReference<Runnable> queued, long budgetNanos) {
            for (int i = 0; i < 10_000 && queued.get() == null; i++) {
                coordinator.tick(budgetNanos, intervalNanos, queued::set);
            }
            assertNotNull(queued.get(), "the capture should have completed and handed off a write");
        }

        @Test
        @Timeout(15)
        @DisplayName("releases the permit after a successful write")
        void releasesThePermitAfterASuccessfulWrite() {
            final AtomicReference<Runnable> queued = new AtomicReference<>();

            tickUntilWriteHandedOff(queued, TimeUnit.HOURS.toNanos(1L));
            queued.get().run();

            assertTrue(gate.tryAcquireWriter(), "the permit must come back after a successful write");
        }

        @Test
        @Timeout(15)
        @DisplayName("releases the permit when the write throws")
        void releasesThePermitWhenTheWriteThrows() throws IOException {
            Mockito.doThrow(new IllegalStateException("backend is down"))
                    .when(arenaAdapter).saveSerialized(Mockito.anyMap());
            final AtomicReference<Runnable> queued = new AtomicReference<>();

            tickUntilWriteHandedOff(queued, TimeUnit.HOURS.toNanos(1L));
            queued.get().run();

            assertTrue(gate.tryAcquireWriter(),
                    "a failed write must not close the gate for the rest of the session");
        }

        @Test
        @Timeout(15)
        @DisplayName("releases the permit when the write cannot even be scheduled")
        void releasesThePermitWhenTheWriteCannotEvenBeScheduled() {
            // `BukkitScheduler#runTaskAsynchronously` throws IllegalPluginAccessException once the plugin
            // is disabled. Before this was handled, that exception escaped the autosave and the permit
            // stayed held, which on a `/reload` cycle silently disabled saving for the rest of the session.
            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, write -> {
                throw new IllegalStateException("Plugin attempted to register task while disabled");
            });

            assertTrue(gate.tryAcquireWriter(),
                    "a write that could not be scheduled must still return the permit");
        }

        @Test
        @Timeout(15)
        @DisplayName("runs the write inline when scheduling fails")
        void runsTheWriteInlineWhenSchedulingFails() throws IOException {
            final AtomicBoolean wroteInline = new AtomicBoolean(false);

            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, write -> {
                wroteInline.set(true);
                throw new IllegalStateException("Plugin attempted to register task while disabled");
            });

            // Losing the data is worse than doing the work on the main thread. This is the situation the
            // server is in when it is shutting down, and blocking the tick to write beats discarding a save.
            assertTrue(wroteInline.get(), "the write should still have happened");
            Mockito.verify(arenaAdapter).saveSerialized(Mockito.anyMap());
        }

        @Test
        @DisplayName("skips rather than queues when another writer holds the permit")
        void skipsRatherThanQueuesWhenAnotherWriterHoldsThePermit() throws IOException {
            assertTrue(gate.tryAcquireWriter(), "simulate an in-flight save");
            final AtomicBoolean handedOff = new AtomicBoolean(false);

            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, write -> handedOff.set(true));

            assertFalse(handedOff.get(), "a busy gate must skip the cycle, not queue it");
            assertFalse(coordinator.hasPendingCapture(), "and must not have started a capture");
            Mockito.verifyNoInteractions(arenaAdapter);

            gate.releaseWriter();
        }

        @Test
        @DisplayName("skips while a migration is running")
        void skipsWhileAMigrationIsRunning() {
            // The autosave used to check this flag and no longer does by accident. Migration holds the
            // write permit across its write and the backend swap, so an autosave that starts first makes
            // migration wait out the remainder of a spread capture and then report itself busy. The admin
            // has already spent their confirmation at that point.
            gate.beginMigration();
            final AtomicBoolean handedOff = new AtomicBoolean(false);

            coordinator.tick(TimeUnit.HOURS.toNanos(1L), 0L, write -> handedOff.set(true));

            assertFalse(handedOff.get(), "no save should start while a migration is running");
            assertFalse(coordinator.hasPendingCapture(), "and no capture should be started either");

            gate.endMigration();

            final AtomicReference<Runnable> afterMigration = new AtomicReference<>();
            coordinator.tick(TimeUnit.HOURS.toNanos(1L), 0L, afterMigration::set);
            assertNotNull(afterMigration.get(), "saving resumes once the migration releases the flag");
        }

        @Test
        @DisplayName("skips after shutdown has begun")
        void skipsAfterShutdownHasBegun() {
            gate.beginShutdown();
            final AtomicBoolean handedOff = new AtomicBoolean(false);

            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, write -> handedOff.set(true));

            assertFalse(handedOff.get(), "no save should start once shutdown has begun");
        }

        @Test
        @DisplayName("a capture that fits in one budget is written in the same tick")
        void aCaptureThatFitsInOneBudgetIsWrittenInTheSameTick() {
            final AtomicReference<Runnable> queued = new AtomicReference<>();

            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, queued::set);

            assertFalse(coordinator.hasPendingCapture(), "the capture should have finished");
            assertNotNull(queued.get(), "and handed the write off without needing a second tick");
        }

        @Test
        @DisplayName("does not start another capture straight after finishing one")
        void doesNotStartAnotherCaptureStraightAfterFinishingOne() throws IOException {
            final AtomicReference<Runnable> first = new AtomicReference<>();
            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, first::set);
            first.get().run();

            Mockito.verify(arenaAdapter, Mockito.times(1)).saveSerialized(Mockito.anyMap());

            // The interval has not elapsed, so the next tick must not start a second capture. Without this
            // the autosave would run every tick instead of every interval, and the arena delete pass would
            // run every tick too.
            coordinator.tick(TimeUnit.HOURS.toNanos(1L), intervalNanos, write -> { });

            assertFalse(coordinator.hasPendingCapture(), "no capture should have started");
            Mockito.verify(arenaAdapter, Mockito.times(1)).saveSerialized(Mockito.anyMap());
        }
    }

    @Nested
    @DisplayName("shutdown flush")
    class ShutdownFlush {

        @Test
        @Timeout(20)
        @DisplayName("waits for an in-flight write before writing")
        void waitsForAnInFlightWriteBeforeWriting() throws Exception {
            assertTrue(gate.tryAcquireWriter(), "simulate a save already running");
            final CountDownLatch release = new CountDownLatch(1);
            final CountDownLatch flushStarted = new CountDownLatch(1);

            final Thread holder = new Thread(() -> {
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    gate.releaseWriter();
                }
            });
            holder.start();

            final Thread shutdown = new Thread(() -> coordinator.shutdownFlush(() -> { }));
            shutdown.start();

            Thread.sleep(300L);
            assertFalse(flushStarted.await(200, TimeUnit.MILLISECONDS),
                    "the flush should still be waiting for the drain");

            release.countDown();
            shutdown.join(15_000L);
            holder.join(15_000L);

            Mockito.verify(arenaAdapter, Mockito.atLeastOnce()).saveSerialized(Mockito.anyMap());
        }

        @Test
        @Timeout(20)
        @DisplayName("closes the database only after writing")
        void closesTheDatabaseOnlyAfterWriting() throws IOException {
            // Arenas, because there is no GuildHandler here and so no guilds to write. See setUp.
            final InOrder order = Mockito.inOrder(arenaAdapter);
            final AtomicBoolean closed = new AtomicBoolean(false);

            coordinator.shutdownFlush(() -> {
                closed.set(true);
                try {
                    order.verify(arenaAdapter).saveSerialized(Mockito.anyMap());
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });

            assertTrue(closed.get(), "the close callback should have run");
            order.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("closes the database even when the write fails")
        void closesTheDatabaseEvenWhenTheWriteFails() throws IOException {
            Mockito.doThrow(new IOException("disk full"))
                    .when(guildAdapter).saveSerialized(Mockito.anyMap());
            final AtomicBoolean closed = new AtomicBoolean(false);

            coordinator.shutdownFlush(() -> closed.set(true));

            assertTrue(closed.get(), "a failed save must not leave the connection pool open");
        }

        @Test
        @Timeout(30)
        @DisplayName("does not write or close underneath a writer the drain gave up on")
        void doesNotWriteOrCloseUnderneathAWriterTheDrainGaveUpOn() {
            // A save wedged on a dead connection must not stop the server stopping, but writing anyway is
            // worse than not writing: two threads writing the same `<uuid>.json` files interleave into JSON
            // that will not parse on the next boot, and closing the pool under a running writer has the same
            // character. Corruption beats staleness as a failure only when it is the less likely outcome.
            assertTrue(gate.tryAcquireWriter(), "simulate a save that never finishes");
            final AtomicBoolean closed = new AtomicBoolean(false);

            coordinator.shutdownFlush(() -> closed.set(true));

            assertFalse(closed.get(), "the pool must be left for the writer that is still using it");
            try {
                Mockito.verify(arenaAdapter, Mockito.never()).saveSerialized(Mockito.anyMap());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Test
        @DisplayName("a capture in progress is abandoned so its permit is not held forever")
        void aCaptureInProgressIsAbandonedSoItsPermitIsNotHeldForever() throws Exception {
            // A spread capture only advances on the main thread, and `shutdownFlush` runs on the main
            // thread. Waiting for one to finish would wait forever, so it has to be abandoned instead. If it
            // were not, the drain below would time out after its full wait, the permit would stay taken, and
            // every later save would skip. On a `/reload` that is permanent, because the plugin instance is
            // re-enabled rather than rebuilt.
            //
            // A real handler with real guilds is needed here: with none, a capture finishes inside its first
            // step and there is nothing to abandon.
            final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(8, tempFolder);
            final PersistenceCoordinator spread = new PersistenceCoordinator(
                    plugin, gate, handler, null, null, null);

            spread.tick(1L, 0L, write -> { });
            assertTrue(spread.hasPendingCapture(), "a one-nanosecond budget cannot finish eight guilds");
            assertFalse(gate.tryAcquireWriter(), "the capture is holding the permit");

            spread.shutdownFlush(() -> { });

            assertFalse(spread.hasPendingCapture(), "the capture must be discarded");
            assertTrue(gate.tryAcquireWriter(), "and its permit handed back, or every later save skips");
        }

        @Test
        @Timeout(30)
        @DisplayName("does not return a permit it never took")
        void doesNotReturnAPermitItNeverTook() {
            // On the drain-timeout path the permit is still held by the other writer. Releasing it here
            // would make it available while that writer is still using it, which is the overlap this class
            // exists to prevent.
            assertTrue(gate.tryAcquireWriter(), "simulate a save that never finishes");

            coordinator.shutdownFlush(() -> { });

            assertFalse(gate.tryAcquireWriter(),
                    "the permit must still be held by the writer the drain gave up on");
        }

        @Test
        @DisplayName("announces shutdown to the gate")
        void announcesShutdownToTheGate() {
            coordinator.shutdownFlush(() -> { });

            assertTrue(gate.isShuttingDown());
        }
    }

    @Nested
    @DisplayName("arena deletion")
    class ArenaDeletion {

        @Test
        @DisplayName("the arena map handed to the adapter carries every captured arena")
        void theArenaMapHandedToTheAdapterCarriesEveryCapturedArena() throws IOException {
            final java.util.Map<String, String> arenas = new java.util.LinkedHashMap<>();
            arenas.put("arena-1", "{}");
            arenas.put("arena-2", "{}");

            final PluginSnapshot snapshot = new PluginSnapshot(
                    Collections.emptyMap(),
                    arenas,
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    database
            );

            coordinator.write(snapshot);

            final ArgumentCaptor<java.util.Map<String, String>> captor = ArgumentCaptor.forClass(java.util.Map.class);
            Mockito.verify(arenaAdapter).saveSerialized(captor.capture());

            assertEquals(2, captor.getValue().size(),
                    "ArenaAdapter deletes stored arenas missing from this map, so it must be complete");
            assertTrue(captor.getValue().containsKey("arena-1"));
            assertTrue(captor.getValue().containsKey("arena-2"));
        }
    }
}
