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
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.region.IWrappedRegion
import java.util.*
import kotlin.properties.Delegates

class GuildClaim (
    @field:Transient var name: String,
    @field:Transient var number: Int,
    @field:Transient var guildId: UUID) {

    fun changeName(name: String) {
        this.name = name
        return
    }

    fun changeNumber(num: Int) {
        this.number = num
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

    override fun toString(): String {
        return "GuildClaim(name=$name, number=$number, guildId=$guildId)"
    }

    class GuildClaimBuilder internal constructor() {
        private lateinit var name: String
        private var number by Delegates.notNull<Int>()
        private lateinit var guildId: UUID

        fun name(name: String): GuildClaimBuilder {
            this.name = name
            return this
        }

        fun number(number: Int): GuildClaimBuilder {
            this.number = number
            return this
        }

        fun guildId(guildId: UUID): GuildClaimBuilder {
            this.guildId = guildId
            return this
        }

        fun build(): GuildClaim {
            return GuildClaim(
                name,
                number,
                guildId
            )
        }

        override fun toString(): String {
            return "GuildClaim.GuildClaimBuilder(name=$name, number=$number, guildId=$guildId)"
        }
    }

    companion object {
        fun builder(): GuildClaimBuilder {
            return GuildClaimBuilder()
        }
    }
}