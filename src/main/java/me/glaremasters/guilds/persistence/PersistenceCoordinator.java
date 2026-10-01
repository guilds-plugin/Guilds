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
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Captures plugin state on the main thread and writes it off it.
 *
 * <p>Reading needs the main thread: the guild, arena and challenge collections are plain maps written
 * by command handlers and war tasks, and guild vaults are live Bukkit inventories a player may be
 * clicking in while a save runs. Writing needs any thread but the main one. So {@link #capture}
 * produces a {@link PluginSnapshot} of serialised bytes and {@link #write} only moves those bytes.
 *
 * <p>The four collections are written independently, so a backend that throws for one of them costs
 * the operator that kind of data and not the rest.
 */
public final class PersistenceCoordinator {

    /**
     * How long the shutdown flush waits for an in-flight save before writing anyway.
     *
     * <p>Bounded on purpose: an unbounded block inside {@code onDisable} is how a plugin hangs a
     * shutdown.
     */
    private static final long SHUTDOWN_DRAIN_TIMEOUT_MILLIS = 5000L;

    /**
     * The budget handed to a capture that must finish in one pass.
     *
     * <p>Deliberately not {@code Long.MAX_VALUE}: {@code CaptureSession#step} adds this to
     * {@code System.nanoTime()}, which would overflow to a negative deadline, expire immediately, and turn
     * the capture into a hard failure.
     */
    private static final long UNBOUNDED_BUDGET_NANOS = TimeUnit.DAYS.toNanos(1L);

    /**
     * The most rows a single migration will reconcile away before refusing.
     *
     * <p>Both reconcile passes delete one row at a time, and on the JSON backend a cooldown delete re-reads
     * and rewrites the whole cooldown file. So the cost is superlinear in the number of stale rows, and a
     * destination that had been used before, or one that failed an earlier migration attempt, can hold
     * thousands. This runs on the main thread inside an explicit console command, so a cap is the
     * difference between an operator waiting and a watchdog kill.
     */
    static final int MAX_RECONCILED_ROWS = 500;

    /**
     * Guilds the plugin no longer has, whose rows a save still has to delete.
     *
     * <p>Concurrent because the main thread adds to it while a worker reads it. Nothing prunes it on capture:
     * a capture does not hold the write permit, so pruning there would erase a disband a write already in
     * flight was about to act on. A guild id is never reused, so a stale entry cannot name a live guild.
     */
    private final Set<String> pendingGuildDisbands = ConcurrentHashMap.newKeySet();

    /** Whether the calling thread is the one that owns mutable plugin state. Overridden in tests. */
    private final java.util.function.BooleanSupplier onMainThread;

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
        this(plugin, gate, guildHandler, arenaHandler, challengeHandler, cooldownHandler,
                PersistenceCoordinator::isBukkitMainThread);
    }

    public PersistenceCoordinator(
            @NotNull Guilds plugin,
            @NotNull PersistenceGate gate,
            @Nullable GuildHandler guildHandler,
            @Nullable ArenaHandler arenaHandler,
            @Nullable ChallengeHandler challengeHandler,
            @Nullable CooldownHandler cooldownHandler,
            @NotNull java.util.function.BooleanSupplier onMainThread
    ) {
        this.onMainThread = onMainThread;
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
     * <p>Must be called on the main thread.
     *
     * @return a resumable capture
     */
    @NotNull public CaptureSession beginCapture() {
        return new CaptureSession(guildHandler, arenaHandler, challengeHandler, cooldownHandler, plugin.getDatabase());
    }

    /**
     * Records a guild the plugin no longer has, as work a save still owes the backend.
     *
     * <p>Must be called on the main thread, before or with the removal from the live map. Acknowledged only
     * by a save that has deleted the row, so a failed delete stays outstanding.
     *
     * @param guildId the guild that was disbanded
     */
    public void noteGuildDisbanded(@NotNull UUID guildId) {
        pendingGuildDisbands.add(guildId.toString());
    }

    /**
     * Whether the calling thread is the one that owns mutable plugin state.
     *
     * @return true on the main thread
     */
    public boolean isOnMainThread() {
        return onMainThread.getAsBoolean();
    }

    /**
     * Serialises every collection in one pass on the calling thread.
     *
     * <p>Must be called on the main thread. Kept for shutdown and migration, where there is no tick
     * budget to respect: neither caller gets another tick. Runs the same {@link CaptureSession} steps
     * with an effectively unlimited budget, so both paths produce identical snapshots.
     *
     * @return a snapshot that later mutations cannot affect
     */
    @NotNull public PluginSnapshot capture() {
        final CaptureSession session = beginCapture();

        // Unbounded: one tick's budget would mean a budgeted capture never finishes and the server stops
        // with no final save. At 5000 guilds with stocked vaults this is around two seconds of stall, which
        // is well inside Paper's sixty-second watchdog.
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
     * <p>Migration uses this rather than {@link #write} because it has to hold the write permit across
     * the write <em>and</em> the backend swap that follows it.
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
     * <p>Migration needs the reporting form because it is about to swap the plugin over to this backend
     * and close the old one. An unnoticed failure means an empty new backend and an operator who was
     * told it worked.
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
     * Whether the calling thread is the Bukkit main thread.
     *
     * <p>{@code Bukkit.isPrimaryThread()} delegates to a static server reference that is null in a unit
     * test, so this reports false rather than throwing when there is no server.
     */
    private static boolean isBukkitMainThread() {
        try {
            return org.bukkit.Bukkit.isPrimaryThread();
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }

    /**
     * Reconciles a migration's destination against live state and publishes it.
     *
     * <p>Must be called on the main thread, and that is checked rather than assumed. Guild disbanding
     * happens on the main thread, so reconciling and publishing adjacently there leaves no point at which
     * the main thread can run between them. The alternative, reconciling after the write on a worker,
     * leaves a window: the destination would be brought into line and then a guild disbanded before the
     * swap, and {@code GuildAdapter} has no delete pass, so that disband would be undone permanently.
     *
     * <p>Assumed rather than checked is not good enough here, because TaskChain does not guarantee it.
     * Its {@code postToMain} runs the task inline on the calling thread when the plugin is disabled,
     * rather than scheduling it, so a migration in flight when the server stops would reconcile against
     * live state from a pool thread while the main thread ran the shutdown flush over the same map.
     *
     * <p>Deliberately not part of {@link #writeTo}, which runs on a worker and is also the autosave path.
     * A regular save that pruned would delete a guild whose row simply had not been written yet.
     *
     * <p>The destination is reconciled for guilds and cooldowns, the two collections whose writes cannot
     * bring the destination into line on their own. Arenas are not touched: {@code ArenaAdapter} already
     * deletes by absence and reads its key set as late as it can, so its residual window is a stale row for
     * one save interval, which the next autosave removes. Touching arenas here as well would give two passes
     * authority over one collection.
     *
     * @param destination the backend to reconcile and then publish
     * @param failures    collects a description of what went wrong, or null
     * @return true when the destination was published
     */
    public boolean reconcileAndPublish(@NotNull DatabaseAdapter destination, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        if (!onMainThread.getAsBoolean()) {
            final String message = "the migration could not be published because the step that reconciles the"
                    + " new backend did not run on the server's main thread. Nothing was changed; the"
                    + " previous backend is still in use.";
            LoggingUtils.severe(message);
            if (failures != null) {
                failures.add(message);
            }
            return false;
        }

        // Validated before anything is deleted here. Not `complete &= ...` across two passes: a bitwise and
        // evaluates its right operand regardless, so a guild pass that had given up would still be followed
        // by the destructive half of the other one.
        //
        // The cooldown pass has already run by this point, on the worker, so a refusal below has not left the
        // destination untouched. It has left the previous backend in use, which is the part that matters.
        final List<String> staleGuilds = findStaleGuilds(destination, failures);
        if (staleGuilds == null) {
            return false;
        }

        for (String id : staleGuilds) {
            try {
                destination.getGuildAdapter().deleteGuild(id);
            } catch (IOException | RuntimeException e) {
                return failed("guild reconciliation", failures, e);
            }
        }

        if (!staleGuilds.isEmpty()) {
            LoggingUtils.info("Migration removed " + staleGuilds.size()
                    + " guild(s) disbanded while it was running.");
        }

        publishBackend(destination);

        // The destination now holds the snapshot, not the plugin's current state. A guild created or edited
        // after the capture is not in it, and the autosave is gated off until the migration flag clears, so
        // without this the gap would last a whole save interval. Making the save due now closes it on the
        // next tick rather than the next interval.
        markSaveDue();

        if (guildHandler != null && snapshot.getGuilds().size() != guildHandler.getGuilds().size()) {
            LoggingUtils.info("Guilds were created or removed while the migration ran ("
                    + snapshot.getGuilds().size() + " in the snapshot, " + guildHandler.getGuilds().size()
                    + " now). The next save brings the new backend level.");
        }

        return true;
    }

    /**
     * Makes the next tick start a capture, by forgetting when the last one finished.
     *
     * <p>Must be called on the main thread, like the rest of the autosave state. Zero is the coordinator's
     * "never captured" marker, so this reads as overdue rather than as a timestamp in the past.
     */
    public void markSaveDue() {
        lastCaptureFinishedNanos = 0L;
    }

    /**
     * Works out which of the destination's guilds the plugin no longer has, without deleting anything.
     *
     * <p>Separated from the delete loop so that every way this can refuse, it refuses before the first
     * row is touched. A half-applied reconciliation leaves the destination inconsistent, and the operator
     * has no way to tell which half ran.
     *
     * @param destination the backend being migrated into
     * @param failures    collects a description of what went wrong, or null
     * @return the ids to delete, or null when the migration must not proceed
     */
    @Nullable private List<String> findStaleGuilds(@NotNull DatabaseAdapter destination, @Nullable List<String> failures) {
        if (guildHandler == null) {
            return Collections.emptyList();
        }

        final GuildAdapter adapter = destination.getGuildAdapter();
        final List<String> stale = new ArrayList<>();

        try {
            final List<String> ids = adapter.getAllGuildIds();

            // Bounded rather than proportional to the destination, because this runs on the main thread.
            // At most `live + MAX` rows can be worth reading: past that, the stale count alone already
            // exceeds the cap and the migration is going to refuse, so the per-row parse and map lookup are
            // skipped along with the delete loop that would have followed.
            //
            // The fetch itself is the provider's, and is not bounded here — a SQL backend answers this with
            // a full scan of the guild table. Pushing a limit down into the providers is the fix for that,
            // and is not in this change.
            final int live = guildHandler.getGuilds().size();
            if (ids.size() > live + MAX_RECONCILED_ROWS) {
                // An upper bound rather than a count: it assumes every live guild is also in the
                // destination, so the true number of stale rows is at least this and possibly all of them.
                atLeastTooManyRows("guilds", ids.size() - live, failures);
                return null;
            }

            for (String id : ids) {
                final UUID guildId = parseGuildId(id, destination, failures);
                if (guildId == null) {
                    return null;
                }
                if (guildHandler.getGuilds().get(guildId) == null) {
                    stale.add(id);
                    if (stale.size() > MAX_RECONCILED_ROWS) {
                        tooManyRows("guilds", stale.size(), failures);
                        return null;
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            failed("guild reconciliation", failures, e);
            return null;
        }

        return stale;
    }

    /**
     * Refuses a reconciliation that would take longer than the operator can be expected to wait.
     *
     * <p>Refusing rather than truncating is the point. Reconciling only some of the rows would leave the
     * destination inconsistent, and publishing an inconsistent destination is the failure this pass exists
     * to prevent.
     *
     * @param what     the kind of row
     * @param count    how many there are
     * @param failures collects the message, or null
     * @return false, so a caller accumulating results can use it directly
     */
    private boolean tooManyRows(@NotNull String what, int count, @Nullable List<String> failures) {
        final String message = "the destination holds " + count + " " + what + " the plugin no longer has, which is"
                + " more than " + MAX_RECONCILED_ROWS + ". Empty that backend, or migrate to a different one, and"
                + " run the migration again. The previous backend is still in use and is what the plugin is"
                + " reading and writing.";
        return refuse(message, failures);
    }

    /**
     * Refuses where only a lower bound on the stale count is known, and says so.
     *
     * @param what     the kind of row
     * @param atLeast  a lower bound on how many the plugin no longer has
     * @param failures collects the message, or null
     * @return false, so a caller accumulating results can use it directly
     */
    private boolean atLeastTooManyRows(@NotNull String what, int atLeast, @Nullable List<String> failures) {
        final String message = "the destination holds at least " + atLeast + " " + what + " the plugin no longer"
                + " has, which is more than " + MAX_RECONCILED_ROWS + ". Empty that backend, or migrate to a"
                + " different one, and run the migration again. The previous backend is still in use and is what"
                + " the plugin is reading and writing.";
        return refuse(message, failures);
    }

    private boolean refuse(@NotNull String message, @Nullable List<String> failures) {
        LoggingUtils.severe(message);
        if (failures != null) {
            failures.add(message);
        }
        return false;
    }

    /**
     * Refuses a cooldown reconciliation that would take longer than an operator can be expected to wait.
     *
     * <p>Separate from {@link #tooManyRows} because the two need different advice. Rows with a wrong expiry
     * are rows the plugin does have, so telling the operator to empty their cooldown table over them would be
     * both wrong and destructive.
     *
     * @param stale        rows the plugin no longer has
     * @param wrongExpiry  rows that match but expire at the wrong time
     * @param failures     collects the message, or null
     * @return false, so a caller accumulating results can use it directly
     */
    private boolean tooManyCooldowns(int stale, int wrongExpiry, @Nullable List<String> failures) {
        final String message = "the destination's cooldowns could not be reconciled in reasonable time: "
                + stale + " the plugin no longer has and " + wrongExpiry + " with the wrong expiry, against a"
                + " limit of " + MAX_RECONCILED_ROWS + " changes or rows. Migrating to a fresh backend avoids"
                + " this. The previous backend is still in use and is what the plugin is reading and writing.";
        LoggingUtils.severe(message);
        if (failures != null) {
            failures.add(message);
        }
        return false;
    }

    /**
     * Parses a guild id from the destination, or reports why it could not.
     *
     * <p>A destination can hold an id the plugin did not write: a hand-edited JSON filename, a leftover
     * from an older plugin version, a truncated row. It cannot be matched against the live guilds, and it
     * cannot be deleted safely either, because a guild this plugin does not recognise is not necessarily a
     * guild the operator wants gone. So the migration stops and names the row instead of guessing.
     *
     * @param id       the id as stored
     * @param failures collects a description, or null
     * @return the parsed id, or null when it is malformed
     */
    @Nullable private UUID parseGuildId(@NotNull String id, @NotNull DatabaseAdapter destination, @Nullable List<String> failures) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // Resolved here rather than on the happy path, so a backend that cannot report its own name
            // does not stop a migration that would otherwise have succeeded.
            final DatabaseBackend backend = destination.getBackend();
            final String where = backend == null ? "new backend" : backend.getBackendName() + " backend";
            final String message = "the destination holds a guild with the malformed id '" + id
                    + "'. Delete that guild's data file or row from the " + where
                    + " and run the migration again.";
            LoggingUtils.severe("Migration cannot continue: " + message);
            if (failures != null) {
                failures.add(message);
            }
            return null;
        }
    }

    /**
     * Brings a migration destination's cooldowns into line with the snapshot, on any thread.
     *
     * <p>Off the main thread on purpose. Two reasons, and the second is the one that matters.
     *
     * <p>A JSON cooldown delete re-reads, filters and rewrites the whole cooldown file, so deleting one row
     * is O(rows) and a destination that has been used before can hold thousands. That is superlinear, and
     * it has no business on the tick thread.
     *
     * <p>Nor does it need to be there. Guild disbanding is the only mutation the main thread has to be
     * near, because {@code GuildAdapter} has no delete pass and a guild caught in a window would be
     * resurrected in the destination permanently. Cooldowns have no such hazard: they expire on their own,
     * and both sides of the comparison are detached, the destination's rows and the snapshot's. So this runs
     * on the worker, before the main-thread step that reconciles guilds and publishes.
     *
     * <p>Detachment holds because a destination on the same tables as the source is refused before a pool is
     * opened — see {@link me.glaremasters.guilds.database.DatabaseAdapter#sharesStorageWith}.
     *
     * <p>Reproduces the snapshot's expiry rather than leaving whatever the destination had. The destination
     * is keyed by type and owner, and {@code CooldownAdapter#saveCooldowns} only ever creates, so a
     * destination that already held a matching cooldown kept its own expiry. After a migration the operator
     * would have cooldowns that expire at the wrong time: a set-home cooldown that outlives the snapshot by
     * a week, or one that has already lapsed. A matching row with a different expiry is deleted and recreated.
     *
     * @param destination the backend to reconcile
     * @param snapshot    the state the destination was written from
     * @param failures    collects a description of what went wrong, or null
     * @return true when the destination's cooldowns match the snapshot
     */
    public boolean reconcileCooldowns(@NotNull DatabaseAdapter destination, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        final CooldownAdapter adapter = destination.getCooldownAdapter();
        final List<Cooldown> stale = new ArrayList<>();
        final Map<Cooldown, Cooldown> wrongExpiry = new LinkedHashMap<>();

        try {
            final List<Cooldown> stored = adapter.getAllCooldowns();
            // Keyed for lookup, because a linear scan per stored row makes the comparison quadratic in the
            // two collections at once — and this runs over a destination that may hold thousands.
            final Map<CooldownKey, Cooldown> captured = indexBy(snapshot.getCooldowns());

            for (Cooldown row : stored) {
                final Cooldown match = captured.get(new CooldownKey(row.getCooldownType(), row.getCooldownOwner()));

                if (match == null) {
                    stale.add(row);
                } else if (!match.getCooldownExpiry().equals(row.getCooldownExpiry())) {
                    wrongExpiry.put(row, match);
                }
            }

            if (stale.size() + wrongExpiry.size() > MAX_RECONCILED_ROWS) {
                return tooManyCooldowns(stale.size(), wrongExpiry.size(), failures);
            }

            // A JSON cooldown delete re-reads, filters and rewrites the whole file, so every row changed
            // costs a pass over every row stored. The bound is on that product rather than on either factor,
            // which keeps a one-row trim of a large destination allowed: that is one pass, and it is the case
            // a destination that outlived a restart mostly needs.
            final long rewrites = (long) (stale.size() + 2L * wrongExpiry.size()) * (long) stored.size();
            if (rewrites > (long) MAX_RECONCILED_ROWS * MAX_RECONCILED_ROWS) {
                return tooManyCooldowns(stale.size(), wrongExpiry.size(), failures);
            }

            for (Cooldown cooldown : stale) {
                adapter.deleteCooldown(cooldown);
            }
            for (Map.Entry<Cooldown, Cooldown> entry : wrongExpiry.entrySet()) {
                adapter.deleteCooldown(entry.getKey());
                adapter.createCooldown(entry.getValue());
            }
        } catch (IOException | RuntimeException e) {
            return failed("cooldown reconciliation", failures, e);
        }

        if (!stale.isEmpty() || !wrongExpiry.isEmpty()) {
            LoggingUtils.info("Migration reconciled " + stale.size() + " stale cooldown(s) and "
                    + wrongExpiry.size() + " with the wrong expiry.");
        }
        return true;
    }

    /** The identity a cooldown is stored under: one per type and owner. */
    private static final class CooldownKey {
        private final Cooldown.Type type;
        private final UUID owner;

        private CooldownKey(@NotNull Cooldown.Type type, @NotNull UUID owner) {
            this.type = type;
            this.owner = owner;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CooldownKey)) {
                return false;
            }
            final CooldownKey that = (CooldownKey) other;
            return type == that.type && owner.equals(that.owner);
        }

        @Override
        public int hashCode() {
            return 31 * type.hashCode() + owner.hashCode();
        }
    }

    /**
     * Indexes cooldowns by the identity the destination stores them under.
     *
     * @param cooldowns the captured cooldowns
     * @return each cooldown, keyed by type and owner
     */
    @NotNull private static Map<CooldownKey, Cooldown> indexBy(@NotNull List<Cooldown> cooldowns) {
        final Map<CooldownKey, Cooldown> index = new HashMap<>();
        for (Cooldown cooldown : cooldowns) {
            index.put(new CooldownKey(cooldown.getCooldownType(), cooldown.getCooldownOwner()), cooldown);
        }
        return index;
    }

    /** The spread autosave capture in progress, if any. At most one exists. */
    private CaptureSession pendingCapture;

    /**
     * When the last capture finished, in {@link System#nanoTime()} terms, or zero if none has. Zero is a
     * usable "never" because the scheduler's own clock is an arbitrary origin.
     */
    private long lastCaptureFinishedNanos;

    /**
     * When the "skipped because busy" message was last logged, or zero if it never has been. Exists only to
     * rate-limit that message, which would otherwise log once per tick for as long as a slow save takes.
     */
    private long lastSkipLoggedNanos;

    /**
     * Runs one tick of the autosave state machine: start a capture if one is due and none is running, spend
     * this tick's budget on a capture that is running, or do nothing.
     *
     * <p>Called from a per-tick synchronous timer on the main thread, so doing nothing is the common case.
     *
     * <p>If the gate is held, or shutdown has begun, no capture is started. A capture already running is
     * carried to completion rather than abandoned: the permit is released by the write, so abandoning
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
                // Without a latch this logs once per tick for as long as the other writer takes.
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
     * <p>Only ever releases a permit this coordinator took: a double release would let two writers run at
     * once, which is the whole thing the gate prevents.
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
            // Either way the permit has to come back, or every later save skips for the rest of the session.
            //
            // Whether to write inline depends on why. Mid-session the write is the only thing standing
            // between the operator and a lost save, so it runs inline even though that blocks a tick.
            // During shutdown it does not: the synchronous flush is about to write the same data anyway,
            // and doing it twice would put a full database write on the main thread of a server that is
            // trying to stop.
            LoggingUtils.severe("An error occurred while scheduling " + context + ".", e);
            if (gate.isShuttingDown()) {
                gate.releaseWriter();
                return;
            }
            write.run();
        }
    }

    /**
     * Points the plugin at a new backend and closes the one it was using.
     *
     * <p>Publish before closing, and never let the close escape. The reverse order means an exception from
     * close leaves the plugin holding a reference to a pool that is being torn down, so every later write
     * fails with "HikariDataSource has been closed". Once the new backend is published the close is a
     * cleanup step rather than part of the transition, so a close that fails costs nothing.
     *
     * @param destination the backend to publish
     */
    public void publishBackend(@NotNull DatabaseAdapter destination) {
        final DatabaseAdapter previous = plugin.getDatabase();
        plugin.setDatabase(destination);

        try {
            previous.close();
        } catch (RuntimeException e) {
            LoggingUtils.severe("Published the new database backend, but closing the previous one failed.", e);
        }
    }

    /**
     * The final save on shutdown.
     *
     * <p>Drain, write, close, in that order. Bukkit has already cancelled the autosave schedule by the time
     * this runs, but cancelling a schedule does not interrupt a run that is already going, so a worker may
     * still hold the write permit.
     *
     * @param closeDatabase closes the backend, called even when the save throws
     */
    public void shutdownFlush(@NotNull Runnable closeDatabase) {
        gate.beginShutdown();

        // A spread capture only advances on the main thread, so waiting for one here would wait forever. It
        // is abandoned instead: the permit comes back and the synchronous capture below does the whole job
        // in one pass.
        abandonPendingCapture();

        final boolean drained = gate.acquireWriter(SHUTDOWN_DRAIN_TIMEOUT_MILLIS);
        if (!drained) {
            // Something is holding the permit and is not a capture, so it is a write already in flight, possibly on a
            // dead connection.
            //
            // Neither writing nor closing is safe here. Writing means two threads writing the same
            // `<uuid>.json` files at once, which interleaves into JSON that will not parse on the next boot;
            // that is corruption, and corruption is worse than a save that did not happen. Closing the pool
            // under a running writer has the same character. So the flush is skipped and the pool is left
            // for the JVM shutdown hook, and the operator is told plainly that the last save did not happen.
            LoggingUtils.severe("A save was still running after " + SHUTDOWN_DRAIN_TIMEOUT_MILLIS
                    + "ms and did not finish. Skipping the final save and leaving the database open; the last"
                    + " autosave is what is on disk. This means a save was wedged, usually on a dead database"
                    + " connection.");
            return;
        }

        try {
            write(capture());
        } catch (RuntimeException e) {
            // Each collection is written inside its own try, so this only fires for a capture that threw.
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
     * <p>An interface rather than a scheduler call so the coordinator does not depend on Bukkit and so
     * tests can run a write inline or on a thread they control.
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
        final Set<String> tombstones = new HashSet<>(pendingGuildDisbands);
        boolean complete = true;

        // Deletes before writes. A crash between the two then leaves a surviving guild at its previous
        // stored version, which the next save repairs, rather than a disbanded guild written back for good.
        // No transaction spans these: the JSON provider rewrites one file per guild, and SQL takes a
        // connection per statement, so ordering is the only lever there is.
        //
        // Acknowledged only against the backend that could actually hold the row. A migration's first write
        // targets a destination that never had it, and acknowledging there would drop a tombstone whose row
        // is still in the backend the plugin stays on when the migration fails.
        if (database == plugin.getDatabase()) {
            for (String id : tombstones) {
                complete &= deleteTombstone(database, id, failures);
            }
            // Arrived while the deletes above were running, so it is not written back below.
            for (String id : new HashSet<>(pendingGuildDisbands)) {
                if (!tombstones.contains(id)) {
                    complete &= deleteTombstone(database, id, failures);
                    tombstones.add(id);
                }
            }
        } else {
            LoggingUtils.warn("A migration's first write left " + tombstones.size()
                    + " disbanded guild(s) for the autosave to delete. The destination never held them.");
        }

        Map<String, String> written = snapshot.getGuilds();
        if (!tombstones.isEmpty()) {
            written = new LinkedHashMap<>(written);
            written.keySet().removeAll(tombstones);
        }

        // Not short-circuited on an empty map: a tombstone is work this call owes the backend whatever the
        // snapshot holds.
        if (!written.isEmpty()) {
            try {
                database.getGuildAdapter().saveSerialized(written);
            } catch (IOException | RuntimeException e) {
                complete = failed("guild", failures, e);
            }
        }
        return complete;
    }

    /**
     * Deletes one tombstoned guild's row and, only if that worked, stops owing it.
     *
     * @return true when the row was deleted, or when the backend is plainly unreachable
     */
    private boolean deleteTombstone(@NotNull DatabaseAdapter database, @NotNull String id, @Nullable List<String> failures) {
        if (!database.isConnected()) {
            // Every tombstone would otherwise pay a full connection timeout and log a stack trace per save.
            // Reported as not done, and left outstanding, which is the same thing.
            return false;
        }
        try {
            database.getGuildAdapter().deleteGuild(id);
            pendingGuildDisbands.remove(id);
            return true;
        } catch (IOException | RuntimeException e) {
            return failed("guild", failures, e);
        }
    }

    private boolean saveArenas(@NotNull DatabaseAdapter database, @NotNull PluginSnapshot snapshot, @Nullable List<String> failures) {
        // Deliberately not short-circuited on an empty map, unlike challenges and cooldowns above.
        //
        // `ArenaAdapter#saveSerialized` deletes every stored arena whose id is absent from the map it is
        // given, and that delete pass is the only thing that persists an arena deletion: `removeArena`
        // only touches the in-memory map. Skipping the call on an empty map means the last arena an admin
        // deletes is never removed from storage, and can never be deleted again because the map is now
        // permanently empty.
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
            database.getCooldownAdapter().saveCooldowns(snapshot.getCooldowns());
            return true;
        } catch (IOException | RuntimeException e) {
            return failed("cooldown", failures, e);
        }
    }

    /**
     * Logs a failed collection write and records it when the caller is tracking failures.
     *
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
