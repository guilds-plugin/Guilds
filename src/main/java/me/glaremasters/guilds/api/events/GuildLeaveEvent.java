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

package me.glaremasters.guilds.api.events;

import lombok.Getter;
import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

@Getter
public class GuildLeaveEvent extends GuildEvent {

    private final Cause cause;


    /**
     * Called a when a player leaves the guild
     * @param player the player leaving the guild
     * @param guild the guild the player was leaving
     * @param cause the reason the event was called
     */
    public GuildLeaveEvent(Player player, Guild guild, Cause cause) {
        super(player, guild);
        this.cause = cause;
    }

    public enum Cause {
        PLAYER_LEFT,
        PLAYER_KICKED,
        ADMIN_REMOVED
    }
}
