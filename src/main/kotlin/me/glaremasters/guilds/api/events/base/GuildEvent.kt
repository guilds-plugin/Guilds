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
package me.glaremasters.guilds.api.events.base

import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * A base class for guild related events that occur in the game.
 *
 * @property player the player who triggered the event
 * @property guild the guild that is involved in the event
 *
 * @constructor Creates a new GuildEvent with the given player and guild.
 */
open class GuildEvent(player: Player, val guild: Guild) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    /**
     * Returns the list of handlers, through which this event is passed.
     *
     * @return the list of handlers
     */
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    /**
     * Returns whether this event is cancelled or not.
     *
     * @return true if the event is cancelled, false otherwise
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Sets whether this event is cancelled or not.
     *
     * @param cancelled the value to set the event cancelled status to
     */
    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
