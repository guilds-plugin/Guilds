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
import me.glaremasters.guilds.guild.Guild;
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
 * Covers a save's guild rows, and the disband tombstones that outlive them.
 *
 * <p>{@code GuildAdapter} has no delete pass, so a stale snapshot does not fail to remove a disbanded
 * guild — it puts the row back, and nothing would clear it. A tombstone is the standing instruction to
 * delete one, held until a save has actually done it.
 *
 * <p>Interleavings are arranged with latches rather than timing, so the ordering is a fact of the test
 * rather than something a sleep might reproduce.
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
        // Read by the tombstone pass to decide whether a delete is worth attempting.
        Mockito.when(database.isConnected()).thenReturn(true);
    }

    private PluginSnapshot snapshotOf(Map<String, String> captured) {
        return new PluginSnapshot(captured, Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyList(), database);
    }

    /** Two guilds in the snapshot, one of them disbanded. */
    private static Map<String, String> pairWith(String disbanded) {
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");
        captured.put(UUID.randomUUID().toString(), "{}");
        return captured;
    }

    // ---------------------------------------------------------------------------------------
    // A disband the write has not reached
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a guild disbanded before the write is not written")
    void aGuildDisbandedBeforeTheWriteIsNotWritten() {
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = pairWith(disbanded);

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));

        assertEquals(1, guildsWritten().size());
        assertFalse(guildsWritten().contains(disbanded));
    }

    @Test
    @DisplayName("a capture taken while a write is in flight does not clear that write's tombstones")
    void aCaptureTakenWhileAWriteIsInFlightDoesNotClearThatWritesTombstones() {
        // A migration captures before it takes the write permit, so a capture can overlap an autosave write
        // that is already running. Pruning on capture would erase a disband that write was about to act on.
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = pairWith(disbanded);

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        coordinator.beginCapture();

        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertFalse(guildsWritten().contains(disbanded));
    }

    @Test
    @DisplayName("a completed write acknowledges the tombstones it has acted on")
    void aCompletedWriteAcknowledgesTheTombstonesItHasActedOn() {
        // So the set cannot grow for as long as the plugin runs. A guild left in it is harmless — it is out
        // of the live map, so no later snapshot holds it — but an unbounded set is not.
        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = pairWith(disbanded);

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertEquals(1, guildsWritten().size());

        // Dealt with, so this write covers everything in the snapshot.
        assertTrue(coordinator.writeTo(database, snapshotOf(captured), null));
        assertEquals(2, guildsWritten().size());
    }

    // ---------------------------------------------------------------------------------------
    // A disband that lands during the write
    // ---------------------------------------------------------------------------------------

    @Test
    @Timeout(30)
    @DisplayName("a guild disbanded while the write is running is removed by the next save")
    void aGuildDisbandedWhileTheWriteIsRunningIsRemovedByTheNextSave() throws Exception {
        // Deletes run before writes, so a tombstone that appears mid-write waits for the following save
        // rather than being chased with this one. That is the deliberate trade: a crash between a write and
        // its delete would leave a disbanded guild written back for good, with nothing left recording it.
        // One save interval late is bounded; being resurrected is not.
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
        Mockito.verify(guilds, Mockito.never()).deleteGuild(Mockito.anyString());

        // Still outstanding, so the next save takes the row out.
        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    @Test
    @Timeout(30)
    @DisplayName("a write that fails still leaves the disband outstanding for the next save")
    void aWriteThatFailsStillLeavesTheDisbandOutstandingForTheNextSave() throws Exception {
        // A partial write is when a resurrected row is most likely, so the tombstone must survive it.
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

        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    // ---------------------------------------------------------------------------------------
    // A deletion that does not land
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a deletion that fails stays outstanding and is retried by the next save")
    void aDeletionThatFailsStaysOutstandingAndIsRetriedByTheNextSave() throws IOException {
        // A disband's own delete is a separate call from the save's, and it fails on its own often enough:
        // a dead connection, a locked SQLite file. Dropping the tombstone there would leave the row behind
        // with nothing left to remove it.
        Mockito.doThrow(new IOException("the connection went away"))
                .doNothing()
                .when(guilds).deleteGuild(Mockito.anyString());

        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = new LinkedHashMap<>();
        captured.put(disbanded, "{}");

        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));
        assertFalse(coordinator.writeTo(database, snapshotOf(captured), null), "the failure has to be reported");

        // Nothing to write, because the guild is gone from the live map. The retry is the whole point.
        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));

        Mockito.verify(guilds, Mockito.times(2)).deleteGuild(disbanded);
    }

    @Test
    @DisplayName("a tombstone is acknowledged only once its own row is gone")
    void aTombstoneIsAcknowledgedOnlyOnceItsOwnRowIsGone() throws IOException {
        // Acknowledged per id rather than by clearing the set, so a permanently broken row does not keep
        // every healthy tombstone alive beside it for the rest of the session.
        final String broken = UUID.randomUUID().toString();
        final String healthy = UUID.randomUUID().toString();
        Mockito.doThrow(new IOException("still broken"))
                .when(guilds).deleteGuild(broken);

        coordinator.noteGuildDisbanded(UUID.fromString(broken));
        coordinator.noteGuildDisbanded(UUID.fromString(healthy));
        assertFalse(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));

        Mockito.doNothing().when(guilds).deleteGuild(broken);
        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));

        // The healthy one is not deleted a second time; the broken one is.
        Mockito.verify(guilds, Mockito.times(1)).deleteGuild(healthy);
        Mockito.verify(guilds, Mockito.times(2)).deleteGuild(broken);
    }

    @Test
    @DisplayName("the last guild on a server is deleted from an empty snapshot")
    void theLastGuildOnAServerIsDeletedFromAnEmptySnapshot() throws IOException {
        // The snapshot is empty, so there is nothing to write, and this is the only case a single-guild
        // server ever reaches. Short-circuiting on the empty map dropped the tombstone here.
        final String last = UUID.randomUUID().toString();
        coordinator.noteGuildDisbanded(UUID.fromString(last));

        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));

        Mockito.verify(guilds).deleteGuild(last);
        Mockito.verify(guilds, Mockito.never()).saveSerialized(Mockito.anyMap());
    }

    @Test
    @DisplayName("a backend that is plainly unreachable leaves the tombstones alone")
    void aBackendThatIsPlainlyUnreachableLeavesTheTombstonesAlone() throws IOException {
        // Each outstanding tombstone would otherwise pay a full connection timeout and log a stack trace on
        // every save while the database is down.
        Mockito.when(database.isConnected()).thenReturn(false);
        final String disbanded = UUID.randomUUID().toString();
        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));

        assertFalse(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));

        Mockito.verify(guilds, Mockito.never()).deleteGuild(Mockito.anyString());

        // And it is still owed once the backend is back.
        Mockito.when(database.isConnected()).thenReturn(true);
        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    @Test
    @DisplayName("a write to a backend that is not live does not acknowledge a tombstone")
    void aWriteToABackendThatIsNotLiveDoesNotAcknowledgeATombstone() throws IOException {
        // A migration's first write targets a destination that never held the row. Acknowledging there would
        // drop a tombstone whose row is still in the backend the plugin stays on when the migration fails,
        // and nothing would ever remove it.
        final DatabaseAdapter destination = Mockito.mock(DatabaseAdapter.class);
        final GuildAdapter destinationGuilds = Mockito.mock(GuildAdapter.class);
        Mockito.when(destination.getGuildAdapter()).thenReturn(destinationGuilds);
        Mockito.when(destination.getArenaAdapter()).thenReturn(Mockito.mock(ArenaAdapter.class));
        Mockito.when(destination.getChallengeAdapter()).thenReturn(Mockito.mock(ChallengeAdapter.class));
        Mockito.when(destination.getCooldownAdapter()).thenReturn(Mockito.mock(CooldownAdapter.class));
        Mockito.when(destination.isConnected()).thenReturn(true);

        final String disbanded = UUID.randomUUID().toString();
        final Map<String, String> captured = pairWith(disbanded);
        coordinator.noteGuildDisbanded(UUID.fromString(disbanded));

        // The guild is still left out of what the destination is given, so the row is never written there.
        assertTrue(coordinator.writeTo(destination, snapshotOf(captured), null));
        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());

        // The migration failed, so the plugin is still on the live backend, which still owes the delete.
        assertTrue(coordinator.writeTo(database, snapshotOf(Collections.emptyMap()), null));
        Mockito.verify(guilds).deleteGuild(disbanded);
    }

    // ---------------------------------------------------------------------------------------
    // The wiring
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("disbanding a guild reports it, so a save in flight cannot write it back")
    void disbandingAGuildReportsItSoASaveInFlightCannotWriteItBack() throws IOException {
        // Without this the set is never populated and the exclusion never fires, which is the state the bug
        // was found in.
        final PersistenceCoordinator wired =
                new PersistenceCoordinator(plugin, gate, null, null, null, null, () -> true);
        Mockito.when(plugin.getPersistenceCoordinator()).thenReturn(wired);

        final GuildHandler handler = GuildSnapshotRemovalTest.newHandler(2, null, plugin);
        final Guild disband = new ArrayList<>(handler.getGuilds().values()).get(0);

        handler.removeGuild(disband);

        final Map<String, String> captured = pairWith(disband.getId().toString());
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
