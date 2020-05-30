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

package me.glaremasters.guilds.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.EconomyUtils
import org.bukkit.entity.Player

class PlaceholderAPI(private val guildHandler: GuildHandler) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "guilds"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun getAuthor(): String {
        return "blockslayer22"
    }

    override fun getVersion(): String {
        return "2.1"
    }

    override fun onPlaceholderRequest(player: Player?, arg: String): String {
        if (player == null) {
            return ""
        }
        val api = Guilds.getApi() ?: return ""

        // Check formatted here because this needs to return before we check the guild
        if (arg.toLowerCase() == "formatted") {
            return guildHandler.getFormattedPlaceholder(player)
        }

        val guild = api.getGuild(player) ?: return ""
        return when (arg.toLowerCase()) {
            "id" -> guild.id.toString()
            "name" -> guild.name
            "master" -> guild.guildMaster.asOfflinePlayer.name.toString()
            "member_count" -> guild.members.size.toString()
            "prefix" -> guild.prefix
            "members_online" -> guild.onlineMembers.size.toString()
            "status" -> guild.status.name
            "role" -> guild.getMember(player.uniqueId).role.name
            "tier" -> guild.tier.level.toString()
            "tier_name" -> guild.tier.name
            "balance" -> EconomyUtils.format(guild.balance)
            "code_amount" -> guild.codes.size.toString()
            "max_members" -> guild.tier.maxMembers.toString()
            "max_balance" -> EconomyUtils.format(guild.tier.maxBankBalance)
            "challenge_wins" -> guild.guildScore.wins.toString()
            "challenge_loses" -> guild.guildScore.loses.toString()
            "motd" -> guild.motd ?: ""
            else -> ""
        }
    }
}
