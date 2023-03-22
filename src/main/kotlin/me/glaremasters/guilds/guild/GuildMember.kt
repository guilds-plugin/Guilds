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
package me.glaremasters.guilds.guild

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * A class representing a member of a guild.
 *
 * @property uuid The unique identifier of the member as a UUID object.
 * @property role The role of the member as a GuildRole object.
 * @property joinDate A Long representing the timestamp of the date the member joined the guild.
 * @property lastLogin A Long representing the timestamp of the last time the member logged in.
 *
 * @constructor Creates an instance of GuildMember.
 */
class GuildMember(
    val uuid: UUID,
    var role: GuildRole
) {
    var joinDate: Long = 0
    var lastLogin: Long = 0

    /**
     * Gets a boolean indicating whether the member is currently online or not.
     *
     * @return A boolean indicating whether the member is online or not.
     */
    val isOnline: Boolean
        get() = asOfflinePlayer.isOnline

    /**
     * Gets the member's Bukkit OfflinePlayer object.
     *
     * @return An OfflinePlayer object representing the member.
     */
    val asOfflinePlayer: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(uuid)

    /**
     * Gets the member's Bukkit Player object if they are currently online, otherwise returns null.
     *
     * @return A Player object representing the member if they are online, otherwise null.
     */
    val asPlayer: Player?
        get() = Bukkit.getPlayer(uuid)

    /**
     * Gets the name of the member if available.
     *
     * @return A string representing the member's name if available, otherwise null.
     */
    val name: String?
        get() = asOfflinePlayer.name
}
