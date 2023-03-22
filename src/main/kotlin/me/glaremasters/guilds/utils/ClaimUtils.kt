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
package me.glaremasters.guilds.utils

import ch.jalu.configme.SettingsManager
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
import java.util.*

/**
 * ClaimUtils is a class that provides utility methods for claim management in a server game.
 */
object ClaimUtils {

    /**
     * Returns a boolean value indicating if the claim feature is enabled or not.
     * @param settingsManager the [SettingsManager] instance to retrieve the configuration from.
     * @return true if the claim feature is enabled, false otherwise.
     */
    @JvmStatic
    fun isEnabled(settingsManager: SettingsManager): Boolean {
        return settingsManager.getProperty(HooksSettings.WORLDGUARD)
    }

    /**
     * Returns the radius value used for creating a claim.
     * @param settingsManager the [SettingsManager] instance to retrieve the configuration from.
     * @return the radius value.
     */
    @JvmStatic
    fun getRadius(settingsManager: SettingsManager): Int {
        return settingsManager.getProperty(ClaimSettings.RADIUS)
    }

    /**
     * Returns the first point of a claim based on the player's location and the radius.
     * @param player the [Player] instance representing the player.
     * @param settingsManager the [SettingsManager] instance to retrieve the configuration from.
     * @return the first point of the claim as a [Location].
     */
    @JvmStatic
    fun claimPointOne(player: Player, settingsManager: SettingsManager): Location {
        val radius = getRadius(settingsManager).toDouble()
        val x = player.location.x - radius
        val y = try {
            player.world.maxHeight.toDouble()
        } catch (error: NoSuchMethodError) {
            0.0
        }
        val z = player.location.z - radius
        return Location(player.world, x, y, z)
    }

    /**
     * Returns the second point of a claim based on the player's location and the radius.
     * @param player the [Player] instance representing the player.
     * @param settingsManager the [SettingsManager] instance to retrieve the configuration from.
     * @return the second point of the claim as a [Location].
     */
    @JvmStatic
    fun claimPointTwo(player: Player, settingsManager: SettingsManager): Location {
        val radius = getRadius(settingsManager).toDouble()
        val x = player.location.x + radius
        val y = try {
            player.world.minHeight.toDouble()
        } catch (error: NoSuchMethodError) {
            0.0
        }
        val z = player.location.z + radius
        return Location(player.world, x, y, z)
    }

    /**
     * Returns the name of a claim based on the given guild.
     * @param guild the [Guild] instance representing the guild.
     * @return the name of the claim.
     */
    @JvmStatic
    fun getClaimName(guild: Guild): String {
        return guild.id.toString()
    }


    /**
     * Returns a boolean value indicating if a claim with the same name as the given guild already exists.
     * @param wrapper the [WorldGuardWrapper] instance to use for checking.
     * @param guild the [Guild] instance representing the guild.
     * @return true if a claim with the same name already exists, false otherwise.
     */
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

    /**
     * Returns a boolean value indicating if a claim with the given name already exists in the player's world.
     * @param wrapper the [WorldGuardWrapper] instance to use for checking.
     * @param player the [Player] instance representing the player.
     * @param name the name of the claim to check.
     * @return true if a claim with the given name already exists, false otherwise.
     */
    @JvmStatic
    fun checkAlreadyExist(wrapper: WorldGuardWrapper, player: Player, name: String): Boolean {
        return wrapper.getRegion(player.world, name).isPresent
    }

