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
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.WrappedState
import org.codemc.worldguardwrapper.region.IWrappedDomain
import java.util.*

object ClaimPermissions {

    // Owner handlers
    @JvmStatic
    fun addOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, guild: Guild) {
        val region = claim.getRegion(wrapper)
        region.owners.addPlayer(guild.guildMaster.uuid)
    }

    @JvmStatic
    fun addOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, player: Player) {
        val region = claim.getRegion(wrapper)
        region.owners.addPlayer(player.uniqueId)
    }

    @JvmStatic
    fun addOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, uuid: UUID) {
        val region = claim.getRegion(wrapper)
        region.owners.addPlayer(uuid)
    }

    @JvmStatic
    fun removeOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, player: Player) {
        val region = claim.getRegion(wrapper)
        region.owners.removePlayer(player.uniqueId)
    }

    @JvmStatic
    fun removeOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, uuid: UUID) {
        val region = claim.getRegion(wrapper)
        region.owners.removePlayer(uuid)
    }


    @JvmStatic
    fun transferOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, playerGive: Player, playerTake: Player) {
        removeOwner(wrapper, claim, playerTake)
        addOwner(wrapper, claim, playerGive)
    }

    @JvmStatic
    fun transferOwner(wrapper: WorldGuardWrapper, claim: GuildClaim, playerGive: UUID, playerTake: UUID) {
        removeOwner(wrapper, claim, playerTake)
        addOwner(wrapper, claim, playerGive)
    }










    // Member handling
    @JvmStatic
    fun getMembers(wrapper: WorldGuardWrapper, claim: GuildClaim): IWrappedDomain {
        return claim.getRegion(wrapper).members
    }

    @JvmStatic
    fun addMembers(wrapper: WorldGuardWrapper, claim: GuildClaim, guild: Guild) {
        guild.members.forEach {
            getMembers(wrapper, claim).addPlayer(it.uuid)
        }
    }

    @JvmStatic
    fun addMember(wrapper: WorldGuardWrapper, claim: GuildClaim, player: Player) {
        getMembers(wrapper, claim).addPlayer(player.uniqueId)
    }

    @JvmStatic
    fun addMember(wrapper: WorldGuardWrapper, claim: GuildClaim, uuid: UUID) {
        getMembers(wrapper, claim).addPlayer(uuid)
    }

    @JvmStatic
    fun removeMember(wrapper: WorldGuardWrapper, claim: GuildClaim, player: OfflinePlayer) {
        getMembers(wrapper, claim).removePlayer(player.uniqueId)
    }

    @JvmStatic
    fun removeMember(wrapper: WorldGuardWrapper, claim: GuildClaim, uuid: UUID) {
        getMembers(wrapper, claim).removePlayer(uuid)
    }

    @JvmStatic
    fun kickMember(wrapper: WorldGuardWrapper, claim: GuildClaim, playerKicked: OfflinePlayer) {
        removeMember(wrapper, claim, playerKicked)
    }

    @JvmStatic
    fun kickMember(wrapper: WorldGuardWrapper, claim: GuildClaim, uuid: UUID) {
        removeMember(wrapper, claim, uuid)
    }










    // Decoration Handler
    @JvmStatic
    fun setEnterMessage(wrapper: WorldGuardWrapper, claim: GuildClaim, settingsManager: SettingsManager, guild: Guild) {
        val region = claim.getRegion(wrapper)
        region.setFlag(wrapper.getFlag("greeting", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }

    @JvmStatic
    fun setExitMessage(wrapper: WorldGuardWrapper, claim: GuildClaim, settingsManager: SettingsManager, guild: Guild) {
        val region = claim.getRegion(wrapper)
        region.setFlag(wrapper.getFlag("farewell", String::class.java).orElse(null),
            StringUtils.color(
                settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.name)
                    .replace("{prefix}", guild.prefix)
            )
        )
    }










    // Misc
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
}