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

import co.aikar.commands.ACFBukkitUtil
import java.util.UUID
import org.bukkit.Location

/**
 * Represents an Arena with unique id and name.
 *
 * @property id the unique id of the arena.
 * @property name the name of the arena.
 * @property challenger the challenger location string representation, can be null.
 * @property defender the defender location string representation, can be null.
 * @property inUse boolean flag indicating whether the arena is currently in use.
 */
data class Arena(
    val id: UUID,
    val name: String,
    var challenger: String?,
    var defender: String?,
    @Transient var inUse: Boolean
) {

    /**
     * Constructs a new Arena with id and name.
     *
     * @param id the unique id of the arena.
     * @param name the name of the arena.
     */
    constructor(id: UUID, name: String) : this(id, name, null, null, false)

    /**
     * Returns the Location object for the challenger.
     *
     * @return the Location object for the challenger, null if the string representation is null.
     */
    val challengerLoc: Location?
        get() = ACFBukkitUtil.stringToLocation(challenger)

    /**
     * Returns the Location object for the defender.
     *
     * @return the Location object for the defender, null if the string representation is null.
     */
    val defenderLoc: Location?
        get() = ACFBukkitUtil.stringToLocation(defender)
}
