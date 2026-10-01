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

import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.DatabaseAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable point-in-time copy of everything the plugin persists, in the exact bytes each backend
 * will receive.
 *
 * <p>These are deliberately <em>serialised</em> records rather than copies of the live models. A
 * {@code List<Guild>} taken on the main thread is not a snapshot of anything: the objects in it are
 * the same instances the main thread keeps mutating, so Gson would read a moving target on the write
 * thread. The payload is byte-identical to what the old code produced, because it comes from the same
 * {@code Guilds.getGson().toJson(...)} call, just earlier.
 *
 * <p>Cooldowns are the exception. {@link Cooldown} has four {@code final} fields and no setters, so
 * a list of references is already fully detached.
 *
 * <p>Instances are produced on the main thread and consumed anywhere. They are safe to publish to
 * another thread without further synchronisation because every field is final and every collection
 * is unmodifiable.
 */
public final class PluginSnapshot {

    private final Map<String, String> guilds;
    private final Map<String, String> arenas;
    private final Map<String, String> challenges;
    private final List<Cooldown> cooldowns;
    private final DatabaseAdapter database;

    /**
     * Creates a snapshot from already-serialised records.
     *
     * @param guilds     guild id to serialised guild JSON
     * @param arenas     arena id to serialised arena JSON
     * @param challenges challenge id to serialised challenge JSON
     * @param cooldowns  the live cooldowns, which are immutable and need no copying beyond the list
     * @param database   the backend this snapshot was captured against
     */
    public PluginSnapshot(
            @NotNull Map<String, String> guilds,
            @NotNull Map<String, String> arenas,
            @NotNull Map<String, String> challenges,
            @NotNull Collection<Cooldown> cooldowns,
            @Nullable DatabaseAdapter database
    ) {
        this.guilds = Collections.unmodifiableMap(new LinkedHashMap<>(guilds));
        this.arenas = Collections.unmodifiableMap(new LinkedHashMap<>(arenas));
        this.challenges = Collections.unmodifiableMap(new LinkedHashMap<>(challenges));
        this.cooldowns = Collections.unmodifiableList(new ArrayList<>(cooldowns));
        this.database = database;
    }

    /**
     * The backend this snapshot was captured against.
     *
     * <p>A save must write here rather than re-reading the plugin's current backend: migration replaces
     * that mid-save, and a writer that re-read the field would split its records across two backends.
     *
     * @return the captured adapter, or null if the plugin had no database
     */
    @Nullable public DatabaseAdapter getDatabase() {
        return database;
    }

    /**
     * Serialised guild records, keyed by guild id.
     *
     * @return unmodifiable map of id to JSON
     */
    @NotNull public Map<String, String> getGuilds() {
        return guilds;
    }

    /**
     * Serialised arena records, keyed by arena id.
     *
     * <p>The keys matter on their own: {@code ArenaAdapter} deletes every stored arena missing from the
     * collection it is given, so a truncated key set is data loss rather than a skipped write.
     *
     * @return unmodifiable map of id to JSON
     */
    @NotNull public Map<String, String> getArenas() {
        return arenas;
    }

    /**
     * Serialised challenge records, keyed by challenge id.
     *
     * @return unmodifiable map of id to JSON
     */
    @NotNull public Map<String, String> getChallenges() {
        return challenges;
    }

    /**
     * The cooldowns in force at capture time.
     *
     * @return unmodifiable list of immutable cooldowns
     */
    @NotNull public List<Cooldown> getCooldowns() {
        return cooldowns;
    }

    /**
     * Whether this snapshot holds nothing worth writing.
     *
     * @return true when every collection is empty
     */
    public boolean isEmpty() {
        return guilds.isEmpty() && arenas.isEmpty() && challenges.isEmpty() && cooldowns.isEmpty();
    }
}
