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
package me.glaremasters.guilds.claim

import ch.jalu.configme.SettingsManager
import me.glaremasters.guilds.configuration.sections.ClaimSettings
import me.glaremasters.guilds.configuration.sections.HooksSettings
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.region.IWrappedRegion
import org.codemc.worldguardwrapper.selection.ICuboidSelection
import java.util.*

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 9:44 PM
 */
object ClaimUtils {

    @JvmStatic
    fun isEnable(settingsManager: SettingsManager): Boolean {
        return settingsManager.getProperty(HooksSettings.WORLDGUARD)
    }

    @JvmStatic
    fun claimPointOne(player: Player): Location {
        return player.location.chunk.getBlock(0, player.world.minHeight, 0).location
        //return player.location.subtract(getRadius(settingsManager).toDouble(), player.location.y, getRadius(settingsManager).toDouble())
    }

    @JvmStatic
    fun claimPointTwo(player: Player): Location {
        return player.location.chunk.getBlock(15, player.world.maxHeight, 15).location
        //return player.location.add(getRadius(settingsManager).toDouble(), player.world.maxHeight - player.location.y, getRadius(settingsManager).toDouble())
    }

    @JvmStatic
    fun getNextAvailableClaimName(): UUID {
        return UUID.randomUUID()
        //return guild.id.toString()
    }

    @JvmStatic
    fun getClaimAmount(guild: Guild): Int {
        return if (!guild.claimedLand.isNullOrEmpty()) {
            guild.claimedLand.size
        } else {
            0
        }
    }

    @JvmStatic
    fun getSelection(wrapper: WorldGuardWrapper, player: Player, name: String): ICuboidSelection {
        return wrapper.getRegion(player.world, name).get().selection as ICuboidSelection
    }

    @JvmStatic
    fun regions(wrapper: WorldGuardWrapper, player: Player): Set<IWrappedRegion> {
        return wrapper.getRegions(claimPointOne(player), claimPointTwo(player))
    }

    @JvmStatic
    fun checkOverlap(wrapper: WorldGuardWrapper, player: Player): Boolean {
        return regions(wrapper, player).isNotEmpty()
    }

    @JvmStatic
    fun isInDisabledWorld(player: Player, settingsManager: SettingsManager): Boolean {
        return settingsManager.getProperty(ClaimSettings.DISABLED_WORLDS).contains(player.world.name)
    }

    @JvmStatic
    fun getStandingOnClaim(wrapper: WorldGuardWrapper, player: Player, guild: Guild): GuildClaim? {
        for (claim in guild.claimedLand) {
            for (region in regions(wrapper, player)) {
                if (region.id.toString().equals(claim.getRegion(wrapper).id.toString())) {
                    return claim
                }
            }
        }
        return null
    }


//    @JvmStatic
//    fun findRegionClaims(wrapper: WorldGuardWrapper, world: World, guild: Guild): List<Optional<IWrappedRegion>> {
//        val claims = mutableListOf<Optional<IWrappedRegion>>()
//        for (claim in guild.claimedLand) {
//            val tempRegion = wrapper.getRegion(world, claim.name.toString())
//            try {
//                tempRegion.get().id
//                claims.add(tempRegion)
//            } catch (ex: Exception) {
//                continue
//            }
//        }
//        return claims
//    }

    @JvmStatic
    fun findRegionClaims(wrapper: WorldGuardWrapper, guild: Guild): List<Optional<IWrappedRegion>> {
        val claims = mutableListOf<Optional<IWrappedRegion>>()
        for (world in Bukkit.getWorlds()) {
            for (claim in guild.claimedLand) {
                val tempRegion = wrapper.getRegion(world, claim.name.toString())
                try {
                    tempRegion.get().id
                    claims.add(tempRegion)
                } catch (ex: Exception) {
                    continue
                }
            }
        }
        return claims
    }

    @JvmStatic
    fun getGuildClaimFromRegion(wrapper: WorldGuardWrapper, region: Optional<IWrappedRegion>, guild: Guild): GuildClaim? {
        for (claim in guild.claimedLand) {
            if (claim.getRegion(wrapper).equals(region.get())) {
                return claim
            }
        }
        return null
    }

    @JvmStatic
    fun getGuildClaimFromRegion(wrapper: WorldGuardWrapper, region: IWrappedRegion, guild: Guild): GuildClaim? {
        for (claim in guild.claimedLand) {
            if (claim.getRegion(wrapper).equals(region)) {
                return claim
            }
        }
        return null
    }

    @JvmStatic
    fun getRegionFromName(wrapper: WorldGuardWrapper, name: UUID): IWrappedRegion? {
        for (world in Bukkit.getWorlds()) {
            val tempRegion = wrapper.getRegion(world, name.toString())
            return try {
                tempRegion.get().id
                tempRegion.get()
            } catch (ex: Exception) {
                null
            }
        }
        return null
    }

    @JvmStatic
    fun getRegionFromName(wrapper: WorldGuardWrapper, name: String): IWrappedRegion? {
        for (world in Bukkit.getWorlds()) {
            val tempRegion = wrapper.getRegion(world, name)
            return try {
                tempRegion.get().id
                tempRegion.get()
            } catch (ex: Exception) {
                null
            }
        }
        return null
    }

    @JvmStatic
    fun getClaimFromName(wrapper: WorldGuardWrapper, player: Player, name: String, guild: Guild): GuildClaim {
        return getGuildClaimFromRegion(wrapper, wrapper.getRegion(player.world, name), guild)!!
    }

    @JvmStatic
    fun checkIfHaveClaims(wrapper: WorldGuardWrapper, guild: Guild): Boolean {
        val foundClaims = findRegionClaims(wrapper, guild)
        return if (guild.claimedLand.size == 0) {
            false
        } else {
            !foundClaims.isNullOrEmpty()
        }
//        for (world in Bukkit.getWorlds()) {
//            guild.tier.claimableLand
//            val tempRegion = wrapper.getRegion(world, getAvailableClaimName(guild))
//            try {
//                tempRegion.get().id
//                return true
//            } catch (ex: Exception) {
//                continue
//            }
//        }
//        return false
    }

    @JvmStatic
    fun checkMaxAlreadyExist(wrapper: WorldGuardWrapper, guild: Guild): Boolean {
        return if (!guild.claimedLand.isNullOrEmpty()) {
            guild.claimedLand.size >= 10
        } else {
            false
        }
//        for (world in Bukkit.getWorlds()) {
//            val tempRegion = wrapper.getRegion(world, getClaimNameFormat(guild, guild.tier.claimableLand))
//            try {
//                tempRegion.get().id
//                return true
//            } catch (ex: Exception) {
//                continue
//            }
//        }
//        return false
    }
}
