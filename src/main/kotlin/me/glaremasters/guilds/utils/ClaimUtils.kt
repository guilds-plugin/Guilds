/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
package me.glaremasters.guilds.utils

import ch.jalu.configme.SettingsManager
import java.util.Optional
import me.glaremasters.guilds.configuration.sections.ClaimSettings
import me.glaremasters.guilds.configuration.sections.HooksSettings
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
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
    fun getRadius(settingsManager: SettingsManager): Int {
        return settingsManager.getProperty(ClaimSettings.RADIUS)
    }

    @JvmStatic
    fun claimPointOne(player: Player, settingsManager: SettingsManager): Location {
        val y = try {
            player.world.minHeight
        } catch (error: NoSuchMethodError) {
            0
        }
        return player.location.subtract(getRadius(settingsManager).toDouble(), player.location.y - y, getRadius(settingsManager).toDouble())
    }

    @JvmStatic
    fun claimPointTwo(player: Player, settingsManager: SettingsManager): Location {
        val y = try {
            player.world.minHeight
        } catch (error: NoSuchMethodError) {
            0
        }
        return player.location.add(getRadius(settingsManager).toDouble(), y - player.location.y, getRadius(settingsManager).toDouble())
    }

    @JvmStatic
    fun getClaimName(guild: Guild): String {
        return guild.id.toString()
    }

    @JvmStatic
    fun checkAlreadyExist(wrapper: WorldGuardWrapper, guild: Guild): Boolean {
        for (world in Bukkit.getWorlds()) {
            val tempRegion = wrapper.getRegion(world, getClaimName(guild))
            try {
                tempRegion.get().id
                return true
            } catch (ex: Exception) {
                continue
            }
        }
        return false
    }

    @JvmStatic
    fun checkAlreadyExist(wrapper: WorldGuardWrapper, player: Player, name: String): Boolean {
        return wrapper.getRegion(player.world, name).isPresent
    }

    @JvmStatic
    fun regions(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager): Set<IWrappedRegion> {
        return wrapper.getRegions(claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager))
    }

    @JvmStatic
    fun checkOverlap(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager): Boolean {
        return regions(wrapper, player, settingsManager).isNotEmpty()
    }

    @JvmStatic
    fun isInDisabledWorld(player: Player, settingsManager: SettingsManager): Boolean {
        return settingsManager.getProperty(ClaimSettings.DISABLED_WORLDS).contains(player.world.name)
    }

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, player: Player, settingsManager: SettingsManager) {
        wrapper.addCuboidRegion(getClaimName(guild), claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager))
    }

    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, selection: ICuboidSelection) {
        wrapper.addCuboidRegion(getClaimName(guild), selection.minimumPoint, selection.maximumPoint)
    }

    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, getClaimName(guild))
        }
    }

    @JvmStatic
    fun getGuildClaim(wrapper: WorldGuardWrapper, player: Player, guild: Guild): Optional<IWrappedRegion> {
        return wrapper.getRegion(player.world, getClaimName(guild))
    }

    @JvmStatic
    fun getClaim(wrapper: WorldGuardWrapper, player: Player, name: String): Optional<IWrappedRegion> {
        return wrapper.getRegion(player.world, name)
    }

    @JvmStatic
    fun getSelection(wrapper: WorldGuardWrapper, player: Player, name: String): ICuboidSelection {
        return wrapper.getRegion(player.world, name).get().selection as ICuboidSelection
    }

    @JvmStatic
    fun addOwner(claim: IWrappedRegion, guild: Guild) {
        claim.owners.addPlayer(guild.guildMaster.uuid)
    }

    @JvmStatic
    fun getMembers(claim: IWrappedRegion): IWrappedDomain {
        return claim.members
    }

    @JvmStatic
    fun addMembers(claim: IWrappedRegion, guild: Guild) {
        guild.members.forEach {
            getMembers(claim).addPlayer(it.uuid)
        }
    }

    @JvmStatic
    fun addMember(claim: IWrappedRegion, player: Player) {
        getMembers(claim).addPlayer(player.uniqueId)
    }

    @JvmStatic
    fun removeMember(claim: IWrappedRegion, player: OfflinePlayer) {
        getMembers(claim).removePlayer(player.uniqueId)
    }

    @JvmStatic
    fun kickMember(playerKicked: OfflinePlayer, playerExecuting: Player, guild: Guild, settingsManager: SettingsManager) {
        if (isEnable(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()
            getGuildClaim(wrapper, playerExecuting, guild).ifPresent { r -> removeMember(r, playerKicked) }
        }
    }

    @JvmStatic
    fun setEnterMessage(wrapper: WorldGuardWrapper, claim: IWrappedRegion, settingsManager: SettingsManager, guild: Guild) {
        claim.setFlag(wrapper.getFlag("greeting", String::class.java).orElse(null), StringUtils.color(settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.name).replace("{prefix}", guild.prefix)))
    }

    @JvmStatic
    fun setExitMessage(wrapper: WorldGuardWrapper, claim: IWrappedRegion, settingsManager: SettingsManager, guild: Guild) {
        claim.setFlag(wrapper.getFlag("farewell", String::class.java).orElse(null), StringUtils.color(settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.name).replace("{prefix}", guild.prefix)))
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
    fun deleteWithGuild(guild: Guild, settingsManager: SettingsManager) {
        if (!isEnable(settingsManager)) {
            return
        }
        val wrapper = WorldGuardWrapper.getInstance()
        if (!checkAlreadyExist(wrapper, guild)) {
            return
        }
        removeClaim(wrapper, guild)
    }
}
