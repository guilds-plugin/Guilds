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
package me.glaremasters.guilds.commands.console;

import co.aikar.commands.CommandIssuer;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.persistence.PersistenceCoordinator;
import me.glaremasters.guilds.persistence.PersistenceGate;
import me.glaremasters.guilds.persistence.PluginSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link MigrationOutcome}, which owns the write permit, the destination adapter and the migration
 * flag for the duration of a migration.
 *
 * <p>Every defect this file pins is one where something escaped and was cleaned up by nothing: a permit
 * never returned, a half-built connection pool never closed, or a destination closed after it had been
 * published. Those are invisible in a passing test suite unless the cleanup path itself is exercised, which
 * is why this drives the outcome object directly rather than through the command.
 *
 * <p>No Bukkit is needed: {@code CommandIssuer}, {@code Guilds} and {@code DatabaseAdapter} are all
 * interfaces or mockable, and the coordinator is a real object with its handler arguments left null.
 */
class MigrationOutcomeTest {

    private PersistenceGate gate;
    private Guilds plugin;
    private PersistenceCoordinator coordinator;
    private DatabaseAdapter source;
    private DatabaseAdapter destination;
    private PluginSnapshot snapshot;
    private CommandIssuer issuer;
    private Thread mainThread;

    @BeforeEach
    void setUp() {
        mainThread = Thread.currentThread();
        gate = new PersistenceGate();
        plugin = Mockito.mock(Guilds.class);
        source = Mockito.mock(DatabaseAdapter.class);
        destination = Mockito.mock(DatabaseAdapter.class);
        issuer = Mockito.mock(CommandIssuer.class);

        Mockito.when(plugin.getDatabase()).thenReturn(source);
        Mockito.doAnswer(invocation -> {
            plugin.getDatabase();
            return null;
        }).when(plugin).setDatabase(Mockito.any());

        snapshot = new PluginSnapshot(
                Collections.singletonMap(UUID.randomUUID().toString(), "{}"),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                source
        );

        // Reports whatever the test says the calling thread is, which is how the main-thread guard is
        // exercised without a server.
        coordinator = coordinatorReporting(true);
    }

    private PersistenceCoordinator coordinatorReporting(boolean mainThreadIsCaller) {
        return new PersistenceCoordinator(plugin, gate, null, null, null, null,
                () -> mainThreadIsCaller);
    }

    private void write(MigrationOutcome outcome) {
        outcome.write(plugin, gate, coordinator, DatabaseBackend.MYSQL, snapshot);
    }

    private void publish(MigrationOutcome outcome) {
        outcome.publish(coordinator, snapshot, gate, issuer, DatabaseBackend.MYSQL, 7);
    }

    private void publishReportingCallerIsNotMain(MigrationOutcome outcome) {
        outcome.publish(coordinatorReporting(false), snapshot, gate, issuer, DatabaseBackend.MYSQL, 7);
    }

    /**
     * The message the operator was told, or null if they were told nothing.
     *
     * Read off the invocation list rather than a captor, because the one-argument and the
     * placeholder-argument forms of `sendInfo` are separate calls and a captor has to pick one.
     */
    private Messages reportedMessage() {
        Messages reported = null;
        for (org.mockito.invocation.Invocation invocation : Mockito.mockingDetails(issuer).getInvocations()) {
            if ("sendInfo".equals(invocation.getMethod().getName())
                    && invocation.getArguments().length > 0
                    && invocation.getArguments()[0] instanceof Messages) {
                reported = (Messages) invocation.getArguments()[0];
            }
        }
        return reported;
    }

    // ---------------------------------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a published destination is not closed afterwards")
    void aPublishedDestinationIsNotClosedAfterwards() throws IOException {
        stubWorkingDestination();
        // Closing a published destination leaves every later save, and every guild deletion, throwing
        // "HikariDataSource has been closed" while the operator was told the migration worked. On the JSON
        // backend `close` is a no-op, so this survives manual testing on the default backend and only bites
        // on SQL.
        final MigrationOutcome outcome = new MigrationOutcome();
        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenReturn(destination);

        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        Mockito.verify(destination, Mockito.never()).close();
    }

