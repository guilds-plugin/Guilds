/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

import java.util.Optional
import me.glaremasters.guilds.Guilds

class ArenaHandler(private val guilds: Guilds) {

    private val arenas = mutableMapOf<String, Arena>()

    fun addArena(arena: Arena) {
        arenas[arena.name.toLowerCase()] = arena
    }

    fun removeArena(arena: Arena) {
        arenas.remove(arena.name)
    }

    fun getArenas(): Collection<Arena> {
        return arenas.values
    }

    fun getArena(name: String): Optional<Arena> {
        return Optional.ofNullable(arenas[name.toLowerCase()])
    }

    fun getAvailableArena(): Optional<Arena> {
        return Optional.ofNullable(arenas.values.shuffled().firstOrNull { !it.inUse })
    }

    fun loadArenas() {
        guilds.database.arenaAdapter.allArenas.forEach(this::addArena)
    }

    fun saveArenas() {
        guilds.database.arenaAdapter.saveArenas(arenas.values)
    }
}
