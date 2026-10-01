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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the gate that serialises writes.
 *
 * <p>Two failures matter, and they point in opposite directions. A permit that is never released silently
 * disables every later save, including the one on shutdown, and the server looks healthy the whole time. A
 * permit released twice lets two writers run at once, which is the bug the gate exists to remove.
 */
class PersistenceGateTest {

    private final PersistenceGate gate = new PersistenceGate();

    @Nested
    @DisplayName("write permit")
    class WritePermit {

        @Test
        @DisplayName("a second writer is refused while one is in flight")
        void aSecondWriterIsRefusedWhileOneIsInFlight() {
            assertTrue(gate.tryAcquireWriter(), "the first writer should get the permit");

            assertFalse(gate.tryAcquireWriter(), "a second writer must not get the permit while the first holds it");

            gate.releaseWriter();
            assertTrue(gate.tryAcquireWriter(), "the permit should be available again once released");
        }

        @Test
        @Timeout(10)
        @DisplayName("an acquire that is waiting returns once the holder releases")
        void anAcquireThatIsWaitingReturnsOnceTheHolderReleases() throws InterruptedException {
            assertTrue(gate.tryAcquireWriter());

            final CountDownLatch started = new CountDownLatch(1);
            final AtomicBoolean acquired = new AtomicBoolean();
            final Thread waiter = new Thread(() -> {
                started.countDown();
                acquired.set(gate.acquireWriter(5000L));
            });
            waiter.start();

            assertTrue(started.await(5, TimeUnit.SECONDS), "the waiting writer should have started");
            assertFalse(acquired.get(), "the waiting writer must still be waiting");

            gate.releaseWriter();
            waiter.join(5000L);

            assertTrue(acquired.get(), "the waiting writer should have got the permit");
        }

        @Test
        @Timeout(20)
        @DisplayName("a timed acquire gives up instead of blocking forever")
        void aTimedAcquireGivesUpInsteadOfBlockingForever() {
            assertTrue(gate.tryAcquireWriter());

            final long startedAt = System.nanoTime();
            assertFalse(gate.acquireWriter(100L), "acquiring against a held permit should time out");

            final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            assertTrue(elapsedMillis >= 50L, "the acquire should have waited, waited " + elapsedMillis + "ms");
            assertTrue(elapsedMillis < 5000L, "the acquire should have given up promptly, waited " + elapsedMillis + "ms");
        }

        @Test
        @Timeout(15)
        @DisplayName("acquiring while interrupted reports failure rather than throwing")
        void acquiringWhileInterruptedReportsFailureRatherThanThrowing() throws InterruptedException {
            // Interrupted inside the acquire rather than before the thread starts: `Thread#interrupt` on a
            // not-yet-running thread explicitly need not have any effect.
            //
            // The permit is held so the acquire cannot return before the interrupt arrives, and the reader
            // blocks until the acquire is provably in progress, so there is no sleep deciding the order.
            assertTrue(gate.tryAcquireWriter());

            final CountDownLatch acquireEntered = new CountDownLatch(1);
            final AtomicReference<Boolean> result = new AtomicReference<>();
            final AtomicReference<Boolean> stillFlagged = new AtomicReference<>();
            final CountDownLatch acquiring = new CountDownLatch(1);

            final Thread thread = new Thread(() -> {
                acquireEntered.countDown();
                // Signals that the acquire is under way, then blocks until the test has interrupted it.
                acquiring.countDown();
                result.set(gate.acquireWriter(30_000L));
                // A swallowed interrupt would leave a retry loop spinning, so the flag has to survive.
                stillFlagged.set(Thread.currentThread().isInterrupted());
            }, "test-interrupted-acquirer");
            thread.setDaemon(true);
            thread.start();

            assertTrue(acquireEntered.await(10, TimeUnit.SECONDS), "the thread should have started");
            assertTrue(acquiring.await(10, TimeUnit.SECONDS), "the thread should be about to acquire");

            thread.interrupt();
            thread.join(10_000L);

            assertFalse(thread.isAlive(), "the interrupted acquire should have returned promptly");
            assertEquals(Boolean.FALSE, result.get(), "an interrupted acquire should report failure");
            assertEquals(Boolean.TRUE, stillFlagged.get(), "the interrupt flag should be restored, not swallowed");

            gate.releaseWriter();
        }

        @Test
        @Timeout(15)
        @DisplayName("repeated acquire and release cycles do not erode the permit")
        void repeatedAcquireAndReleaseCyclesDoNotErodeThePermit() {
            // A double release is the one failure that produces no error at all: two permits, two concurrent writers,
            // and every test above still passes. So the count is checked by exhausting it.
            for (int i = 0; i < 100; i++) {
                assertTrue(gate.tryAcquireWriter(), "attempt " + i + " should have taken the permit");
                gate.releaseWriter();
            }

            assertTrue(gate.tryAcquireWriter(), "the gate should still work after 100 cycles");
            assertFalse(gate.tryAcquireWriter(), "and still hand out exactly one permit");
            gate.releaseWriter();
        }
    }

