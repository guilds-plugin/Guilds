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
package me.glaremasters.guilds.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exte.rounded
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.EconomyUtils
import org.bukkit.entity.Player
import java.util.*

class PlaceholderAPI(private val guildHandler: GuildHandler) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "guilds"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun getAuthor(): String {
        return "Glare"
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
        if (arg.lowercase(Locale.getDefault()) == "formatted") {
            return guildHandler.getFormattedPlaceholder(player)
        }

        // %guilds_top_wins_name_#1%
        if (arg.startsWith("top_wins_name_")) {
            val updated = try {
                arg.replace("top_wins_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.values.sortedBy { it.guildScore.wins }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_wins_amount_#%
        if (arg.startsWith("top_wins_amount_")) {
            val updated = try {
                arg.replace("top_wins_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.values.sortedBy { it.guildScore.wins }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.guildScore.wins.toString()
        }

        // %guilds_top_losses_name_1%
        if (arg.startsWith("top_losses_name_")) {
            val updated = try {
                arg.replace("top_losses_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.values.sortedBy { it.guildScore.loses }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_losses_amount_#%
        if (arg.startsWith("top_losses_amount_")) {
            val updated = try {
                arg.replace("top_losses_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.values.sortedBy { it.guildScore.loses }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.guildScore.loses.toString()
        }

        // %guilds_top_wlr_name_#%
        if (arg.startsWith("top_wlr_name_")) {
            val updated = try {
                arg.replace("top_wlr_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val filterValid = api.guildHandler.guilds.values.filter { it.guildScore.wins > 0 && it.guildScore.loses > 0 }

            val guild = try {
                filterValid.sortedBy { (it.guildScore.wins.toDouble() / it.guildScore.loses.toDouble()) }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_wlr_amount_#%
        if (arg.startsWith("top_wlr_amount_")) {
            val updated = try {
                arg.replace("top_wlr_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val filterValid = api.guildHandler.guilds.values.filter { it.guildScore.wins > 0 && it.guildScore.loses > 0 }

            val guild = try {
                filterValid.sortedBy  { (it.guildScore.wins.toDouble() / it.guildScore.loses.toDouble()) }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return (guild.guildScore.wins.toDouble() / guild.guildScore.loses.toDouble()).rounded().toString()
        }

        val guild = api.getGuild(player) ?: return ""

        if (arg.startsWith("member_")) {
            val updated = try {
                arg.replace("member_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val member = guild.members.toList().getOrNull(updated - 1) ?: return ""
            return member.name ?: ""
        }

        return when (arg.lowercase(Locale.getDefault())) {
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
            "balance_raw" -> guild.balance.toString()
            "code_amount" -> guild.codes.size.toString()
            "max_members" -> guild.tier.maxMembers.toString()
            "max_balance" -> EconomyUtils.format(guild.tier.maxBankBalance)
            "challenge_wins" -> guild.guildScore.wins.toString()
            "challenge_loses" -> guild.guildScore.loses.toString()
            "motd" -> guild.motd ?: ""
            "spying" -> guildHandler.isSpy(player).toString()
            else -> ""
        }
    }
}
