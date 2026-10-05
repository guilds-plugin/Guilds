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