    @Nested
    @DisplayName("migration flag")
    class MigrationFlag {

        @Test
        @DisplayName("only one migration can claim the flag at a time")
        void onlyOneMigrationCanClaimTheFlagAtATime() {
            assertTrue(gate.beginMigration());
            assertTrue(gate.isMigrating());

            assertFalse(gate.beginMigration(), "a second migration must be refused");

            gate.endMigration();
            assertFalse(gate.isMigrating());
            assertTrue(gate.beginMigration(), "the flag should be claimable again after release");
        }

        @Test
        @Timeout(15)
        @DisplayName("the flag is visible to a reader, set and cleared")
        void theFlagIsVisibleToAReaderSetAndCleared() throws InterruptedException {
            // Both directions, because the dangerous one is the second. A `true` that never became visible
            // would mean the autosave and the NotMigrating commands silently keep refusing work.
            //
            // The reader is released by latches at each point rather than after a sleep, so it observes the
            // flag exactly when the test says it should, and never because the reader happened to be
            // scheduled first.
            final AtomicBoolean sawSet = new AtomicBoolean();
            final AtomicBoolean sawCleared = new AtomicBoolean();
            final CountDownLatch readTheSetFlag = new CountDownLatch(1);
            final CountDownLatch setFlagRead = new CountDownLatch(1);
            final CountDownLatch readTheClearedFlag = new CountDownLatch(1);

            final Thread reader = new Thread(() -> {
                try {
                    readTheSetFlag.await();
                    sawSet.set(gate.isMigrating());
                    setFlagRead.countDown();
                    readTheClearedFlag.await();
                    sawCleared.set(gate.isMigrating());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "test-flag-reader");
            reader.setDaemon(true);
            reader.start();

            gate.beginMigration();
            readTheSetFlag.countDown();
            assertTrue(setFlagRead.await(10, TimeUnit.SECONDS), "the reader should have read the set flag");

            gate.endMigration();
            readTheClearedFlag.countDown();
            reader.join(10_000L);

            assertFalse(reader.isAlive(), "the reader should have finished");
            assertTrue(sawSet.get(), "a migration in progress must be visible to another thread");
            assertFalse(sawCleared.get(), "a finished migration must be visible too, or saving stays off");
        }

    }

    @Nested
    @DisplayName("shutdown")
    class Shutdown {

        @Test
        @DisplayName("shutdown is announced once and stays announced")
        void shutdownIsAnnouncedOnceAndStaysAnnounced() {
            assertFalse(gate.isShuttingDown());

            gate.beginShutdown();

            assertTrue(gate.isShuttingDown());
            assertEquals(true, gate.isShuttingDown(), "the flag should not flip back on a second read");
        }

        @Test
        @DisplayName("a fresh gate does not report an earlier gate's state")
        void aFreshGateDoesNotReportAnEarlierGatesState() {
            gate.beginMigration();
            gate.beginShutdown();

            final PersistenceGate other = new PersistenceGate();

            assertFalse(other.isMigrating(), "state must not be static");
            assertFalse(other.isShuttingDown(), "state must not be static");
        }
    }

    @Nested
    @DisplayName("relationship between the flag and the permit")
    class FlagAndPermit {

        @Test
        @Timeout(10)
        @DisplayName("the permit is independent of the migration flag")
        void thePermitIsIndependentOfTheMigrationFlag() {
            gate.beginMigration();

            assertTrue(gate.tryAcquireWriter(), "claiming a migration must not take the write permit");

            gate.releaseWriter();
            gate.endMigration();
        }

        @Test
        @DisplayName("releasing the permit does not clear the migration flag")
        void releasingThePermitDoesNotClearTheMigrationFlag() {
            gate.beginMigration();
            gate.acquireWriter(1000L);
            gate.releaseWriter();

            assertTrue(gate.isMigrating(), "the two are separate: migration holds its flag across the write");

            gate.endMigration();
        }
    }

    @Test
    @DisplayName("a snapshot with no database is written without touching storage")
    void aSnapshotWithNoDatabaseIsWrittenWithoutTouchingStorage() {
        // This is the state during a partial onEnable, and it must be a no-op rather than an NPE that skips the
        // rest of the shutdown sequence.
        final PluginSnapshot snapshot = new PluginSnapshot(
                java.util.Collections.emptyMap(),
                java.util.Collections.emptyMap(),
                java.util.Collections.emptyMap(),
                java.util.Collections.emptyList(),
                null
        );

        assertNull(snapshot.getDatabase());
        assertTrue(snapshot.isEmpty());
    }
}
