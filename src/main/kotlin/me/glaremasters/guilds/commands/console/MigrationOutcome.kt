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

        // Refused before the pool opens. See `DatabaseAdapter#sharesStorageWith` for what counts as the
        // same tables.
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
     * Reconciles the destination and publishes it, and releases the write permit. Runs on the main thread.
     *
     * <p>The main thread is the consistency boundary rather than a convention. Guild disbanding happens
     * there, so reconciling and publishing adjacently on that thread leaves no instant at which the
     * destination can be brought into line and then have a guild removed from under it. A worker would
     * leave that window open, and `GuildAdapter` has no delete pass, so a guild caught in it would be
     * resurrected in the destination permanently.
     *
     * @param coordinator    used for the reconciliation
     * @param snapshot       the state the destination was written from
     * @param gate           the shared gate
     * @param issuer         told what happened
     * @param backend        the backend being migrated to, for the log
     * @param guildCount     reported to the operator on success
     */
    fun publish(
        coordinator: PersistenceCoordinator,
        snapshot: PluginSnapshot,
        gate: PersistenceGate,
        issuer: CommandIssuer,
        backend: DatabaseBackend,
        guildCount: Int
    ) {
        try {
            val adapter = destination
            val alreadyFailed = failure
            if (adapter == null || alreadyFailed != null) {
                issuer.sendInfo(alreadyFailed ?: Messages.MIGRATE__FAILED)
                return
            }

            val failures = ArrayList<String>()
            if (!coordinator.reconcileAndPublish(adapter, snapshot, failures)) {
                LoggingUtils.severe(
                    "Migration to ${backend.backendName} could not be reconciled: ${failures.joinToString("; ")}",
                )
                issuer.sendInfo(Messages.MIGRATE__FAILED)
                return
            }

            published = true
            issuer.sendInfo(Messages.MIGRATE__COMPLETE, "{amount}", guildCount.toString())
        } finally {
            if (permitHeld) {
                gate.releaseWriter()
            }
        }
    }

    /**
     * Closes a destination that was opened but never published.
     *
     * <p>Called after {@link #publish} has reported, so it runs whether publication succeeded or not. A
     * destination left open is a leaked connection pool and, on a SQL backend, a pool's worth of open
     * connections to a server the operator has no reason to be connected to.
     */
    fun closeUnpublished() {
        // A published destination is the plugin's live backend. Closing it here would leave every later
        // save and every guild deletion throwing "HikariDataSource has been closed", while the operator
        // had been told the migration worked. On the JSON backend `close` is a no-op, which is why this
        // survives manual testing and only bites on SQL.
        if (published) {
            return
        }

        val adapter = destination ?: return
        try {
            adapter.close()
        } catch (ex: RuntimeException) {
            LoggingUtils.severe("Migration left an unused database connection open.", ex)
        }
    }

    private fun fail(message: Messages, cause: Throwable?, doing: String) {
        LoggingUtils.severe("Migration failed while $doing. The previous backend is still in use.", cause)
        failure = message
    }

    private companion object {
        /** Bounded, so a wedged save cannot make the command hang indefinitely. */
        const val WRITE_PERMIT_TIMEOUT_MS = 10_000L
    }
}
