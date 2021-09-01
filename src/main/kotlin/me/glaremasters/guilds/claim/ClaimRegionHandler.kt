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
import java.util.*

object ClaimRegionHandler {

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, player: Player): GuildClaim {
        val name = ClaimUtils.getNextAvailableClaimName()
        wrapper.addCuboidRegion(name.toString(), ClaimUtils.claimPointOne(player), ClaimUtils.claimPointTwo(player))
        println(GuildClaim.builder().name(name).guildId(guild.id).build())
        return GuildClaim.builder().name(name).guildId(guild.id).build()
    }

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, selection: ICuboidSelection): GuildClaim {
        val name = ClaimUtils.getNextAvailableClaimName()

        wrapper.addCuboidRegion(name.toString(), selection.minimumPoint, selection.maximumPoint)
        return GuildClaim.builder().name(name).guildId(guild.id).build()

    }


    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, name: UUID) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, name.toString())
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, claim: GuildClaim) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, claim.name.toString())
        }
    }

    @JvmStatic
    fun removeAllClaims(wrapper: WorldGuardWrapper, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            for (claim in guild.claimedLand) {
                wrapper.removeRegion(world, claim.name.toString())
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