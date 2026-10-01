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
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A capture in progress, resumable across ticks.
 *
 * <p>A capture has to read mutable state on the main thread, because guild vault inventories are live
 * Bukkit objects. It also has to fit in a tick, because capturing a thousand guilds costs roughly 400ms
 * and a tick is 50ms. Those two requirements conflict, so the work is spread: {@link #step} does as
 * much as fits in the budget it is given and returns, and the caller calls it again next tick until it
 * reports the capture is finished.
 *
 * <h2>Guilds and challenges are spread</h2>
 *
 * <p>Guilds are the expensive part: about 65us each with empty vaults, 420us with three full ones. And
 * a guild record is written by upsert, so a guild serialised ten ticks late is a slightly older version
 * of a guild that changed during those ten ticks. Nothing is destroyed by that, and the next save
 * corrects it.
 *
 * <h2>Arenas are captured last, in one pass</h2>
 *
 * <p>This is the part that needs care. {@code ArenaAdapter} deletes every stored arena whose id is
 * missing from the collection it is given, so the arena key set is a claim about which arenas exist.
 * Capturing it at the start of a spread capture would mean an arena deleted ten ticks later is written
 * back to storage, resurrecting it. Capturing it as late as possible shrinks that window to the hop
 * between capture and write, which is the same window a single-pass save always had.
 *
 * <p>It is not eliminated, and cannot be: the write happens on another thread, so there is always a gap.
 * The residual window is about a tick, and an arena deleted inside it is written back once and removed
 * by the following save. That is a stale row for one interval, not a lost arena, which is the right way
 * round to fail.
 *
 * <p>Arenas are also few, so capturing them costs microseconds. There is nothing to gain by spreading
 * them.
 *
 * <p>Challenges are spread too, for the same reason guilds are: a {@link GuildChallenge} carries seventeen
 * properties and the set holds one entry per live war, so serialising them all in one unbudgeted pass
 * would put an unbounded amount of work in whichever tick started the capture. They are upsert-only, so
 * spreading them costs nothing in correctness.
 *
 * <p>Cooldowns are captured in the constructor. They are four immutable fields each, the count is bounded
 * by the number of players who have used a command recently, and the copy is the only work, so there is
 * nothing to gain by spreading them and a window to lose.
 *
 * <h2>Guild removal</h2>
 *
 * <p>A guild deleted while its capture is pending must not be written back, or {@code removeGuild}'s
 * database delete is undone by this save. Each guild is re-read from the handler immediately before it
 * is serialised, and skipped if it has gone. Because both the check and the serialisation run on the
 * main thread, and {@code removeGuild} only ever runs there, nothing can interleave between them.
 */
public final class CaptureSession {

    private final GuildHandler guildHandler;
    private final ArenaHandler arenaHandler;
    private final Map<String, String> guilds = new LinkedHashMap<>();
    private final Map<String, String> challenges = new LinkedHashMap<>();
    private final List<Cooldown> cooldowns = new ArrayList<>();
    private final DatabaseAdapter database;

    /**
     * The guilds still to serialise, as ids in capture order.
     *
     * <p>Ids rather than {@link Guild} references, so a deleted guild cannot be serialised by accident
     * and so the list stays valid however the guild map changes underneath it.
     */
    private final List<UUID> pending;

    /**
     * The challenges still to serialise.
     *
     * <p>Spread for the same reason guilds are, and for the same order of cost. A {@link GuildChallenge}
     * has seventeen properties and the set holds one entry per live war, so a server running a few hundred
     * concurrent wars would otherwise serialise all of them in whichever tick happened to start the
     * capture, unbudgeted. Challenges are written by upsert, so serialising one late is no more harmful
     * than serialising a guild late.
     */
    private final List<GuildChallenge> pendingChallenges = new ArrayList<>();

    private int index;
    private int challengeIndex;

    /**
     * Captures the small collections and takes the detached list of guild ids to work through.
     *
     * <p>Must be called on the main thread.
     *
     * @param guildHandler     the guild handler, or null when guilds are not being captured
     * @param arenaHandler     the arena handler, or null when there is none
     * @param challengeHandler the challenge handler, or null when there is none
     * @param cooldownHandler  the cooldown handler, or null when there is none
     * @param database         the backend to write to, captured with the data
     */
    public CaptureSession(
            @Nullable GuildHandler guildHandler,
            @Nullable ArenaHandler arenaHandler,
            @Nullable ChallengeHandler challengeHandler,
            @Nullable CooldownHandler cooldownHandler,
            @Nullable DatabaseAdapter database
    ) {
        this.guildHandler = guildHandler;
        this.arenaHandler = arenaHandler;
        this.database = database;

        if (guildHandler != null) {
            final List<Guild> present = guildHandler.getGuildsForSnapshot();
            pending = new ArrayList<>(present.size());
            for (Guild guild : present) {
                pending.add(guild.getId());
            }
        } else {
            pending = new ArrayList<>();
        }

        if (challengeHandler != null) {
            pendingChallenges.addAll(challengeHandler.getChallengesForSnapshot());
        }

        if (cooldownHandler != null) {
            cooldowns.addAll(cooldownHandler.getCooldownsForSnapshot());
        }
    }

    /**
     * Whether there is any guild work left to do.
     *
     * @return true while guilds remain to serialise
     */
    public boolean hasWork() {
        return index < pending.size() || challengeIndex < pendingChallenges.size();
    }

    /**
     * How many records are still to serialise, guilds and challenges together.
     *
     * @return the number of pending records
     */
    public int remaining() {
        return pending.size() - index + pendingChallenges.size() - challengeIndex;
    }

    /**
     * Serialises as many guilds as fit in the budget.
     *
     * <p>Must be called on the main thread.
     *
     * <p>At least one guild is always serialised, even when that overshoots the budget, so a capture
     * always makes progress and always terminates. A guild costs at most about half a millisecond with
     * full vaults, which bounds the overshoot.
     *
     * @param budgetNanos the time budget for this call
     * @return true when every guild has been serialised and {@link #finish()} can be called
     */
    public boolean step(long budgetNanos) {
        final long deadline = System.nanoTime() + budgetNanos;

        while (index < pending.size()) {
            final UUID id = pending.get(index++);

            if (guildHandler != null) {
                // Re-read rather than trusting the id list. A guild deleted since the capture began must
                // not be written back, or this save undoes the delete in `GuildHandler#removeGuild`.
                final Guild guild = guildHandler.getGuilds().get(id);
                if (guild != null) {
                    guilds.put(id.toString(), guildHandler.serializeGuild(guild));
                }
            }

            if (System.nanoTime() >= deadline) {
                return false;
            }
        }

        while (challengeIndex < pendingChallenges.size()) {
            final GuildChallenge challenge = pendingChallenges.get(challengeIndex++);
            challenges.put(challenge.getId().toString(), Guilds.getGson().toJson(challenge, GuildChallenge.class));

            if (System.nanoTime() >= deadline) {
                return false;
            }
        }

        return true;
    }

    /**
     * Captures the arenas and returns the finished snapshot.
     *
     * <p>Must be called on the main thread, once {@link #step} has reported the guilds are done. This is
     * deliberately last rather than first: see the class javadoc on why the arena key set has to be as
     * close to the write as possible.
     *
     * @return the captured state, detached from everything that changes after this call
     */
    @NotNull public PluginSnapshot finish() {
        final Map<String, String> arenas = new LinkedHashMap<>();

        if (arenaHandler != null) {
            for (Arena arena : arenaHandler.getArenasForSnapshot()) {
                arenas.put(arena.getId().toString(), Guilds.getGson().toJson(arena, Arena.class));
            }
        }

        // Drop any guild that was serialised earlier in this capture and has since been deleted.
        //
        // `step` already re-reads each guild before serialising it, which covers a guild removed before its
        // turn. This covers the other order: a guild serialised on tick two and disbanded on tick three was
        // a true snapshot when it was taken, but `removeGuild` deleted the row and this capture would write
        // it straight back a second later.
        //
        // That is not self-correcting. `GuildAdapter` upserts by id and has no delete pass, so a record a
        // save does not mention is left alone rather than removed. Resurrecting a disbanded guild would
        // therefore be permanent: its balance, vaults and home would come back and there would be no way
        // to clear them from inside the plugin.
        //
        // The window this closes is the whole capture, which at a large guild count is several seconds
        // rather than the fraction of a second a single-pass save had.
        if (guildHandler != null) {
            guilds.keySet().removeIf(id -> guildHandler.getGuilds().get(UUID.fromString(id)) == null);
        }

        return new PluginSnapshot(guilds, arenas, challenges, cooldowns, database);
    }
}
