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
     */
    fun saveArenas() {
        guilds.database.arenaAdapter.saveArenas(arenas.values)
    }
}
