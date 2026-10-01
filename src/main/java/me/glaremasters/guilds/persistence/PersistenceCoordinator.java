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
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Captures plugin state on the main thread and writes it off it.
 *
 * <p>The split exists because the two halves have opposite requirements. Reading state needs the main
 * thread: the guild map, the arena map and the challenge set are plain {@link java.util.HashMap} and
 * {@link java.util.HashSet} fields written by command handlers and war tasks, and guild vaults are live
 * Bukkit {@link org.bukkit.inventory.Inventory} objects a player may be clicking in while a save
 * runs. Writing needs any thread but the main one: a save is a serialisation per guild plus a file
 * write or a database round trip, and doing that inline stalls the tick.
 *
 * <p>So {@link #capture} runs on the main thread and produces a {@link PluginSnapshot} of serialised
 * bytes, and {@link #write} runs on a worker and only moves those bytes to storage.
 *
 * <p>The four collections are written independently, so a backend that throws for one of them costs
 * the operator that kind of data and not the rest. That is the behaviour
 * {@code Guilds#savePluginData} already documented for shutdown.
 */
public final class PersistenceCoordinator {

    /**
     * How long the shutdown flush waits for an in-flight save before writing anyway.
     *
     * <p>Bounded on purpose. A save wedged on a dead database connection must not stop the server from
     * stopping, and an unbounded block inside {@code onDisable} is how a plugin hangs a shutdown.
     */
    private static final long SHUTDOWN_DRAIN_TIMEOUT_MILLIS = 5000L;

    /**
     * The budget handed to a capture that must finish in one pass.
     *
     * <p>Deliberately not {@code Long.MAX_VALUE}: {@code CaptureSession#step} adds this to
     * {@code System.nanoTime()}, which would overflow to a negative deadline, expire immediately, and turn
     * the capture into a hard failure. A day is about 292 years of budget, which is the same as unbounded
     * for any capture that will finish and cannot overflow for another century.
     */
    private static final long UNBOUNDED_BUDGET_NANOS = TimeUnit.DAYS.toNanos(1L);

    private final Guilds plugin;
    private final PersistenceGate gate;
    private final GuildHandler guildHandler;
    private final ArenaHandler arenaHandler;
    private final ChallengeHandler challengeHandler;
    private final CooldownHandler cooldownHandler;

    public PersistenceCoordinator(
            @NotNull Guilds plugin,
            @NotNull PersistenceGate gate,
            @Nullable GuildHandler guildHandler,
            @Nullable ArenaHandler arenaHandler,
            @Nullable ChallengeHandler challengeHandler,
            @Nullable CooldownHandler cooldownHandler
    ) {
        this.plugin = plugin;
        this.gate = gate;
        this.guildHandler = guildHandler;
        this.arenaHandler = arenaHandler;
        this.challengeHandler = challengeHandler;
        this.cooldownHandler = cooldownHandler;
    }

    /**
     * The shared gate, for callers that coordinate their own writes.
     *
     * @return the gate
     */
    @NotNull public PersistenceGate getGate() {
        return gate;
    }

    /**
     * Starts a capture that can be spread over several ticks.
     *
     * <p>Must be called on the main thread. Captures the small collections immediately and returns a
     * session holding the guild ids to work through.
     *
     * @return a resumable capture
     */
    @NotNull public CaptureSession beginCapture() {
        return new CaptureSession(guildHandler, arenaHandler, challengeHandler, cooldownHandler, plugin.getDatabase());
    }

    /**
     * Serialises every collection in one pass on the calling thread.
     *
     * <p>Must be called on the main thread. This is the single-pass capture, kept for shutdown and for
     * migration, where there is no tick budget to respect: the server is stopping, or an admin is waiting
     * on a console command that is not going to tick again. It runs the same {@link CaptureSession}
     * steps with an effectively unlimited budget, so both paths produce identical snapshots.
     *
     * @return a snapshot that later mutations cannot affect
     */
    @NotNull public PluginSnapshot capture() {
        final CaptureSession session = beginCapture();

        // Unbounded. One tick's budget would be wrong here: this runs from `onDisable` and from a console
        // command, neither of which gets another tick, so a budgeted capture could never finish and the
        // server would stop with no final save at all.
        //
        // At 5000 guilds with stocked vaults this is around two seconds of main-thread stall. Paper's
        // watchdog is sixty seconds, and both callers are rare, so paying it is the right trade.
        if (!session.step(UNBOUNDED_BUDGET_NANOS)) {
            // Unreachable: `step` stops only when it runs out of guilds, and the budget cannot run out.
            // Treated as a hard failure rather than looping, because a loop here would hang the shutdown.
            throw new IllegalStateException("An unbounded capture did not complete in one pass.");
        }

        return session.finish();
    }

    /**
     * Writes a snapshot to the backend it was captured against.
     *
     * <p>Safe to call from any thread, and takes no permit: {@link #tick} and
     * {@link #shutdownFlush} already hold one for the duration.
     *
     * @param snapshot the snapshot to write
     */
    public void write(@NotNull PluginSnapshot snapshot) {
        final DatabaseAdapter database = snapshot.getDatabase();
        if (database == null) {
            LoggingUtils.warn("Skipping save: the plugin has no database adapter.");
            return;
        }
        writeTo(database, snapshot);
    }

    /**
     * Writes a snapshot to a specific backend.
     *
     * <p>Migration uses this directly rather than {@link #write}, because it has to hold the write
     * permit across the write <em>and</em> the backend swap that follows it, and because it writes to
     * the new backend rather than the captured one.
     *
     * @param database the backend to write to
     * @param snapshot the snapshot to write
     */
    public void writeTo(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot) {
        writeTo(database, snapshot, null);
    }

    /**
     * Writes a snapshot to a specific backend, reporting whether everything was written.
     *
     * <p>Migration needs the second form. The autosave can afford to swallow a failure per collection,
     * because the next one will try again and nothing depends on the outcome. Migration cannot: it is about
     * to swap the plugin over to this backend and close the old one, so a failure it does not notice means
     * an empty new backend, a closed old pool, and an operator who was told it worked.
     *
     * @param database the backend to write to
     * @param snapshot the snapshot to write
     * @param failures collects a description of each collection that could not be written, or null
     * @return true when every collection was written without throwing
     */
    public boolean writeTo(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        boolean complete = true;
        complete &= saveGuilds(database, snapshot, failures);
        complete &= saveArenas(database, snapshot, failures);
        complete &= saveChallenges(database, snapshot, failures);
        complete &= saveCooldowns(database, snapshot, failures);
        return complete;
    }

    /**
     * The state of a spread autosave capture that is in progress.
     *
     * <p>At most one exists. {@link #tick} creates it, ticks it forward, and hands the
     * finished snapshot to the write.
     */
    private CaptureSession pendingCapture;

    /**
     * When the last capture finished, in {@link System#nanoTime()} terms, or zero if none has.
     *
     * <p>Zero is a usable "never" because the scheduler's own clock is an arbitrary origin, but a capture
     * at exactly zero is not a thing that happens.
     */
    private long lastCaptureFinishedNanos;

    /**
     * When the "skipped because busy" message was last logged, or zero if it never has been.
     *
     * <p>Exists only to rate-limit that message. The timer runs every tick, so without it a save that
     * takes thirty seconds logs six hundred identical lines.
     */
    private long lastSkipLoggedNanos;

    /**
     * Runs one tick of the autosave state machine.
     *
     * <p>Called from a per-tick synchronous timer on the main thread, and does one of three things:
     * starts a capture if the interval has elapsed and none is running, spends this tick's budget on a
     * capture that is already running, or does nothing at all.
     *
     * <p>Doing nothing is the common case. The timer runs every tick because a capture has to be able to
     * continue within a tick of finishing, but a save is only due once per configured interval, so almost
     * every tick is spent before the interval is up.
     *
     * <p>If the gate is held, or shutdown has begun, no capture is started. A capture already running is
     * carried to completion rather than abandoned: the permit is released by the write, and abandoning
     * would mean the write never happens and the permit never comes back.
     *
     * @param budgetNanos how long this tick may spend capturing
     * @param intervalNanos how often a new capture is due
     * @param asyncWriter hands the write to a background thread
     */
    public void tick(long budgetNanos, long intervalNanos, @NotNull AsyncWrite asyncWriter) {
        if (pendingCapture == null) {
            if (gate.isShuttingDown() || gate.isMigrating() || !isSaveDue(intervalNanos)) {
                return;
            }

            if (!gate.tryAcquireWriter()) {
                // The timer runs every tick, so without a latch this logs once per tick for as long as the
                // other writer takes. A slow SQL write would bury the console in hundreds of identical
                // lines.
                if (lastSkipLoggedNanos == 0L || System.nanoTime() - lastSkipLoggedNanos >= intervalNanos) {
                    lastSkipLoggedNanos = System.nanoTime();
                    LoggingUtils.info("Skipping autosave: another save or migration is still running.");
                }
                return;
            }

            try {
                pendingCapture = beginCapture();
                lastSkipLoggedNanos = 0L;
            } catch (RuntimeException e) {
                gate.releaseWriter();
                LoggingUtils.severe("An error occurred while starting the plugin data capture. The save was skipped.", e);
                return;
            }
        }

        advancePendingCapture(budgetNanos, asyncWriter);
    }

    /**
     * Whether the save interval has elapsed since the last capture finished.
     *
     * <p>Measured from the end of the previous capture rather than from when it started, so a slow capture
     * does not immediately become due again and the autosave cannot run back to back.
     *
     * @param intervalNanos how often a capture is due
     * @return true when a capture should start
     */
    private boolean isSaveDue(long intervalNanos) {
        return lastCaptureFinishedNanos == 0L || System.nanoTime() - lastCaptureFinishedNanos >= intervalNanos;
    }

    /**
     * Whether a capture is in progress.
     *
     * @return true while a spread capture is running
     */
    public boolean hasPendingCapture() {
        return pendingCapture != null;
    }

    /**
     * Steps the running capture once, and submits the write when it completes.
     *
     * @param budgetNanos how long this tick may spend capturing
     * @param asyncWriter hands the write to a background thread
     * @return true while the capture is still in progress
     */
    private boolean advancePendingCapture(long budgetNanos, @NotNull AsyncWrite asyncWriter) {
        final CaptureSession session = pendingCapture;
        if (session == null) {
            return false;
        }

        final boolean complete;
        try {
            complete = session.step(budgetNanos);
        } catch (RuntimeException e) {
            LoggingUtils.severe("An error occurred while capturing plugin data. The save was skipped.", e);
            abandonPendingCapture();
            return false;
        }

        if (!complete) {
            return true;
        }

        final PluginSnapshot snapshot;
        try {
            snapshot = session.finish();
        } catch (RuntimeException e) {
            LoggingUtils.severe("An error occurred while finishing the plugin data capture. The save was skipped.", e);
            abandonPendingCapture();
            return false;
        } finally {
            pendingCapture = null;
        }

        submitWrite(snapshot, asyncWriter, "the scheduled save task");
        lastCaptureFinishedNanos = System.nanoTime();
        return false;
    }

    /**
     * Discards an in-progress capture and gives the permit back, if one was running.
     *
     * <p>Without this, a capture that threw would hold the write permit until the process ended, and every
     * later save, including the one on shutdown, would skip.
     *
     * <p>Only ever releases a permit this coordinator took. A double release would put the gate back to
     * two permits and let two writers run at once, which is the whole thing it prevents.
     */
    private void abandonPendingCapture() {
        if (pendingCapture == null) {
            return;
        }
        pendingCapture = null;
        gate.releaseWriter();
    }

    /**
     * Hands a finished snapshot to a worker, falling back to writing inline if it cannot be scheduled.
     *
     * @param snapshot   the captured state
     * @param asyncWriter hands the write to a background thread
     * @param context     what the write was for, used in the log message
     */
    private void submitWrite(@NotNull PluginSnapshot snapshot, @NotNull AsyncWrite asyncWriter, @NotNull String context) {
        final Runnable write = () -> {
            try {
                write(snapshot);
            } catch (RuntimeException e) {
                LoggingUtils.severe("An error occurred while saving plugin data during " + context + ".", e);
            } finally {
                gate.releaseWriter();
            }
        };

        try {
            asyncWriter.execute(write);
        } catch (RuntimeException e) {
            // The scheduler refuses to queue a task for a disabled plugin, and that surfaces here as an
            // IllegalPluginAccessException before the write ever runs. Either way the permit has to come
            // back, or every later save skips for the rest of the session.
            //
            // Whether to write inline depends on why. Mid-session a reload that did not fully disable the
            // plugin, say, the write is the only thing standing between the operator and a lost save, so it
            // runs inline even though that means blocking a tick. During shutdown it does not: the
            // synchronous flush is about to write the same data anyway, and doing it twice would put a
            // full database write on the main thread of a server that is trying to stop.
            LoggingUtils.severe("An error occurred while scheduling " + context + ".", e);
            if (gate.isShuttingDown()) {
                gate.releaseWriter();
                return;
            }
            write.run();
        }
    }

    /**
     * The final save on shutdown.
     *
     * <p>The ordering is the fix. Bukkit has already cancelled the autosave schedule by the time this
     * runs, but cancelling a schedule does not interrupt a run that is already going, so a worker may
     * still hold the write permit; draining first means this flush cannot interleave with it. Writing
     * next means the connection pool is still open. Closing last means the flush had a database to
     * write to.
     *
     * @param closeDatabase closes the backend, called even when the save throws
     */
    public void shutdownFlush(@NotNull Runnable closeDatabase) {
        gate.beginShutdown();

        // A spread capture can only advance on the main thread, and this method is running on the main
        // thread. Waiting for one to finish would therefore wait forever, so it is abandoned instead: the
        // permit comes back, the snapshot is discarded, and the synchronous capture below does the whole
        // job in one pass. A half-finished capture has no value on its own, and it is the only writer that
        // the drain below could not have waited out.
        abandonPendingCapture();

        final boolean drained = gate.acquireWriter(SHUTDOWN_DRAIN_TIMEOUT_MILLIS);
        if (!drained) {
            // Something is holding the permit and is not a capture, so it is a write already in flight,
            // possibly on a dead connection.
            //
            // Neither writing nor closing is safe here. Writing means two threads writing the same
            // `<uuid>.json` files at once, which interleaves into JSON that will not parse on the next
            // boot; that is corruption, and corruption is worse than a save that did not happen. Closing
            // the connection pool under a running writer has the same character. So the flush is skipped
            // and the pool is left for the JVM shutdown hook, and the operator is told plainly that the
            // last save did not happen.
            LoggingUtils.severe("A save was still running after " + SHUTDOWN_DRAIN_TIMEOUT_MILLIS
                    + "ms and did not finish. Skipping the final save and leaving the database open; the last"
                    + " autosave is what is on disk. This means a save was wedged, usually on a dead database"
                    + " connection.");
            return;
        }

        try {
            write(capture());
        } catch (RuntimeException e) {
            // Each collection is written inside its own try, so this only fires for something outside
            // them: a capture that threw, or a failure in the database close below.
            LoggingUtils.severe("An error occurred while saving plugin data during shutdown.", e);
        } finally {
            try {
                closeDatabase.run();
            } finally {
                gate.releaseWriter();
            }
        }
    }

    /**
     * Hands a write to a background thread.
     *
     * <p>An interface rather than a direct scheduler call so the coordinator does not depend on Bukkit
     * and so tests can run a write inline or on a thread they control.
     */
    @FunctionalInterface
    public interface AsyncWrite {
        /**
         * Runs the write away from the calling thread.
         *
         * @param write the write to perform
         */
        void execute(@NotNull Runnable write);
    }

    private boolean saveGuilds(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        if (snapshot.getGuilds().isEmpty()) {
            return true;
        }
        try {
            database.getGuildAdapter().saveSerialized(snapshot.getGuilds());
            return true;
        } catch (IOException | RuntimeException e) {
            return failed("guild", failures, e);
        }
    }

    private boolean saveArenas(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        // Deliberately not short-circuited on an empty map, unlike the three above.
        //
        // `ArenaAdapter#saveSerialized` deletes every stored arena whose id is absent from the map it is
        // given, and that delete pass is the only thing that persists an arena deletion: `removeArena`
        // only touches the in-memory map. Skipping the call when the map is empty therefore means the
        // last arena an admin deletes is never removed from storage, comes back on the next restart, and
        // can never be deleted again because the map is now permanently empty.
        try {
            database.getArenaAdapter().saveSerialized(snapshot.getArenas());
            return true;
        } catch (IOException | RuntimeException e) {
            return failed("arena", failures, e);
        }
    }

    private boolean saveChallenges(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        if (snapshot.getChallenges().isEmpty()) {
            return true;
        }
        try {
            database.getChallengeAdapter().saveSerialized(snapshot.getChallenges());
            return true;
        } catch (IOException | RuntimeException e) {
            return failed("challenge", failures, e);
        }
    }

    private boolean saveCooldowns(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        if (snapshot.getCooldowns().isEmpty()) {
            return true;
        }
        try {
            // Last, and guarded, unlike the three above. `CooldownAdapter#saveCooldowns` swallows
            // IOException itself but lets a RuntimeException through, and a stale Hikari connection
            // raises exactly that. Sequenced after the others, one such exception cost challenges their
            // write too.
            database.getCooldownAdapter().saveCooldowns(snapshot.getCooldowns());
            return true;
        } catch (RuntimeException e) {
            return failed("cooldown", failures, e);
        }
    }

    /**
     * Logs a failed collection write and records it when the caller is tracking failures.
     *
     * @param label    what kind of data, used in the log message
     * @param failures the collector, or null when the caller does not care
     * @param e        what was thrown
     * @return false, so a caller accumulating results can use it directly
     */
    private boolean failed(@NotNull String label, @Nullable List<String> failures, @NotNull Exception e) {
        LoggingUtils.severe("An error occurred while saving " + label + " data.", e);
        if (failures != null) {
            failures.add(label + ": " + e);
        }
        return false;
    }
}