    @Test
    @DisplayName("a destination that was written but not published is closed")
    void aDestinationThatWasWrittenButNotPublishedIsClosed() throws IOException {
        final MigrationOutcome outcome = new MigrationOutcome();
        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenReturn(destination);
        Mockito.when(destination.getGuildAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.guild.GuildAdapter.class));
        Mockito.when(destination.getArenaAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.arenas.ArenaAdapter.class));
        Mockito.when(destination.getCooldownAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.cooldowns.CooldownAdapter.class));
        Mockito.when(destination.getChallengeAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.challenges.ChallengeAdapter.class));

        write(outcome);
        // The reconcile is refused, so nothing is published and the pool is still ours to close.
        publishReportingCallerIsNotMain(outcome);
        outcome.closeUnpublished();

        Mockito.verify(destination).close();
    }

    /** Runs `publish` with a coordinator that refuses to reconcile, so nothing is published. */


    @Test
    @DisplayName("a destination opened but never written to is still closed")
    void aDestinationOpenedButNeverWrittenToIsStillClosed() throws IOException {
        // A migration that failed at the connection check must not leak the pool it opened.
        final MigrationOutcome outcome = new MigrationOutcome();
        final DatabaseAdapter dead = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenReturn(dead);
        Mockito.when(dead.isConnected()).thenReturn(false);

        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        Mockito.verify(dead).close();
        assertEquals(Messages.MIGRATE__CONNECTION_FAILED, reportedMessage());
    }

    @Test
    @DisplayName("nothing is closed when the destination was never opened")
    void nothingIsClosedWhenTheDestinationWasNeverOpened() throws IOException {
        final MigrationOutcome outcome = new MigrationOutcome();
        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenThrow(new IllegalArgumentException("same backend"));

        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        assertEquals(Messages.MIGRATE__SAME_BACKEND, reportedMessage());
        assertTrue(gate.tryAcquireWriter(), "no permit should have been taken");
    }

    @Test
    @DisplayName("a migration onto the storage already in use is refused before anything is opened")
    void aMigrationOntoTheStorageAlreadyInUseIsRefusedBeforeAnythingIsOpened() throws IOException {
        // `MYSQL` and `MARIADB` are built from one set of properties, so this is the same tables twice. The
        // migration would write over the rows it is reading, and a failure partway through would have
        // deleted cooldowns out of the live tables with no second copy to restore from. Refused before the
        // pool is opened, so there is nothing to clean up either.
        Mockito.when(source.sharesStorageWith(DatabaseBackend.MYSQL)).thenReturn(true);

        final MigrationOutcome outcome = new MigrationOutcome();
        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        Mockito.verify(source, Mockito.never()).cloneWith(Mockito.any());
        assertEquals(Messages.MIGRATE__FAILED, reportedMessage());
        assertTrue(gate.tryAcquireWriter(), "no permit should have been taken");
    }

    // ---------------------------------------------------------------------------------------
    // Permit
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("the permit is returned after a successful migration")
    void thePermitIsReturnedAfterASuccessfulMigration() throws IOException {
        final MigrationOutcome outcome = new MigrationOutcome();
        stubWorkingDestination();

        write(outcome);
        assertFalse(gate.tryAcquireWriter(), "the migration should be holding the permit");

        publish(outcome);
        assertTrue(gate.tryAcquireWriter(), "publish must hand the permit back");
    }

    @Test
    @DisplayName("the permit is returned when the write throws an Error")
    void thePermitIsReturnedWhenTheWriteThrowsAnError() throws IOException {
        // `writeTo` catches IOException and RuntimeException per collection, so what reaches `write` is an
        // Error. Letting it out aborts the chain, and the chain's remaining step is the one that releases
        // the permit and the migration flag, so both would stay held for the rest of the session: every
        // later save skips and every NotMigrating command is refused.
        final MigrationOutcome outcome = new MigrationOutcome();
        stubWorkingDestination();
        final me.glaremasters.guilds.database.guild.GuildAdapter failing =
                Mockito.mock(me.glaremasters.guilds.database.guild.GuildAdapter.class);
        Mockito.when(destination.getGuildAdapter()).thenReturn(failing);
        Mockito.doThrow(new OutOfMemoryError("five thousand guilds to SQL"))
                .when(failing).saveSerialized(Mockito.anyMap());

        write(outcome);
        publish(outcome);

        assertTrue(gate.tryAcquireWriter(), "an Error must not strand the permit");
        assertEquals(Messages.MIGRATE__FAILED, reportedMessage());
    }

    @Test
    @DisplayName("no permit is taken when the destination cannot be opened")
    void noPermitIsTakenWhenTheDestinationCannotBeOpened() throws IOException {
        final MigrationOutcome outcome = new MigrationOutcome();
        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenThrow(new IOException("connection refused"));

        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        assertTrue(gate.tryAcquireWriter(), "a permit must not have been taken");
        assertEquals(Messages.MIGRATE__CONNECTION_FAILED, reportedMessage());
    }

    @Test
    @DisplayName("no permit is taken when another save holds it for too long")
    void noPermitIsTakenWhenAnotherSaveHoldsItForTooLong() throws IOException {
        stubWorkingDestination();
        assertTrue(gate.tryAcquireWriter(), "simulate a save already running");

        final MigrationOutcome outcome = new MigrationOutcome();
        write(outcome);
        publish(outcome);
        outcome.closeUnpublished();

        assertEquals(Messages.MIGRATE__BUSY, reportedMessage());
        assertFalse(gate.tryAcquireWriter(), "the permit it never took must still be held by the save");
        Mockito.verify(destination).close();
        gate.releaseWriter();
    }

    // ---------------------------------------------------------------------------------------
    // The main-thread guard
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("publishing from a worker refuses instead of reconciling off the main thread")
    void publishingFromAWorkerRefusesInsteadOfReconcilingOffTheMainThread() throws IOException {
        stubWorkingDestination();
        // The guard exists because TaskChain's `postToMain` runs the task inline on the calling thread when
        // the plugin is disabled, rather than scheduling it. A migration in flight when the server stops
        // would otherwise reconcile against live state from a pool thread while the main thread ran the
        // shutdown flush over the same map, and every invariant in `reconcileAndPublish` would be void.
        final MigrationOutcome outcome = new MigrationOutcome();
        stubWorkingDestination();

        write(outcome);
        publishReportingCallerIsNotMain(outcome);

        assertEquals(Messages.MIGRATE__FAILED, reportedMessage());
        Mockito.verify(plugin, Mockito.never()).setDatabase(Mockito.any());
        assertTrue(gate.tryAcquireWriter(), "the permit must still come back");
        Mockito.verify(source, Mockito.never()).close();
    }

    // ---------------------------------------------------------------------------------------
    // Reporting
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a successful migration reports the guild count")
    void aSuccessfulMigrationReportsTheGuildCount() throws IOException {
        final MigrationOutcome outcome = new MigrationOutcome();
        stubWorkingDestination();

        write(outcome);
        publish(outcome);

        Mockito.verify(issuer).sendInfo(Mockito.eq(Messages.MIGRATE__COMPLETE), Mockito.eq("{amount}"),
                Mockito.eq("7"));
    }

    private void stubWorkingDestination() throws IOException {
        final me.glaremasters.guilds.database.guild.GuildAdapter guilds =
                Mockito.mock(me.glaremasters.guilds.database.guild.GuildAdapter.class);
        Mockito.when(guilds.getAllGuildIds()).thenReturn(Collections.<String>emptyList());
        final me.glaremasters.guilds.database.cooldowns.CooldownAdapter cooldowns =
                Mockito.mock(me.glaremasters.guilds.database.cooldowns.CooldownAdapter.class);
        Mockito.when(cooldowns.getAllCooldowns()).thenReturn(Collections.emptyList());

        Mockito.when(source.cloneWith(DatabaseBackend.MYSQL)).thenReturn(destination);
        Mockito.when(destination.isConnected()).thenReturn(true);
        Mockito.when(destination.getGuildAdapter()).thenReturn(guilds);
        Mockito.when(destination.getArenaAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.arenas.ArenaAdapter.class));
        Mockito.when(destination.getCooldownAdapter()).thenReturn(cooldowns);
        Mockito.when(destination.getChallengeAdapter()).thenReturn(Mockito.mock(
                me.glaremasters.guilds.database.challenges.ChallengeAdapter.class));
    }
}
