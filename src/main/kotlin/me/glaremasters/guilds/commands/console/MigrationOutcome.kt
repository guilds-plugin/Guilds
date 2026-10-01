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
package me.glaremasters.guilds.commands.console

import co.aikar.commands.CommandIssuer
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.database.DatabaseAdapter
import me.glaremasters.guilds.database.DatabaseBackend
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.persistence.PersistenceCoordinator
import me.glaremasters.guilds.persistence.PersistenceGate
import me.glaremasters.guilds.persistence.PluginSnapshot
import me.glaremasters.guilds.utils.LoggingUtils

/**
 * Carries one migration across its two threads: the write on a worker, the reconciliation and publication
 * on the main thread.
 *
 * <p>Holding the outcome in one object keeps the sequence a single call per thread and gives the write
 * permit and the destination adapter a single owner. Split across the chain's lambdas, the two halves could
 * be reordered or one run without the other, and every path out of the worker step would have to remember
 * to release a permit it no longer obviously held.
 */
internal class MigrationOutcome {

    private var destination: DatabaseAdapter? = null
    private var failure: Messages? = null
    private var permitHeld = false
    private var published = false
    private var catchUpSnapshot: PluginSnapshot? = null
    private var persistedGuildCount = 0

    /**
     * Opens the destination and writes the snapshot into it. Runs on a worker.
     *
     * <p>Never throws. TaskChain skips the rest of a chain when a step throws, and the rest of this chain is
     * the step that publishes, cleans up and reports, so propagating would strand the write permit and leave
     * the migration flag set.
     *
     * @param guilds      the plugin, for the backend being migrated from
     * @param gate        the shared gate
     * @param coordinator used to write the snapshot
     * @param backend     the backend to migrate to
     * @param snapshot    the state captured on the main thread
     */
    fun write(
        guilds: Guilds,
        gate: PersistenceGate,
        coordinator: PersistenceCoordinator,
        backend: DatabaseBackend,
        snapshot: PluginSnapshot
    ) {
        try {
            writeInternal(guilds, gate, coordinator, backend, snapshot)
        } catch (ex: Throwable) {
            // `writeTo` catches `IOException` and `RuntimeException` per collection, so what reaches here
            // is an `Error`: an OutOfMemoryError writing five thousand guilds to SQL is the realistic one.
            //
            // Letting it out would abort the chain, and the chain's remaining step is the one that
            // publishes, reports and releases. The permit and the migration flag would both stay held, and
            // every later save, including the one on shutdown, would skip. The flag being stuck is the
            // failure this whole branch exists to remove.
            fail(Messages.MIGRATE__FAILED, ex, "writing the destination")
        }
    }

