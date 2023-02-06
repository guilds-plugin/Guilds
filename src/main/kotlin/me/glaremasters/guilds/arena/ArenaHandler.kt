/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
     * Adds a [Arena] to the map of arenas.
     *
     * @param arena The [Arena] to be added.
     */
    fun addArena(arena: Arena) {
        arenas[arena.name.toLowerCase()] = arena
    }

    /**
     * Removes a [Arena] from the map of arenas.
     *
     * @param arena The [Arena] to be removed.
     */
    fun removeArena(arena: Arena) {
        arenas.remove(arena.name)
    }

    /**
     * Returns a collection of all the [Arena] objects in the map.
     *
     * @return The collection of [Arena] objects.
     */
    fun getArenas(): Collection<Arena> {
        return arenas.values
    }

    /**
     * Returns an [Optional] object containing the [Arena] object with the given name.
     *
     * @param name The name of the [Arena].
     * @return An [Optional] object containing the [Arena] object.
     */
    fun getArena(name: String): Optional<Arena> {
        return Optional.ofNullable(arenas[name.toLowerCase()])
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
     */
    fun saveArenas() {
        guilds.database.arenaAdapter.saveArenas(arenas.values)
    }
}
