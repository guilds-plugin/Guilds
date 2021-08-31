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
import org.codemc.worldguardwrapper.region.IWrappedRegion
import java.util.*

class GuildClaim (
    @field:Transient var name: String?,
    @field:Transient var num: Int,
    @field:Transient var guildID: UUID,
    @field:Transient var region: Optional<IWrappedRegion>) {

    fun changeName(name: String) {
        this.name = name
        return
    }

    fun changeNumber(num: Int) {
        this.num = num
        return
    }

    fun changeRegion(region: Optional<IWrappedRegion>) {
        this.region = region
        return
    }

    fun changeGuildID(guildID: UUID) {
        this.guildID = guildID
        return
    }

    override fun toString(): String {
        return "GuildClaim(name=" + name + ", num=" + num + ", region=" + region?.get()?.id + ", guildID=" + guildID + ")"
    }

    class GuildClaimBuilder internal constructor() {
        private var name: String? = null
        private var num = 0
        private lateinit var guildID: UUID
        private lateinit var region: Optional<IWrappedRegion>

        fun name(name: String?): GuildClaimBuilder {
            this.name = name
            return this
        }

        fun num(num: Int): GuildClaimBuilder {
            this.num = num
            return this
        }

        fun region(region: Optional<IWrappedRegion>): GuildClaimBuilder {
            this.region = region
            return this
        }

        fun guildID(guildID: UUID): GuildClaimBuilder {
            this.guildID = guildID
            return this
        }

        fun build(): GuildClaim {
            return GuildClaim(
                name,
                num,
                guildID,
                region
            )
        }

        override fun toString(): String {
            return "GuildClaim.GuildClaimBuilder(name=" + name + ", num=" + num + ", region=" + region.get().id + ", guildID=" + guildID + ")"
        }
    }

    companion object {
        fun builder(): GuildClaimBuilder {
            return GuildClaimBuilder()
        }
    }
}