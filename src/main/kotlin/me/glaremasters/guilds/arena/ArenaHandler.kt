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
package me.glaremasters.guilds.arena

import me.glaremasters.guilds.Guilds
import java.util.*

/**
 * The `ArenaHandler` class manages and holds all the [Arena] objects.
 *
 * @property guilds The [Guilds] object which holds the [ArenaHandler].
 * @property arenas The map of [Arena] objects, with their names as the key.
 */
class ArenaHandler(private val guilds: Guilds) {

    private val arenas = mutableMapOf<String, Arena>()

    /**
     * Builds the map key an [Arena] is stored under.
     *
     * Add, get, and remove all have to agree on this, or a lookup misses an arena that is present.
     * `Locale.ROOT` is deliberate: the default locale would map `"I"` to a dotless `"ı"` on a
     * Turkish-locale JVM, so the same name could resolve on one path and miss on another.
     *
     * @param name The raw [Arena] name.
     * @return The normalised key.
     */
    private fun key(name: String): String = name.lowercase(Locale.ROOT)

    /**
     * Adds a [Arena] to the map of arenas.
     *
     * @param arena The [Arena] to be added.
     */
    fun addArena(arena: Arena) {
        arenas[key(arena.name)] = arena
    }

    /**
     * Removes a [Arena] from the map of arenas.
     *
     * @param arena The [Arena] to be removed.
     */
    fun removeArena(arena: Arena) {
        arenas.remove(key(arena.name))
    }

    /**
     * Returns a collection of all the [Arena] objects in the map.
     *
     * <p>This is a live view, not a copy, and it stays that way for the GUI and command callers that
     * read it on the main thread. Do not hand it to a background thread: iterating it while
     * `/guilds arena create` or `delete` runs a structural change throws, and
     * `ArenaAdapter#saveArenas` deletes every stored arena missing from the collection it is given, so
     * a short read is data loss. Use [getArenasForSnapshot] for anything that leaves the main thread.
     *
     * @return The collection of [Arena] objects.
     */
    fun getArenas(): Collection<Arena> {
        return arenas.values
    }

    /**
     * Returns every arena in a list safe to iterate once the caller has left the main thread's
     * exclusive ownership.
     *
     * <p>The [Arena] instances are still live and mutable. This is only safe because the caller
     * serialises them here, on the main thread, and then works from the resulting strings.
     *
     * @return a detached list of the arenas
     */
    fun getArenasForSnapshot(): List<Arena> {
        return ArrayList(arenas.values)
    }

    /**
     * Returns an [Optional] object containing the [Arena] object with the given name.
     *
     * @param name The name of the [Arena].
     * @return An [Optional] object containing the [Arena] object.
     */
    fun getArena(name: String): Optional<Arena> {
        return Optional.ofNullable(arenas[key(name)])
    }

    /**
     * Returns an [Optional] object containing the first [Arena] object which is not in use.
     *
     * @return An [Optional] object containing the [Arena] object.
     */
    fun getAvailableArena(): Optional<Arena> {
        return Optional.ofNullable(arenas.values.shuffled().firstOrNull { !it.inUse })
    }

    /**
     * Returns a list of the names of all the [Arena] objects in the map.
     *
     * @return The list of arena names.
     */
    fun arenaNames(): List<String> {
        return getArenas().map { it.name }
    }

    /**
     * Loads all the [Arena] objects from the database and adds them to the map of arenas.
     */
    fun loadArenas() {
        guilds.database.arenaAdapter.allArenas.forEach(this::addArena)
    }

    /**
     * Saves all the [Arena] objects in the map to the database.
     *
     * <p>Serialises on the calling thread and hands the adapter strings rather than live arenas. It
     * used to pass `arenas.values` straight through, which is a live view: a save running while an
     * arena was created or deleted either threw `ConcurrentModificationException` or, in the delete
     * pass, deleted an arena it had never seen.
     */
    fun saveArenas() {
        val serialized = LinkedHashMap<String, String>()
        for (arena in getArenasForSnapshot()) {
            serialized[arena.id.toString()] = Guilds.getGson().toJson(arena, Arena::class.java)
        }
        guilds.database.arenaAdapter.saveSerialized(serialized)
    }
}
