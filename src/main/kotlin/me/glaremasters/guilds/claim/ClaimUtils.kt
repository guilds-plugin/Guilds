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
import java.util.Optional
import me.glaremasters.guilds.configuration.sections.ClaimSettings
import me.glaremasters.guilds.configuration.sections.HooksSettings
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.WrappedState
import org.codemc.worldguardwrapper.region.IWrappedDomain
import org.codemc.worldguardwrapper.region.IWrappedRegion
import org.codemc.worldguardwrapper.selection.ICuboidSelection

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
    fun getNextAvailableClaimName(guild: Guild): String {
        val num = getClaimAmount(guild) + 1
        return "${guild.id}-${num}"
        //return guild.id.toString()
    }

    @JvmStatic
    fun getNumFromName(name: String): Int {
        return name.substringAfterLast("-").toInt()
    }

    @JvmStatic
    fun getClaimNameFormat(guild: Guild, int: Int): String {
        return "${guild.id}-${int}"
    }

    @JvmStatic
    fun getClaimNames(wrapper: WorldGuardWrapper, guild: Guild): List<String> {
        val claims = mutableListOf<String>()
        for (claim in guild.claimedLand) {
            claims.add(claim.region.get().id.toString())
        }
        return claims
    }

    @JvmStatic
    fun getClaimNumbers(guild: Guild): List<Int> {
        val claims = mutableListOf<Int>()
        for (claim in guild.claimedLand) {
            claims.add(claim.num)
        }
        return claims
    }

    @JvmStatic
    fun getClaimAmount(guild: Guild): Int {
        return guild.claimedLand.size
    }

    @JvmStatic
    fun getClaims(guild: Guild): List<GuildClaim> {
        return guild.claimedLand
    }

    @JvmStatic
    fun findRegionClaims(wrapper: WorldGuardWrapper, world: World, guild: Guild): List<Optional<IWrappedRegion>> {
        val claims = mutableListOf<Optional<IWrappedRegion>>()
        for (claim in guild.claimedLand) {
            val tempRegion = wrapper.getRegion(world, getClaimNameFormat(guild, claim.num))
            try {
                tempRegion.get().id
                claims.add(tempRegion)
            } catch (ex: Exception) {
                continue
            }
        }
        return claims
    }

    @JvmStatic
    fun findRegionClaims(wrapper: WorldGuardWrapper, guild: Guild): List<Optional<IWrappedRegion>> {
        val claims = mutableListOf<Optional<IWrappedRegion>>()
        for (world in Bukkit.getWorlds()) {
            for (claim in guild.claimedLand) {
                val tempRegion = wrapper.getRegion(world, getClaimNameFormat(guild, claim.num))
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
    fun getGuildClaimFromRegion(region: Optional<IWrappedRegion>, guild: Guild): GuildClaim? {
        for (claim in guild.claimedLand) {
            if (claim.region.equals(region)) {
                return claim
            }
        }
        return null
    }

    @JvmStatic
    fun getGuildClaimFromRegion(region: IWrappedRegion, guild: Guild): GuildClaim? {
        for (claim in guild.claimedLand) {
            if (claim.region.equals(region)) {
                return claim
            }
        }
        return null
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
        return guild.claimedLand.size >= 10
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
        for (claim in findRegionClaims(wrapper, guild)) {
            if (regions(wrapper, player).contains(claim.get())) {
                return getGuildClaimFromRegion(claim, guild)
            }
        }
        return null
    }















    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, player: Player): GuildClaim {
        getNextAvailableClaimName(guild).also {
            wrapper.addCuboidRegion(it, claimPointOne(player), claimPointTwo(player))
            return GuildClaim.builder().guildID(guild.id).num(getNumFromName(it)).name(it).region(wrapper.getRegion(player.world, it)).build()
        }
    }

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, selection: ICuboidSelection): GuildClaim {
        getNextAvailableClaimName(guild).also {
            wrapper.addCuboidRegion(it, selection.minimumPoint, selection.maximumPoint)
            return GuildClaim.builder().guildID(guild.id).num(getNumFromName(it)).name(it).region(wrapper.getRegion(selection.maximumPoint.world!!, it)).build()
        }
    }

    @JvmStatic
    fun removeAllClaims(wrapper: WorldGuardWrapper, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            for (claim in guild.claimedLand) {
                claim.region.get().id?.let { wrapper.removeRegion(world, it) }
            }
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, guild: Guild, num: Int) {
        for (world in Bukkit.getWorlds()) {
            for (claim in findRegionClaims(wrapper, guild)) {
                if (getClaimNameFormat(guild, num) == claim.get().id) {
                    wrapper.removeRegion(world, getClaimNameFormat(guild, num))
                }
            }
        }
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, guildClaim: GuildClaim, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            for (claim in findRegionClaims(wrapper, guild)) {
                if (guildClaim.region.get().id == claim.get().id) {
                    guildClaim.region.get().id?.let { wrapper.removeRegion(world, it) }
                }
            }
        }
    }

    @JvmStatic
    fun getClaim(wrapper: WorldGuardWrapper, player: Player, name: String, guild: Guild): GuildClaim {
        return getGuildClaimFromRegion(wrapper.getRegion(player.world, name), guild)!!
    }

    @JvmStatic
    fun addOwner(claim: GuildClaim, guild: Guild) {
        claim.region.get().owners.addPlayer(guild.guildMaster.uuid)
    }

    @JvmStatic
    fun removeOwner(claim: GuildClaim, player: Player) {
        claim.region.get().owners.removePlayer(player.uniqueId)
    }

    @JvmStatic
    fun getMembers(claim: GuildClaim): IWrappedDomain {
        return claim.region.get().members
    }

    @JvmStatic
    fun addMembers(claim: GuildClaim, guild: Guild) {
        guild.members.forEach {
            getMembers(claim).addPlayer(it.uuid)
        }
    }

    @JvmStatic
    fun addMember(claim: GuildClaim, player: Player) {
        getMembers(claim).addPlayer(player.uniqueId)
    }

    @JvmStatic
    fun removeMember(claim: GuildClaim, player: OfflinePlayer) {
        getMembers(claim).removePlayer(player.uniqueId)
    }

    @JvmStatic
    fun kickMember(playerKicked: OfflinePlayer, playerExecuting: Player, guild: Guild) {
        val wrapper = WorldGuardWrapper.getInstance()
        for (claim in guild.claimedLand) {
            removeMember(claim, playerKicked)
        }
    }

    @JvmStatic
    fun setEnterMessage(wrapper: WorldGuardWrapper, claim: GuildClaim, settingsManager: SettingsManager, guild: Guild) {
        claim.region.get().setFlag(wrapper.getFlag("greeting", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }

    @JvmStatic
    fun setExitMessage(wrapper: WorldGuardWrapper, claim: GuildClaim, settingsManager: SettingsManager, guild: Guild) {
        claim.region.get().setFlag(wrapper.getFlag("farewell", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }

    @JvmStatic
    fun checkPvpDisabled(player: Player): Boolean {
        val wrapper = WorldGuardWrapper.getInstance()
        val flag = wrapper.getFlag("pvp", WrappedState::class.java)
        var state = WrappedState.ALLOW

        if (!flag.isPresent) {
            return false
        }

        val check = flag.map { f -> wrapper.queryFlag(player, player.location, f) }
        check.ifPresent {
            state = try {
                it.get()
            } catch (ex: Exception) {
                WrappedState.ALLOW
            }
        }
        return state == WrappedState.DENY
    }

    @JvmStatic
    fun deleteWithGuild(guild: Guild) {

        val wrapper = WorldGuardWrapper.getInstance()

        if (!checkIfHaveClaims(wrapper, guild)) {
            return
        }

        removeAllClaims(wrapper, guild)
        guild.claimedLand.clear()
    }
}
