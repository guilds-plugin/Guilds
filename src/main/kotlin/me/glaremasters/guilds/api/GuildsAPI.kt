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
package me.glaremasters.guilds.api

import java.util.UUID
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildMember
import me.glaremasters.guilds.guild.GuildRole
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * The `GuildsAPI` class provides functions for accessing information about guilds.
 *
 * @param guildHandler: an instance of the `GuildHandler` class
 * @param cooldownHandler: an instance of the `CooldownHandler` class
 */
class GuildsAPI(val guildHandler: GuildHandler, val cooldownHandler: CooldownHandler) {

    /**
     * Get the guild of a player.
     *
     * @param player: the player whose guild you want to retrieve
     * @return the guild that the player is in, or `null` if the player is not in a guild
     */
    fun getGuild(player: OfflinePlayer): Guild? {
        return guildHandler.getGuild(player)
    }

    /**
     * Get a guild by its UUID.
     *
     * @param uuid: the UUID of the guild
     * @return the guild with the given UUID, or `null` if no such guild exists
     */
    fun getGuild(uuid: UUID): Guild? {
        return guildHandler.getGuild(uuid)
    }

    /**
     * Get a guild by its name.
     *
     * @param name: the name of the guild
     * @return the guild with the given name, or `null` if no such guild exists
     */
    fun getGuild(name: String): Guild? {
        return guildHandler.getGuild(name)
    }

    /**
     * Get a guild by the UUID of a player.
     *
     * @param uuid: the UUID of the player
     * @return the guild of the player with the given UUID, or `null` if the player is not in a guild
     */
    fun getGuildByPlayerId(uuid: UUID): Guild? {
        return guildHandler.getGuildByPlayerId(uuid)
    }

    /**
     * Get a guild member by their UUID.
     *
     * @param uuid: the UUID of the player
     * @return the `GuildMember` instance of the player with the given UUID, or `null` if the player is not in a guild
     */
    fun getGuildMember(uuid: UUID): GuildMember? {
        return guildHandler.getGuildMember(uuid)
    }

    /**
     * Get a copy of one of a guild's vaults.
     *
     * @param guild: the guild to retrieve the vault from
     * @param vaultNumber: the number of the vault to retrieve
     * @return a copy of the specified guild vault
     */
    fun getGuildVault(guild: Guild, vaultNumber: Int): Inventory {
        return guildHandler.getGuildVault(guild, vaultNumber)
    }

    /**
     * Get the role of a player.
     *
     * @param player: the player whose role you want to retrieve
     * @return the `GuildRole` of the player, or `null` if the player is not in a guild
     */
    fun getGuildRole(player: Player): GuildRole? {
        return getGuild(player)?.getMember(player.uniqueId)?.role
    }
}
