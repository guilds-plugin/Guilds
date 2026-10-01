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
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.arenas.ArenaAdapter;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the migration-specific guarantees, with the operations scheduled at chosen points rather than left
 * to race.
 *
 * <p>Every test drives the coordinator directly and decides for itself when the mutation happens, so the
 * interleaving is a fact of the test. The bugs pinned here only appear between two specific operations, which
 * a timing test cannot reliably demonstrate.
 */
class MigrationCoordinatorTest {

    @TempDir
    Path dataFolder;

    /** Reports that the test thread is the main thread, which is what `reconcileAndPublish` requires. */
    private final java.util.function.BooleanSupplier onTestThread = () -> true;

    private PersistenceGate gate;
    private Guilds plugin;
    private GuildHandler guildHandler;
    private ArenaHandler arenaHandler;
    private CooldownHandler cooldownHandler;
    private PersistenceCoordinator coordinator;

    private DatabaseAdapter source;
    private DatabaseAdapter destination;
    private GuildAdapter sourceGuilds;
    private GuildAdapter destinationGuilds;
    private ArenaAdapter destinationArenas;
    private CooldownAdapter destinationCooldowns;

    @BeforeEach
    void setUp() throws Exception {
        installGson();

        gate = new PersistenceGate();
        plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getPersistenceGate()).thenReturn(gate);

        source = Mockito.mock(DatabaseAdapter.class);
        destination = Mockito.mock(DatabaseAdapter.class);
        sourceGuilds = Mockito.mock(GuildAdapter.class);
        destinationGuilds = Mockito.mock(GuildAdapter.class);
        destinationArenas = Mockito.mock(ArenaAdapter.class);
        destinationCooldowns = Mockito.mock(CooldownAdapter.class);

        Mockito.when(source.getGuildAdapter()).thenReturn(sourceGuilds);
        Mockito.when(destination.getGuildAdapter()).thenReturn(destinationGuilds);
        Mockito.when(destination.getArenaAdapter()).thenReturn(destinationArenas);
        Mockito.when(destination.getCooldownAdapter()).thenReturn(destinationCooldowns);
        Mockito.when(destination.getChallengeAdapter())
                .thenReturn(Mockito.mock(me.glaremasters.guilds.database.challenges.ChallengeAdapter.class));

        // The destination starts empty, as a freshly-created backend is.
        Mockito.when(destinationGuilds.getAllGuildIds()).thenReturn(new ArrayList<String>());