    /**
     * Returns a set of regions that overlap with the claim being made by the player
     *
     * @param wrapper The [WorldGuardWrapper] instance
     * @param player The [Player] making the claim
     * @param settingsManager The [SettingsManager] instance
     *
     * @return A set of regions that overlap with the claim
     */
    @JvmStatic
    fun regions(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager): Set<IWrappedRegion> {
        return wrapper.getRegions(claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager))
    }

    /**
     * Checks if the claim made by the player overlaps with any other regions
     *
     * @param wrapper The [WorldGuardWrapper] instance
     * @param player The [Player] making the claim
     * @param settingsManager The [SettingsManager] instance
     *
     * @return True if the claim overlaps with another region, false otherwise
     */
    @JvmStatic
    fun checkOverlap(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager): Boolean {
        return regions(wrapper, player, settingsManager).isNotEmpty()
    }

    /**
     * Checks if the player's current world is disabled for claiming
     *
     * @param player The [Player] making the claim
     * @param settingsManager The [SettingsManager] instance
     *
     * @return True if the world is disabled for claiming, false otherwise
     */
    @JvmStatic
    fun isInDisabledWorld(player: Player, settingsManager: SettingsManager): Boolean {
        return settingsManager.getProperty(ClaimSettings.DISABLED_WORLDS).contains(player.world.name)
    }

    /**
     * Creates a claim for the specified guild using the player's selection
     *
     * @param wrapper The [WorldGuardWrapper] instance
     * @param guild The [Guild] making the claim
     * @param player The [Guild] making the claim
     * @param settingsManager The [SettingsManager] instance
     */
    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, player: Player, settingsManager: SettingsManager) {
        wrapper.addCuboidRegion(
            getClaimName(guild),
            claimPointOne(player, settingsManager),
            claimPointTwo(player, settingsManager)
        )
    }

    /**
     * Creates a claim for the specified guild using the provided selection
     *
     * @param wrapper The [WorldGuardWrapper] instance
     * @param guild The [Guild] making the claim
     * @param selection The selection for the claim
     */
    @JvmStatic
    fun createClaim(wrapper: WorldGuardWrapper, guild: Guild, selection: ICuboidSelection) {
        wrapper.addCuboidRegion(getClaimName(guild), selection.minimumPoint, selection.maximumPoint)
    }

    /**
     * Removes the claim for the specified guild
     *
     * @param wrapper The [WorldGuardWrapper] instance
     * @param guild The [Guild] whose claim is being removed
     */
    @JvmStatic
    fun removeClaim(wrapper: WorldGuardWrapper, guild: Guild) {
        for (world in Bukkit.getWorlds()) {
            wrapper.removeRegion(world, getClaimName(guild))
        }
    }

    /**
     * Returns the guild claim for the given [guild] and [player].
     *
     * @param wrapper The [WorldGuardWrapper] instance to use.
     * @param player The [Player] object representing the player.
     * @param guild The [Guild] object representing the guild.
     * @return An Optional [IWrappedRegion] representing the guild claim, if it exists.
     */
    @JvmStatic
    fun getGuildClaim(wrapper: WorldGuardWrapper, player: Player, guild: Guild): Optional<IWrappedRegion> {
        return wrapper.getRegion(player.world, getClaimName(guild))
    }

    /**
     * Returns the claim with the given [name] for the given [player].
     *
     * @param wrapper The [WorldGuardWrapper] instance to use.
     * @param player The [Player] object representing the player.
     * @param name The name of the claim to retrieve.
     * @return An Optional [IWrappedRegion] representing the claim, if it exists.
     */
    @JvmStatic
    fun getClaim(wrapper: WorldGuardWrapper, player: Player, name: String): Optional<IWrappedRegion> {
        return wrapper.getRegion(player.world, name)
    }

    /**
     * Returns the cuboid selection for the claim with the given [name] for the given [player].
     *
     * @param wrapper The [WorldGuardWrapper] instance to use.
     * @param player The [Player] object representing the player.
     * @param name The name of the claim to retrieve the selection for.
     * @return An [ICuboidSelection] representing the selection of the claim.
     */
    @JvmStatic
    fun getSelection(wrapper: WorldGuardWrapper, player: Player, name: String): ICuboidSelection {
        return wrapper.getRegion(player.world, name).get().selection as ICuboidSelection
    }

    /**
     * Adds the given [guild]'s guild master as an owner of the given [claim].
     *
     * @param claim The [IWrappedRegion] representing the claim.
     * @param guild The [Guild] object representing the guild.
     */
    @JvmStatic
    fun addOwner(claim: IWrappedRegion, guild: Guild) {
        claim.owners.addPlayer(guild.guildMaster.uuid)
    }

    /**
     * Returns the members of the given [claim].
     *
     * @param claim The [IWrappedRegion] representing the claim.
     * @return An [IWrappedDomain] representing the members of the claim.
     */
    @JvmStatic
    fun getMembers(claim: IWrappedRegion): IWrappedDomain {
        return claim.members
    }

    /**
     * Adds all members of the given [guild] as members of the given [claim].
     *
     * @param claim The [IWrappedRegion] representing the claim.
     * @param guild The [Guild] object representing the guild.
     */
    @JvmStatic
    fun addMembers(claim: IWrappedRegion, guild: Guild) {
        guild.members.forEach {
            getMembers(claim).addPlayer(it.uuid)
        }
    }

    /**
     * Adds a [player] to the members of the specified [claim].
     *
     * @param claim The [IWrappedRegion] representing the claim.
     * @param player The [Player] to be added as a member
     */
    @JvmStatic
    fun addMember(claim: IWrappedRegion, player: Player) {
        getMembers(claim).addPlayer(player.uniqueId)
    }

    /**
     * Removes a [player] from the members of the specified claim.
     *
     * @param claim The [IWrappedRegion] representing the claim
     * @param player The [Player] to be removed from the members
     */
    @JvmStatic
    fun removeMember(claim: IWrappedRegion, player: OfflinePlayer) {
        getMembers(claim).removePlayer(player.uniqueId)
    }

    /**
     * Kicks a [player] from the specified [guild] claim.
     *
     * @param playerKicked the [Player] to be kicked
     * @param playerExecuting the [Player] executing the kick command
     * @param guild the [Guild] whose claim the player is being kicked from
     * @param settingsManager the [SettingsManager] to check if the feature is enabled
     */
    @JvmStatic
    fun kickMember(
        playerKicked: OfflinePlayer,
        playerExecuting: Player,
        guild: Guild,
        settingsManager: SettingsManager
    ) {
        if (isEnabled(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()
            getGuildClaim(wrapper, playerExecuting, guild).ifPresent { r -> removeMember(r, playerKicked) }
        }
    }

    /**
     * Sets the greeting message for the specified claim.
     *
     * @param wrapper the instance of the WorldGuardWrapper
     * @param claim the wrapped region representing the claim
     * @param settingsManager the settings manager to retrieve the greeting message
     * @param guild the guild whose claim the greeting message is being set for
     */
    @JvmStatic
    fun setEnterMessage(
        wrapper: WorldGuardWrapper,
        claim: IWrappedRegion,
        settingsManager: SettingsManager,
        guild: Guild
    ) {
        claim.setFlag(
            wrapper.getFlag("greeting", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }

    /**
     * Sets the farewell message for the specified claim.
     *
     * @param wrapper the instance of the WorldGuardWrapper
     * @param claim the wrapped region representing the claim
     * @param settingsManager the settings manager to retrieve the farewell message
     * @param guild the guild whose claim the farewell message is being set for
     */
    @JvmStatic
    fun setExitMessage(
        wrapper: WorldGuardWrapper,
        claim: IWrappedRegion,
        settingsManager: SettingsManager,
        guild: Guild
    ) {
        claim.setFlag(
            wrapper.getFlag("farewell", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }

    /**
     * Check if the PvP is disabled for the [player].
     *
     * @param player The player to check the PvP status for.
     * @return Returns `true` if PvP is disabled, `false` otherwise.
     */
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

    /**
     * Deletes the claim for the given [guild].
     *
     * @param guild The [Guild] to delete the claim for.
     * @param settingsManager The [SettingsManager] to check if the feature is enabled.
     */
    @JvmStatic
    fun deleteWithGuild(guild: Guild, settingsManager: SettingsManager) {
        if (!isEnabled(settingsManager)) {
            return
        }
        val wrapper = WorldGuardWrapper.getInstance()
        if (!checkAlreadyExist(wrapper, guild)) {
            return
        }
        removeClaim(wrapper, guild)
    }
}
