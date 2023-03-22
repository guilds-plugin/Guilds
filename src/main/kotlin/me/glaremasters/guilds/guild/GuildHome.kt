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
package me.glaremasters.guilds.guild

import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * A class representing a player's home location in a specific world.
 *
 * @property world A string representing the name of the world the home location is in.
 * @property x A double representing the X coordinate of the home location.
 * @property y A double representing the Y coordinate of the home location.
 * @property z A double representing the Z coordinate of the home location.
 * @property yaw A float representing the yaw of the home location.
 * @property pitch A float representing the pitch of the home location.
 *
 * @constructor Creates an instance of GuildHome.
 */
class GuildHome(
    private val world: String,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float,
    private val pitch: Float
) {

    /**
     * Gets the location of the guild home as a Bukkit location object.
     *
     * @return A Location object representing the guild home location.
     */
    val asLocation: Location
        get() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
}