    private fun writeInternal(
        guilds: Guilds,
        gate: PersistenceGate,
        coordinator: PersistenceCoordinator,
        backend: DatabaseBackend,
        snapshot: PluginSnapshot
    ) {
        // Before anything is opened, and before the storage check, so a backend the operator already has
        // gets the message that names it rather than one about storage.
        if (guilds.database.backend == backend) {
            fail(Messages.MIGRATE__SAME_BACKEND, null, "the destination is the backend already in use")
            return
        }

        // Refused before the pool opens. See `DatabaseAdapter#sharesStorageWith`.
        if (guilds.database.sharesStorageWith(backend)) {
            LoggingUtils.severe(
                "Migration from ${guilds.database.backend.backendName} to ${backend.backendName} was refused:" +
                    " both are configured against the same host, database and table prefix, so they are the" +
                    " same rows. Point the storage settings at a different database or server, reload, and" +
                    " migrate again.",
            )
            fail(Messages.MIGRATE__FAILED, null, "the destination is the storage already in use")
            return
        }

        val adapter = try {
            guilds.database.cloneWith(backend)
        } catch (ex: IllegalArgumentException) {
            fail(Messages.MIGRATE__SAME_BACKEND, ex, "the destination is the backend already in use")
            return
        } catch (ex: Exception) {
            fail(Messages.MIGRATE__CONNECTION_FAILED, ex, "opening the destination")
            return
        }

        destination = adapter

        if (!adapter.isConnected) {
            fail(Messages.MIGRATE__CONNECTION_FAILED, null, "the destination reported that it is not connected")
            return
        }

        if (!gate.acquireWriter(WRITE_PERMIT_TIMEOUT_MS)) {
            fail(Messages.MIGRATE__BUSY, null, "another save is still running")
            return
        }
        permitHeld = true

        if (gate.isShuttingDown) {
            fail(Messages.MIGRATE__FAILED, null, "the server started shutting down")
            return
        }

        val failures = ArrayList<String>()
        if (!coordinator.writeTo(adapter, snapshot, failures)) {
            LoggingUtils.severe("Migration to ${backend.backendName} could not write every collection: ${failures.joinToString("; ")}")
            fail(Messages.MIGRATE__FAILED, null, "writing the destination")
            return
        }

        // Here rather than in `publish`, because it is the superlinear part of a migration and does not
        // need the main thread. See `PersistenceCoordinator#reconcileCooldowns`.
        if (!coordinator.reconcileCooldowns(adapter, snapshot, failures)) {
            LoggingUtils.severe(
                "Migration to ${backend.backendName} could not reconcile cooldowns: ${failures.joinToString("; ")}",
            )
            fail(Messages.MIGRATE__FAILED, null, "reconciling the destination's cooldowns")
            return
        }
    }

    /**
     * Reconciles the destination and publishes it. Runs on the main thread.
     *
     * <p>Main thread, and adjacent to nothing else, so no disband can land between reconciling the
     * destination and publishing it. See `PersistenceCoordinator#reconcileAndPublish`.
     *
     * <p>Says nothing to the operator, and keeps holding the write permit. The destination still holds the
     * snapshot rather than the plugin's current state, and [report] runs only once [writeCatchUp] has put
     * that right.
     *
     * @param coordinator used for the reconciliation
     * @param snapshot    the state the destination was written from
     * @param backend     the backend being migrated to, for the log
     */
    fun publish(
        coordinator: PersistenceCoordinator,
        snapshot: PluginSnapshot,
        backend: DatabaseBackend
    ) {
        // Total, like `write`. A throw here aborts the chain, and the step that returns the permit and the
        // migration flag is the last one in it, so both would be held for the rest of the session.
        try {
            val adapter = destination
            if (adapter == null || failure != null) {
                return
            }

            val failures = ArrayList<String>()
            if (!coordinator.reconcileAndPublish(adapter, snapshot, failures)) {
                LoggingUtils.severe(
                    "Migration to ${backend.backendName} could not be reconciled: ${failures.joinToString("; ")}",
                )
                fail(Messages.MIGRATE__FAILED, null, "reconciling the destination")
                return
            }

            published = true
        } catch (ex: Throwable) {
            fail(Messages.MIGRATE__FAILED, ex, "reconciling the destination")
        }
    }

    /**
     * Captures what the migration window changed, so it can be written to the destination. Main thread.
     *
     * <p>The autosave is gated off for the whole migration, so this write is the only thing that persists
     * what changed during it.
     *
     * <p>Total, like [write]. Refuses off the main thread — TaskChain runs a sync step inline on the
     * calling thread when the plugin is disabled.
     */
    fun catchUp(coordinator: PersistenceCoordinator, gate: PersistenceGate) {
        if (!published) {
            return
        }
        if (gate.isShuttingDown) {
            // `shutdownFlush` writes the live backend, which is the published destination, so this would be
            // the same write twice, against a shutdown that is already waiting on the permit.
            return
        }
        // Insurance rather than a reachable path: the two are adjacent sync steps, so a capture here is on
        // the main thread by construction. Kept because TaskChain's `postToMain` runs a task inline on the
        // calling thread when the plugin is disabled, and "by construction" is the kind of thing that
        // changes.
        if (!coordinator.isOnMainThread) {
            LoggingUtils.severe(
                "Migration could not save the changes made while it ran: the capture would not have been on" +
                    " the server's main thread. The destination holds the state from the start of the" +
                    " migration; the next save will bring it level.",
            )
            failAfterPublish("capturing the changes made during the migration")
            return
        }
        catchUpSnapshot = try {
            coordinator.capture()
        } catch (ex: Throwable) {
            LoggingUtils.severe("Migration could not read plugin data to save the changes made during it.", ex)
            failAfterPublish("capturing the changes made during the migration")
            null
        }
    }

