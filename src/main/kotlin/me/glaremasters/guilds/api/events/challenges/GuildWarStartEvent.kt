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
package me.glaremasters.guilds.api.events.challenges

import me.glaremasters.guilds.guild.Guild
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * An event that is called when a guild war starts.
 *
 * @property challenger the guild who initiated the war
 * @property defender the guild who was challenged
 *
 * @constructor Creates a new GuildWarStartEvent with the given challenger and defender.
 */
class GuildWarStartEvent(val challenger: Guild, val defender: Guild) : Event() {

    /**
     * Gets a list of all registered handlers for this event.
     *
     * @return the handlers list
     */
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        /**
         * The static list of event handlers for this event type.
         */
        @JvmStatic
        val handlerList = HandlerList()
    }
}
