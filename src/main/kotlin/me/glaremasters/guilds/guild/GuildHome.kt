package me.glaremasters.guilds.guild

import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * Created by GlareMasters
 * Date: 2/14/2019
 * Time: 9:33 AM
 */
class GuildHome(private val world: String, private val x: Double, private val y: Double, private val z: Double, private val yaw: Float, private val pitch: Float) {

    val asLocation: Location
        get() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
}