    /**
     * Writes what [catchUp] captured into the now-live destination. Runs on a worker.
     *
     * <p>Never throws, for the reason [write] gives.
     */
    fun writeCatchUp(coordinator: PersistenceCoordinator, gate: PersistenceGate) {
        val snapshot = catchUpSnapshot
        if (!published || snapshot == null || gate.isShuttingDown) {
            return
        }
        persistedGuildCount = snapshot.guilds.size
        try {
            val failures = ArrayList<String>()
            if (!coordinator.writeTo(destination!!, snapshot, failures)) {
                LoggingUtils.severe(
                    "Migration could not save the changes made while it ran: ${failures.joinToString("; ")}",
                )
                failAfterPublish("saving the changes made during the migration")
            }
        } catch (ex: Throwable) {
            failAfterPublish("saving the changes made during the migration")
        }
    }

    /**
     * Tells the operator how it went. Main thread, and only after the changes made during the migration have
     * been written.
     *
     * @param issuer  told what happened
     * @param backend the backend migrated to, for the log
     */
    fun report(issuer: CommandIssuer, backend: DatabaseBackend) {
        val alreadyFailed = failure
        if (alreadyFailed != null) {
            issuer.sendInfo(alreadyFailed)
            return
        }
        if (!published) {
            issuer.sendInfo(Messages.MIGRATE__FAILED)
            return
        }
        LoggingUtils.info("Migration to ${backend.backendName} completed.")
        issuer.sendInfo(Messages.MIGRATE__COMPLETE, "{amount}", persistedGuildCount.toString())
    }

    /**
     * Closes a destination that was opened but never published, and gives the write permit back. Main
     * thread, and last: the permit covers the catch-up write as well as the migration's own.
     *
     * <p>Runs whether the migration succeeded or not. A destination left open is a leaked connection pool
     * and, on a SQL backend, a pool's worth of open connections to a server the operator has no reason to be
     * connected to.
     *
     * @param gate the shared gate
     */
    fun finish(gate: PersistenceGate) {
        // A published destination is the live backend; closing it would break every later save.
        val adapter = if (published) null else destination
        if (adapter != null) {
            try {
                adapter.close()
            } catch (ex: RuntimeException) {
                LoggingUtils.severe("Migration left an unused database connection open.", ex)
            }
        }

        // Not inside the branch above: a published migration held the permit through its catch-up write.
        if (permitHeld) {
            permitHeld = false
            gate.releaseWriter()
        }
    }

    private fun fail(message: Messages, cause: Throwable?, doing: String) {
        LoggingUtils.severe("Migration failed while $doing. The previous backend is still in use.", cause)
        failure = message
    }

    /**
     * Fails after the destination is already the live backend, which `migrate.failed` denies: the plugin is
     * no longer on the old one and its pool is closed, and an operator who believes otherwise will reboot
     * back onto it.
     */
    private fun failAfterPublish(doing: String) {
        LoggingUtils.severe(
            "Migration could not $doing after the new backend was published. The new backend is in use and" +
                " the previous one is closed; the changes made during the migration are not on it.",
        )
        failure = Messages.MIGRATE__PUBLISHED_UNSAVED
    }

    private companion object {
        /** Bounded, so a wedged save cannot make the command hang indefinitely. */
        const val WRITE_PERMIT_TIMEOUT_MS = 10_000L
    }
}
