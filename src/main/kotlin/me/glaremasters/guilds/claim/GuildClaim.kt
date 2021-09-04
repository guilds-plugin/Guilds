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
import me.glaremasters.guilds.guild.GuildHandler
import org.bukkit.Location
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.region.IWrappedRegion
import org.codemc.worldguardwrapper.selection.ICuboidSelection
import java.util.*
import kotlin.properties.Delegates

class GuildClaim (var name: UUID, var guildId: UUID) {

    fun changeName(name: UUID) {
        this.name = name
        return
    }

    fun changeGuildId(guildID: UUID) {
        this.guildId = guildID
        return
    }

    fun getRegion(wrapper: WorldGuardWrapper): IWrappedRegion {
        return ClaimUtils.getRegionFromName(wrapper, name)!!
    }

    fun getGuild(guildHandler: GuildHandler): Guild {
        return guildHandler.getGuild(guildId)
    }

    fun getClaimFirstCorner(): Location {
        val wrapper = WorldGuardWrapper.getInstance()

        val selection = ClaimUtils.getSelection(wrapper, name.toString())

        return selection?.minimumPoint!!
    }

    fun getClaimSecondCorner(): Location {
        val wrapper = WorldGuardWrapper.getInstance()

        val selection = ClaimUtils.getSelection(wrapper, name.toString())

        return selection?.maximumPoint!!
    }

    override fun toString(): String {
        return "GuildClaim(name=$name, guildId=$guildId)"
    }

    class GuildClaimBuilder internal constructor() {
        private lateinit var name: UUID
        private lateinit var guildId: UUID

        fun name(name: UUID): GuildClaimBuilder {
            this.name = name
            return this
        }

        fun guildId(guildId: UUID): GuildClaimBuilder {
            this.guildId = guildId
            return this
        }

        fun build(): GuildClaim {
            return GuildClaim( name, guildId)
        }

        override fun toString(): String {
            return "GuildClaim.GuildClaimBuilder(name=$name, guildId=$guildId)"
        }
    }

    companion object {
        fun builder(): GuildClaimBuilder {
            return GuildClaimBuilder()
        }
    }
}