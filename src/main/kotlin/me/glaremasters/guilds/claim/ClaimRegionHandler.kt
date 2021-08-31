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

import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.selection.ICuboidSelection

object ClaimRegionHandler {

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, player: Player): GuildClaim {
        ClaimUtils.getNextAvailableClaimName(guild).also {
            wrapper.addCuboidRegion(it, ClaimUtils.claimPointOne(player), ClaimUtils.claimPointTwo(player))
            return GuildClaim.builder().guildId(guild.id).number(ClaimUtils.getNumFromName(it)).name(it).build()
        }
    }

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, selection: ICuboidSelection): GuildClaim {
        ClaimUtils.getNextAvailableClaimName(guild).also {
            wrapper.addCuboidRegion(it, selection.minimumPoint, selection.maximumPoint)
            return GuildClaim.builder().guildId(guild.id).number(ClaimUtils.getNumFromName(it)).name(it).build()
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, num: Int, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, ClaimUtils.getClaimNameFormat(guild, num))
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, name: String) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, name)
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, claim: GuildClaim) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, claim.getRegion(wrapper).id)
        }
    }

    @JvmStatic
    fun removeAllClaims(wrapper: WorldGuardWrapper, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            for (claim in guild.claimedLand) {
                wrapper.removeRegion(world, claim.getRegion(wrapper).id)
            }
        }
    }

    @JvmStatic
    fun deleteWithGuild(wrapper: WorldGuardWrapper, guild: Guild) {
        if (!ClaimUtils.checkIfHaveClaims(wrapper, guild)) {
            return
        }
        removeAllClaims(wrapper, guild)
        guild.claimedLand.clear()
    }
}