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
package me.glaremasters.guilds.api

import java.util.UUID
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildRole
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class GuildsAPI(val guildHandler: GuildHandler, val cooldownHandler: CooldownHandler) {

    /**
     *
     * Get the guild of a player
     * @param player the players you're getting the guild of
     * @return the guild that the player is in
     */
    fun getGuild(player: OfflinePlayer): Guild? {
        return guildHandler.getGuild(player)
    }

    /**
     * Get a guild by it's uuid
     * @param uuid uuid of the guild
     * @return the guild the uuid belong to
     */
    fun getGuild(uuid: UUID): Guild? {
        return guildHandler.getGuild(uuid)
    }

    /**
     * Get a guild by it's name
     * @param name the name of the guild
     * @return the guild object
     */
    fun getGuild(name: String): Guild? {
        return guildHandler.getGuild(name)
    }

    /**
     * Get a copy of one of a guild's vaults
     * @param guild the guild to get the vault of
     * @param vaultNumber which vault to get
     * @return guild vault
     */
    fun getGuildVault(guild: Guild, vaultNumber: Int): Inventory {
        return guildHandler.getGuildVault(guild, vaultNumber)
    }

    /**
     * Get the role of a player
     * @param player role
     * @return the role of a player
     */
    fun getGuildRole(player: Player): GuildRole? {
        return getGuild(player)?.getMember(player.uniqueId)?.role
    }
}
