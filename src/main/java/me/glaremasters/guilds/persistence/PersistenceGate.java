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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The single point of serialisation for everything that writes plugin data to storage.
 *
 * <p>Three writers exist: the autosave task, the console migration command, and the shutdown
 * flush. They used to be independent, and two of them could be inside the same write at once:
 * {@code scheduleAsyncRepeatingTask} re-arms its timer without waiting for the running copy to
 * finish, and the {@code isMigrating} flag that migration set was a check-then-act on a plain
 * {@code boolean}, so an autosave that had already passed the check kept writing to the adapter
 * that migration was closing underneath it.
 *
 * <p>Ownership is a permit rather than a lock. A {@link Semaphore} hands out a permit that any
 * thread may return, while {@link java.util.concurrent.locks.ReentrantLock} can only be unlocked by
 * the thread that took it. That distinction matters here: a write is acquired on one thread and
 * released on another (the autosave acquires before snapshotting on the main thread and releases at
 * the end of the async worker), and a migration holds its permit across a hop back to the main
 * thread to swap the backend. Neither is expressible with a thread-owned lock.
 *
 * <p>Every method here is safe to call from any thread.
 */
public final class PersistenceGate {

    /**
     * The single write permit. Fair, so a waiting migration is not starved by a steady autosave.
     */
    private final Semaphore writerPermit = new Semaphore(1, true);

    /**
     * Whether a backend migration is in progress.
     *
     * <p>This replaces {@code GuildHandler#migrating}, which was a plain {@code boolean} written on
     * a TaskChain thread and read both by the autosave worker and by the ACF command condition. With
     * no happens-before edge between them, a reader was permitted to observe a stale value for an
     * unbounded time, which is the opposite of what a "don't write while I migrate" flag needs.
     */
    private final AtomicBoolean migrating = new AtomicBoolean(false);

    /**
     * Set once shutdown starts, so a migration that is still queued reports failure instead of
     * swapping the backend after the connection pool has been closed.
     */
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    /**
     * Whether a migration is currently running.
     *
     * @return true while a migration holds the gate
     */
    public boolean isMigrating() {
        return migrating.get();
    }

    /**
     * Whether shutdown has begun.
     *
     * @return true once {@link #beginShutdown()} has been called
     */
    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    /**
     * Claims the right to migrate the storage backend.
     *
     * <p>Separate from the write permit on purpose. A migration needs the {@code isMigrating} flag to
     * be visible for its whole duration, including the window before it takes the permit, because
     * commands gated on it are refused for that entire window.
     *
     * @return true when the caller now owns the migration, false when one is already running
     */
    public boolean beginMigration() {
        return migrating.compareAndSet(false, true);
    }

    /**
     * Releases the migration flag.
     *
     * <p>Must be called from a {@code finally} block. Leaving it set is not a cosmetic failure: the
     * autosave skips while it is true, so a stuck flag silently stops all periodic saving for the
     * rest of the session, and every command carrying the {@code NotMigrating} condition is refused.
     */
    public void endMigration() {
        migrating.set(false);
    }

    /**
     * Marks the plugin as shutting down.
     *
     * <p>Called once from {@code onDisable}, before the final flush, so an in-flight migration stops
     * rather than swapping a backend behind a closing connection pool.
     */
    public void beginShutdown() {
        shuttingDown.set(true);
    }

    /**
     * Attempts to take the write permit without blocking.
     *
     * <p>The autosave uses this and skips its run when it returns false. Skipping rather than queueing
     * is deliberate: a snapshot of a large guild set is not small, and a save that overruns its
     * interval must not build a backlog of them. Skipping costs at most one interval of staleness, and
     * the next tick writes everything anyway.
     *
     * @return true when the permit was taken and must later be given back with {@link #releaseWriter()}
     */
    public boolean tryAcquireWriter() {
        return writerPermit.tryAcquire();
    }

    /**
     * Takes the write permit, waiting up to a timeout.
     *
     * <p>Used by migration and by the shutdown flush, both of which have to write and would otherwise
     * silently skip. The timeout is what keeps shutdown bounded: if a write is genuinely stuck, the
     * server still stops.
     *
     * @param timeoutMillis how long to wait for the permit
     * @return true when the permit was taken, false on timeout or interruption
     */
    public boolean acquireWriter(long timeoutMillis) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }

        try {
            return writerPermit.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Restore the flag so a caller looping on this gate does not spin.
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Returns the write permit.
     *
     * <p>Must be called from a {@code finally} block. A lost permit is permanent: every later save,
     * including the shutdown flush, would skip.
     */
    public void releaseWriter() {
        writerPermit.release();
    }
}