        arenaHandler = new ArenaHandler(plugin);
        cooldownHandler = new CooldownHandler(plugin);
        coordinator = new PersistenceCoordinator(plugin, gate, null, arenaHandler, null, cooldownHandler, onTestThread);
    }

    /**
     * Runs the two halves of a migration the way the command does: the write, then the reconciliation and
     * publication.
     */
    private boolean migrate(PersistenceCoordinator coordinator, PluginSnapshot snapshot, List<String> failures) {
        return coordinator.writeTo(destination, snapshot, failures)
                && coordinator.reconcileAndPublish(destination, snapshot, failures);
    }

    private static void installGson() throws Exception {
        final java.lang.reflect.Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        field.set(null, new com.google.gson.GsonBuilder().setPrettyPrinting().create());
    }

    /** Builds a coordinator over a real guild handler, which the reconciliation needs to read. */
    private PersistenceCoordinator withRealGuilds(int count) throws IOException {
        guildHandler = GuildSnapshotRemovalTest.newHandler(count, dataFolder);
        return new PersistenceCoordinator(plugin, gate, guildHandler, arenaHandler, null, cooldownHandler, onTestThread);
    }

    /**
     * A guild id that the destination already holds, standing in for one that the snapshot was taken with
     * and that has since been disbanded.
     */
    private void destinationHolds(String id) throws IOException {
        final List<String> stored = new ArrayList<>();
        stored.add(id);
        Mockito.when(destinationGuilds.getAllGuildIds()).thenReturn(stored);
    }

    // -------------------------------------------------------------------------------------------
    // 1. Guilds disbanded while the migration is in flight
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a guild disbanded after the snapshot is removed from the destination")
    void aGuildDisbandedAfterTheSnapshotIsRemovedFromTheDestination() throws IOException {
        // `/guilds delete` registers a ConfirmAction that runs on a later `/guilds confirm`, so `NotMigrating` on
        // `/guilds delete` does not cover the disband itself. `GuildAdapter` has no delete pass, so
        // without the reconciliation pass below the disband would be undone permanently.
        final PersistenceCoordinator migrating = withRealGuilds(4);

        // Captured while the guild still exists, exactly as the migration does.
        final PluginSnapshot captured = migrating.capture();
        assertEquals(4, captured.getGuilds().size());

        final Guild victim = new ArrayList<>(guildHandler.getGuilds().values()).get(0);
        final String victimId = victim.getId().toString();

        // Disbanded, as `/guilds confirm` would do, after the capture and before the write.
        guildHandler.getGuilds().remove(victim.getId());
        destinationHolds(victimId);

        final List<String> failures = new ArrayList<>();
        assertTrue(migrate(migrating, captured, failures), failures.toString());

        try {
            Mockito.verify(destinationGuilds).deleteGuild(victimId);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("a guild still present is never pruned from the destination")
    void aGuildStillPresentIsNeverPrunedFromTheDestination() throws IOException {
        // The direction that would be catastrophic if it were wrong: pruning a live guild from the destination
        // deletes a guild the owner still has.
        final PersistenceCoordinator migrating = withRealGuilds(3);
        final PluginSnapshot captured = migrating.capture();

        destinationHolds(captured.getGuilds().keySet().iterator().next());

        final List<String> failures = new ArrayList<>();
        assertTrue(migrate(migrating, captured, failures), failures.toString());

        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());
    }

    @Test
    @DisplayName("a guild added after the snapshot is not pruned from the destination")
    void aGuildAddedAfterTheSnapshotIsNotPrunedFromTheDestination() throws IOException {
        // Created after the capture, so absent from the snapshot but present in the handler.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();

        final Guild late = new Guild(UUID.randomUUID());
        guildHandler.addGuild(late);
        destinationHolds(late.getId().toString());

        final List<String> failures = new ArrayList<>();
        assertTrue(migrate(migrating, captured, failures), failures.toString());

        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(late.getId().toString());
    }

    @Test
    @DisplayName("an ordinary save does not prune anything")
    void anOrdinarySaveDoesNotPruneAnything() throws IOException {
        // A regular save that pruned would delete a guild whose row simply had not been written yet.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();

        final Guild disbanded = guildHandler.getGuilds().values().iterator().next();
        final String id = disbanded.getId().toString();
        guildHandler.getGuilds().remove(disbanded.getId());
        destinationHolds(id);

        migrating.writeTo(destination, captured);

        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());
    }

    @Test
    @DisplayName("a guild disbanded mid-migration is reported rather than silently pruned")
    void aGuildDisbandedMidMigrationIsReportedRatherThanSilentlyPruned() throws IOException {
        final PersistenceCoordinator migrating = withRealGuilds(4);
        final PluginSnapshot captured = migrating.capture();

        final List<Guild> present = new ArrayList<>(guildHandler.getGuilds().values());
        final List<String> stored = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            final Guild gone = present.get(i);
            guildHandler.getGuilds().remove(gone.getId());
            stored.add(gone.getId().toString());
        }
        // A survivor, so the pass proves it deletes only what is gone.
        stored.add(present.get(2).getId().toString());
        Mockito.when(destinationGuilds.getAllGuildIds()).thenReturn(stored);

        migrate(migrating, captured, null);

        final org.mockito.InOrder pruned = Mockito.inOrder(destinationGuilds);
        pruned.verify(destinationGuilds).deleteGuild(present.get(0).getId().toString());
        pruned.verify(destinationGuilds).deleteGuild(present.get(1).getId().toString());
    }

    // -------------------------------------------------------------------------------------------
    // 2. Backend transition ordering
    // -------------------------------------------------------------------------------------------

    @Test
    @Timeout(30)
    @DisplayName("the shutdown callback sees the backend published during its drain")
    void theShutdownCallbackSeesTheBackendPublishedDuringItsDrain() throws Exception {
        // Shutdown drains before it reads the adapter to close. Capturing the field on entry would close
        // whichever adapter was current then, and leave the one a migration published during the drain open
        // forever while the plugin went on using it.
        //
        // The migration is a thread holding the write permit. Latches order it against the shutdown thread
        // rather than sleeping and hoping: the migrator publishes, says so, and only then releases the
        // permit, so the shutdown thread cannot reach its drain until the publish has already happened.
        final AtomicReference<DatabaseAdapter> current = new AtomicReference<>(source);
        Mockito.when(plugin.getDatabase()).thenAnswer(invocation -> current.get());
        Mockito.doAnswer(invocation -> {
            current.set(invocation.getArgument(0));
            return null;
        }).when(plugin).setDatabase(Mockito.any(DatabaseAdapter.class));

        final DatabaseAdapter published = Mockito.mock(DatabaseAdapter.class);
        assertTrue(gate.tryAcquireWriter(), "simulate the write permit being held");

        final CountDownLatch publishedNow = new CountDownLatch(1);
        final CountDownLatch migratorDone = new CountDownLatch(1);

        final Thread migrator = new Thread(() -> {
            try {
                plugin.setDatabase(published);
                publishedNow.countDown();
            } finally {
                gate.releaseWriter();
                migratorDone.countDown();
            }
        }, "test-migrator");
        migrator.setDaemon(true);
        migrator.start();

        assertTrue(publishedNow.await(10, TimeUnit.SECONDS), "the migrator should have published");

        final AtomicReference<DatabaseAdapter> closed = new AtomicReference<>();
        final PersistenceCoordinator real = new PersistenceCoordinator(plugin, gate, null, null, null, null, onTestThread);
        real.shutdownFlush(() -> closed.set(plugin.getDatabase()));

        assertTrue(migratorDone.await(10, TimeUnit.SECONDS), "the migrator should have released the permit");
        assertSame(published, closed.get(), "the close callback must read the field when it runs");
        assertSame(published, current.get(), "and the plugin must be left on the published backend");
    }

    @Test
    @DisplayName("publishing then closing survives a close that throws")
    void publishingThenClosingSurvivesACloseThatThrows() {
        // Publish before closing: closing first means an exception there leaves the plugin pointing at a pool that
        // is being torn down.
        final DatabaseAdapter previous = Mockito.mock(DatabaseAdapter.class);
        final DatabaseAdapter next = Mockito.mock(DatabaseAdapter.class);
        final AtomicReference<DatabaseAdapter> current = new AtomicReference<>(previous);
        Mockito.when(plugin.getDatabase()).thenAnswer(invocation -> current.get());
        Mockito.doAnswer(invocation -> {
            current.set(invocation.getArgument(0));
            return null;
        }).when(plugin).setDatabase(Mockito.any(DatabaseAdapter.class));
        Mockito.doThrow(new IllegalStateException("pool shutdown hook failed")).when(previous).close();

        coordinator.publishBackend(next);

        assertSame(next, current.get(), "the new backend must be published");
        Mockito.verify(previous).close();
    }

    @Test
    @DisplayName("publishing closes the backend it replaced, not the new one")
    void publishingClosesTheBackendItReplacedNotTheNewOne() {
        final DatabaseAdapter previous = Mockito.mock(DatabaseAdapter.class);
        final DatabaseAdapter next = Mockito.mock(DatabaseAdapter.class);
        final AtomicReference<DatabaseAdapter> current = new AtomicReference<>(previous);
        Mockito.when(plugin.getDatabase()).thenAnswer(invocation -> current.get());
        Mockito.doAnswer(invocation -> {
            current.set(invocation.getArgument(0));
            return null;
        }).when(plugin).setDatabase(Mockito.any(DatabaseAdapter.class));

        coordinator.publishBackend(next);

        Mockito.verify(previous).close();
        Mockito.verify(next, Mockito.never()).close();
    }

    // -------------------------------------------------------------------------------------------
    // 3. Cooldown failure propagation
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a cooldown IOException refuses the migration")
    void aCooldownIOExceptionRefusesTheMigration() throws IOException {
        // A swallowed cooldown IOException would read as success, and migration would publish an empty cooldown
        // table.
        cooldownHandler.addCooldown(Cooldown.Type.Home, UUID.randomUUID(), 10, java.util.concurrent.TimeUnit.MINUTES);
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();
        assertFalse(captured.getCooldowns().isEmpty(), "the fixture needs a cooldown for this to mean anything");

        Mockito.doThrow(new IOException("cooldowns table is missing"))
                .when(destinationCooldowns).saveCooldowns(Mockito.anyCollection());

        final List<String> failures = new ArrayList<>();
        assertFalse(migrate(migrating, captured, failures),
                "a cooldown failure must not read as success");

        boolean named = false;
        for (String failure : failures) {
            if (failure.startsWith("cooldown:")) {
                named = true;
            }
        }
        assertTrue(named, "the failure should name the cooldown collection: " + failures);
    }

    @Test
    @DisplayName("a cooldown RuntimeException refuses the migration")
    void aCooldownRuntimeExceptionRefusesTheMigration() throws IOException {
        cooldownHandler.addCooldown(Cooldown.Type.Home, UUID.randomUUID(), 10, java.util.concurrent.TimeUnit.MINUTES);
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();
        assertFalse(captured.getCooldowns().isEmpty());

        Mockito.doThrow(new IllegalStateException("HikariDataSource has been closed"))
                .when(destinationCooldowns).saveCooldowns(Mockito.anyCollection());

        assertFalse(migrate(migrating, captured, new ArrayList<String>()));
    }

    @Test
    @DisplayName("a failed cooldown still lets the other collections be written")
    void aFailedCooldownStillLetsTheOtherCollectionsBeWritten() throws IOException {
        // One backend problem should not cost every other kind of data, so the guilds land on the destination even
        // though the migration is refused and a retry does not start from nothing.
        cooldownHandler.addCooldown(Cooldown.Type.Home, UUID.randomUUID(), 10, java.util.concurrent.TimeUnit.MINUTES);
        final PersistenceCoordinator migrating = withRealGuilds(3);
        final PluginSnapshot captured = migrating.capture();

        Mockito.doThrow(new IOException("nope")).when(destinationCooldowns).saveCooldowns(Mockito.anyCollection());

        migrate(migrating, captured, new ArrayList<String>());

        Mockito.verify(destinationGuilds).saveSerialized(Mockito.anyMap());
        Mockito.verify(destinationArenas).saveSerialized(Mockito.anyMap());
    }

    // -------------------------------------------------------------------------------------------
    // 4. The consistency boundary: reconciliation and publication are one main-thread step
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a guild disbanded before the reconciliation is pruned")
    void aGuildDisbandedBeforeTheReconciliationIsPruned() throws IOException {
        // The window this closes. A guild disbanded after the write but before the destination is
        // reconciled would be in the destination with nothing to remove it: `GuildAdapter` upserts by id and
        // has no delete pass, so the next save would write the other guilds over it and leave it standing.
        // The disband would be undone permanently, balance and vaults included.
        final PersistenceCoordinator migrating = withRealGuilds(4);
        final PluginSnapshot captured = migrating.capture();

        final Guild disbanded = new ArrayList<>(guildHandler.getGuilds().values()).get(0);
        migrating.writeTo(destination, captured, new ArrayList<String>());
        destinationHolds(disbanded.getId().toString());

        // Disbanded in the gap, as `/guilds confirm` would do.
        guildHandler.getGuilds().remove(disbanded.getId());

        assertTrue(migrating.reconcileAndPublish(destination, captured, new ArrayList<String>()));

        Mockito.verify(destinationGuilds).deleteGuild(disbanded.getId().toString());
    }

    @Test
    @DisplayName("a guild disbanded after the reconciliation is deleted from the published backend")
    void aGuildDisbandedAfterTheReconciliationIsDeletedFromThePublishedBackend() throws IOException {
        // Why the boundary is placed where it is, and why nothing is needed on the far side.
        //
        // `reconcileAndPublish` reconciles and publishes adjacently on the main thread, so no disband can
        // land between them. A disband immediately afterwards takes the ordinary path: `removeGuild`
        // deletes the row from whatever backend the plugin is now using, which is the published one. So the
        // destination stays correct and nothing needs to be reconciled twice.
        final PersistenceCoordinator migrating = withRealGuilds(3);
        final PluginSnapshot captured = migrating.capture();

        migrating.writeTo(destination, captured, new ArrayList<String>());
        migrating.reconcileAndPublish(destination, captured, new ArrayList<String>());

        final Guild after = new ArrayList<>(guildHandler.getGuilds().values()).get(0);
        final String id = after.getId().toString();
        destinationHolds(id);
        guildHandler.getGuilds().remove(after.getId());

        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(id);

        // What `removeGuild` then does, against the published backend. The gateway is the plugin's own
        // adapter field, which `reconcileAndPublish` has just pointed at the destination.
        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(id);
        destination.getGuildAdapter().deleteGuild(id);
        Mockito.verify(destinationGuilds).deleteGuild(id);
    }

    @Test
    @DisplayName("the reconciliation refuses to run off the main thread")
    void theReconciliationRefusesToRunOffTheMainThread() throws IOException {
        // TaskChain's `postToMain` runs the task inline on the calling thread when the plugin is disabled
        // rather than scheduling it. A migration in flight when the server stops would otherwise reconcile
        // against live state from a pool thread while the main thread ran the shutdown flush over the same
        // map, and every invariant in `reconcileAndPublish` would be void.
        withRealGuilds(2);
        final PersistenceCoordinator offMain =
                new PersistenceCoordinator(plugin, gate, guildHandler, arenaHandler, null, cooldownHandler,
                        () -> false);
        final PluginSnapshot captured = offMain.capture();
        final Guild disbanded = new ArrayList<>(guildHandler.getGuilds().values()).get(0);
        offMain.writeTo(destination, captured, null);
        destinationHolds(disbanded.getId().toString());
        guildHandler.getGuilds().remove(disbanded.getId());

        final List<String> failures = new ArrayList<>();
        assertFalse(offMain.reconcileAndPublish(destination, captured, failures),
                "reconciling off the main thread must be refused, not attempted");

        assertEquals(1, failures.size(), failures.toString());
        assertTrue(failures.get(0).contains("main thread"), failures.get(0));
        Mockito.verify(plugin, Mockito.never()).setDatabase(Mockito.any());
        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());
        Mockito.verify(destination, Mockito.never()).close();
    }

    // -------------------------------------------------------------------------------------------
    // 5. Malformed ids in the destination
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a malformed destination id stops the migration and names the row")
    void aMalformedDestinationIdStopsTheMigrationAndNamesTheRow() throws IOException {
        // A destination can hold an id the plugin did not write: a hand-edited filename, a leftover from an
        // older version, a truncated row. It cannot be matched against the live guilds, and deleting it
        // unasked would destroy a guild the operator may still want.
        //
        // So the migration stops, and says which row and what to do about it. `UUID.fromString` throwing
        // into the generic catch would have produced "guild reconciliation failed" and no way to act on it.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();
        migrating.writeTo(destination, captured, null);

        Mockito.when(destination.getBackend()).thenReturn(DatabaseBackend.MYSQL);
        final List<String> stored = new ArrayList<>();
        stored.add("not-a-uuid");
        stored.add(new ArrayList<>(guildHandler.getGuilds().keySet()).get(0).toString());
        Mockito.when(destinationGuilds.getAllGuildIds()).thenReturn(stored);

        final List<String> failures = new ArrayList<>();
        assertFalse(migrating.reconcileAndPublish(destination, captured, failures),
                "an unmatchable row must stop the migration");

        assertEquals(1, failures.size(), failures.toString());
        assertTrue(failures.get(0).contains("not-a-uuid"), failures.get(0));
        assertTrue(failures.get(0).contains("mysql"), "the message should say which backend: " + failures);
        assertTrue(failures.get(0).contains("run the migration again"),
                "the message should say what to do: " + failures);

        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());
        Mockito.verify(plugin, Mockito.never()).setDatabase(Mockito.any());
    }

    @Test
    @DisplayName("a malformed id aborts the prune rather than deleting the rows after it")
    void aMalformedIdAbortsThePruneRatherThanDeletingTheRowsAfterIt() throws IOException {
        // A half-applied reconciliation is worse than none: the destination would hold some deletions and
        // not others, and the operator would be told the migration failed with no idea which half ran.
        final PersistenceCoordinator migrating = withRealGuilds(3);
        final PluginSnapshot captured = migrating.capture();
        migrating.writeTo(destination, captured, null);

        final List<Guild> present = new ArrayList<>(guildHandler.getGuilds().values());
        final String goneId = present.get(0).getId().toString();
        guildHandler.getGuilds().remove(present.get(0).getId());

        final List<String> stored = new ArrayList<>();
        stored.add(goneId);
        stored.add("garbage");
        Mockito.when(destinationGuilds.getAllGuildIds()).thenReturn(stored);

        assertFalse(migrating.reconcileAndPublish(destination, captured, new ArrayList<String>()));
        Mockito.verify(destinationGuilds, Mockito.never()).deleteGuild(Mockito.anyString());
    }

    // -------------------------------------------------------------------------------------------
    // 6. Cooldowns the destination already holds
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a cooldown the plugin no longer has is removed from the destination")
    void aCooldownThePluginNoLongerHasIsRemovedFromTheDestination() throws IOException {
        // `saveCooldowns` only creates, so without reconciliation a destination that already held
        // cooldowns keeps every one of them. Reachable: the destination is a real backend the operator may
        // have used before, and a failed earlier migration leaves its rows behind.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final UUID staleOwner = UUID.randomUUID();
        Mockito.when(destinationCooldowns.getAllCooldowns()).thenReturn(Arrays.asList(
                new Cooldown(UUID.randomUUID(), Cooldown.Type.Home, staleOwner, System.currentTimeMillis() + 600000L),
                new Cooldown(UUID.randomUUID(), Cooldown.Type.Buffs, UUID.randomUUID(), System.currentTimeMillis() + 600000L)
        ));

        assertTrue(migrate(migrating, migrating.capture(), new ArrayList<String>()));

        Mockito.verify(destinationCooldowns).deleteCooldown(Mockito.argThat(
                cooldown -> cooldown.getCooldownType() == Cooldown.Type.Home
                        && cooldown.getCooldownOwner().equals(staleOwner)));
    }

    @Test
    @DisplayName("a cooldown the plugin still has is kept")
    void aCooldownThePluginStillHasIsKept() throws IOException {
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final UUID owner = UUID.randomUUID();
        cooldownHandler.addCooldown(Cooldown.Type.Home, owner, 10, TimeUnit.MINUTES);

        final PluginSnapshot captured = migrating.capture();
        final Cooldown capturedCooldown = captured.getCooldowns().get(0);
        Mockito.when(destinationCooldowns.getAllCooldowns())
                .thenReturn(Arrays.asList(new Cooldown(UUID.randomUUID(), Cooldown.Type.Home, owner,
                        capturedCooldown.getCooldownExpiry())));

        assertTrue(migrate(migrating, captured, new ArrayList<String>()));

        Mockito.verify(destinationCooldowns, Mockito.never()).deleteCooldown(Mockito.any(Cooldown.class));
    }

    @Test
    @DisplayName("a cooldown of a different type for the same owner is not confused with it")
    void aCooldownOfADifferentTypeForTheSameOwnerIsNotConfusedWithIt() throws IOException {
        // Cooldowns are keyed by type and owner together, so a same-owner match alone would keep the wrong
        // rows alive and delete the right ones.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final UUID owner = UUID.randomUUID();
        cooldownHandler.addCooldown(Cooldown.Type.Home, owner, 10, TimeUnit.MINUTES);

        final PluginSnapshot captured = migrating.capture();
        Mockito.when(destinationCooldowns.getAllCooldowns())
                .thenReturn(Arrays.asList(new Cooldown(UUID.randomUUID(), Cooldown.Type.Join, owner,
                        System.currentTimeMillis() + 600000L)));

        assertTrue(migrate(migrating, captured, new ArrayList<String>()));

        Mockito.verify(destinationCooldowns).deleteCooldown(Mockito.argThat(
                cooldown -> cooldown.getCooldownType() == Cooldown.Type.Join));
    }

    // -------------------------------------------------------------------------------------------
    // 4. Arenas are not affected by the guild reconciliation
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("a live arena is never deleted from the destination")
    void aLiveArenaIsNeverDeletedFromTheDestination() throws IOException {
        // The guild reconciliation added for migration deliberately does not touch arenas, so it cannot make the
        // arena key set disagree with the adapter's delete pass.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final Arena live = new Arena(UUID.randomUUID(), "live");
        arenaHandler.addArena(live);

        final PluginSnapshot captured = migrating.capture();

        final ArgumentCaptor<java.util.Map<String, String>> captor = ArgumentCaptor.forClass(java.util.Map.class);
        migrate(migrating, captured, new ArrayList<String>());
        Mockito.verify(destinationArenas).saveSerialized(captor.capture());

        assertTrue(captor.getValue().containsKey(live.getId().toString()),
                "an arena that exists must reach the destination, or the delete pass removes it from storage");
    }

    @Test
    @DisplayName("an arena removed after the capture is a stale row, not a lost one")
    void anArenaRemovedAfterTheCaptureIsAStaleRowNotALostOne() throws IOException {
        // Migration captures once and writes that snapshot, so an arena removed in the gap is written to the
        // destination and stays there until the next save removes it: a stale row for one interval, rather
        // than a live arena deleted by a stale key set.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final Arena doomed = new Arena(UUID.randomUUID(), "doomed");
        arenaHandler.addArena(doomed);

        final PluginSnapshot captured = migrating.capture();
        arenaHandler.removeArena(doomed);

        migrate(migrating, captured, new ArrayList<String>());

        final ArgumentCaptor<java.util.Map<String, String>> captor = ArgumentCaptor.forClass(java.util.Map.class);
        Mockito.verify(destinationArenas).saveSerialized(captor.capture());

        assertTrue(captor.getValue().containsKey(doomed.getId().toString()),
                "the snapshot is what migration writes; the stale row is corrected by the next save");
    }

    // -------------------------------------------------------------------------------------------
    // 7. Ordering: guilds are written before the reconciliation reads the destination
    // -------------------------------------------------------------------------------------------

    @Test
    @DisplayName("the reconciliation runs after the guilds are written")
    void theReconciliationRunsAfterTheGuildsAreWritten() throws IOException {
        // Reading the destination's ids before writing would reconcile against an empty list and prune nothing.
        final PersistenceCoordinator migrating = withRealGuilds(2);
        final PluginSnapshot captured = migrating.capture();

        final InOrder order = Mockito.inOrder(destinationGuilds);
        migrate(migrating, captured, null);

        order.verify(destinationGuilds).saveSerialized(Mockito.anyMap());
        order.verify(destinationGuilds).getAllGuildIds();
    }

}
