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
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.arenas.ArenaAdapter;
import me.glaremasters.guilds.database.challenges.ChallengeAdapter;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.GuildHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the window between a capture finishing and its asynchronous write landing.
 *
 * <p>A guild disbanded in that window used to be written back, because {@code GuildAdapter} has no delete
 * pass: the disband's own delete ran first and the worker's write recreated the row afterwards, leaving it
 * in storage permanently. The interleaving is arranged with latches rather than timing, because the bug is
 * entirely about which of two operations happens first and a sleep cannot demonstrate that reliably.
 */
class SaveOrderingTest {

    private PersistenceGate gate;
    private Guilds plugin;
    private PersistenceCoordinator coordinator;

    private DatabaseAdapter database;
    private GuildAdapter guilds;
    private ArenaAdapter arenas;
    private ChallengeAdapter challenges;
    private CooldownAdapter cooldowns;

    @BeforeEach
    void setUp() {
        gate = new PersistenceGate();
        plugin = Mockito.mock(Guilds.class);
        coordinator = new PersistenceCoordinator(plugin, gate, null, null, null, null, () -> true);

        database = Mockito.mock(DatabaseAdapter.class);
        guilds = Mockito.mock(GuildAdapter.class);
        arenas = Mockito.mock(ArenaAdapter.class);
        challenges = Mockito.mock(ChallengeAdapter.class);
        cooldowns = Mockito.mock(CooldownAdapter.class);

        Mockito.when(database.getGuildAdapter()).thenReturn(guilds);
        Mockito.when(database.getArenaAdapter()).thenReturn(arenas);
        Mockito.when(database.getChallengeAdapter()).thenReturn(challenges);
        Mockito.when(database.getCooldownAdapter()).thenReturn(cooldowns);
        Mockito.when(plugin.getDatabase()).thenReturn(database);
    }

    private PluginSnapshot snapshotOf(Map<String, String> guildsCaptured) {
        return new PluginSnapshot(guildsCaptured, Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyList(), database);
    }

    // ---------------------------------------------------------------------------------------
    // Disbanded between the capture and the write
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a guild disbanded before the write is not written")
    void aGuildDisbandedBeforeTheWriteIsNotWritten() {
        final String disbanded = UUID.randomUUID().toString();
        final String survivor = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");
        captured.put(survivor, "{}");

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));

        assertEquals(Collections.singleton(survivor), guildsWritten());
    }

    @Test
    @DisplayName("a capture taken while a write is in flight does not clear that write's disbands")
    void aCaptureTakenWhileAWriteIsInFlightDoesNotClearThatWritesDisbands() {
        // A migration captures before it takes the write permit, so a capture can overlap an autosave write
        // that is already running. Clearing the set on capture therefore erased a disband the in-flight write
        // was about to read, and put the row back — permanently, since `GuildAdapter` has no delete pass.
        final String disbanded = UUID.randomUUID().toString();
        final String survivor = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");
        captured.put(survivor, "{}");

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        coordinator.beginCapture();

        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertEquals(Collections.singleton(survivor), guildsWritten());
    }

    @Test
    @DisplayName("a completed write clears the disbands it has already dealt with")
    void aCompletedWriteClearsTheDisbandsItHasAlreadyDealtWith() {
        // Pruning after the write rather than on the next capture, so the set cannot grow for as long as the
        // plugin runs. A guild left in it is harmless — it is out of the live map, so no later snapshot holds
        // it — but an unbounded set is not.
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");
        captured.put(UUID.randomUUID().toString(), "{}");

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertEquals(1, guildsWritten().size());

        // The disband has been dealt with, so it is gone and this write covers everything in the snapshot.
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertEquals(2, guildsWritten().size());
    }

    // ---------------------------------------------------------------------------------------
    // Disbanded while the write is in progress
    // ---------------------------------------------------------------------------------------

    @Test
    @Timeout(30)
    @DisplayName("a guild disbanded while the write is running has its row deleted afterwards")
    void aGuildDisbandedWhileTheWriteIsRunningHasItsRowDeletedAfterwards() throws Exception {
        // The harder ordering: the disband lands after the write has already read the set and chosen what to
        // write, so the row this write just put there has to be taken back out.
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");

        final CountDownLatch writeStarted = new CountDownLatch(1);
        final CountDownLatch releaseWrite = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            writeStarted.countDown();
            assertTrue(releaseWrite.await(20, TimeUnit.SECONDS), "the test never released the write");
            return null;
        }).when(guilds).saveSerialized(Mockito.anyMap());

        final AtomicReference<Throwable> thrown = new AtomicReference<>();
        final Thread worker = new Thread(() -> {
            try {
                coordinator.writeTo(database, snapshotOf(captured), null);
            } catch (Throwable t) {
                thrown.set(t);
            }
        }, "save-under-test");
        worker.setDaemon(true);
        worker.start();

        assertTrue(writeStarted.await(20, TimeUnit.SECONDS), "the write never started");
        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        releaseWrite.countDown();
        worker.join(TimeUnit.SECONDS.toMillis(20));

        assertEquals(null, thrown.get(), String.valueOf(thrown.get()));
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    @Test
    @Timeout(30)
    @DisplayName("a row is removed even when the write it came from failed")
    void aRowIsRemovedEvenWhenTheWriteItCameFromFailed() throws Exception {
        // A partial write is when a resurrected row is most likely, so the removal cannot be skipped just
        // because the write threw.
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");

        final CountDownLatch writeStarted = new CountDownLatch(1);
        final CountDownLatch releaseWrite = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            writeStarted.countDown();
            releaseWrite.await(20, TimeUnit.SECONDS);
            throw new IOException("the connection went away mid-write");
        }).when(guilds).saveSerialized(Mockito.anyMap());

        final AtomicReference<Boolean> complete = new AtomicReference<>();
        final Thread worker = new Thread(
                () -> complete.set(coordinator.writeTo(database, snapshotOf(captured), null)),
                "save-under-test");
        worker.setDaemon(true);
        worker.start();

        assertTrue(writeStarted.await(20, TimeUnit.SECONDS), "the write never started");
        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        releaseWrite.countDown();
        worker.join(TimeUnit.SECONDS.toMillis(20));

        assertFalse(complete.get(), "a failed guild write has to be reported as incomplete");
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    // ---------------------------------------------------------------------------------------
    // What actually reached the backend
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("disbanding a guild reports it, so a save in flight cannot write it back")
    void disbandingAGuildReportsItSoASaveInFlightCannotWriteItBack() throws IOException {
        // The wiring, rather than the coordinator's half of it. Without this the set is never populated and
        // the exclusion above never fires, which is the state the bug was found in.
        final PersistenceCoordinator wired = new PersistenceCoordinator(plugin, gate, null, null, null, null, () -> true);
        Mockito.when(plugin.getPersistenceCoordinator()).thenReturn(wired);

        final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(2, null, plugin);
        final me.glaremasters.guilds.guild.Guild disband = new ArrayList<>(handler.getGuilds().values()).get(0);

        handler.removeGuild(disband);

        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disband.getId().toString(), "{}");
        captured.put(UUID.randomUUID().toString(), "{}");
        assertTrue(wired.writeTo(database, snapshotOf(captured), null));

        assertEquals(1, guildsWritten().size());
        assertFalse(guildsWritten().contains(disband.getId().toString()));
    }

    @SuppressWarnings("unchecked")
    private Set<String> guildsWritten() {
        final ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        try {
            Mockito.verify(guilds, Mockito.atLeastOnce()).saveSerialized(captor.capture());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return captor.getValue().keySet();
    }
}
