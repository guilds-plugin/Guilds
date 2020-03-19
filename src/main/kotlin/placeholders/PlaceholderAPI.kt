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

package placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import org.bukkit.entity.Player
import java.text.DecimalFormat

class PlaceholderAPI(private val guildHandler: GuildHandler) : PlaceholderExpansion() {
    private val df = DecimalFormat("###.##")

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

    override fun onPlaceholderRequest(p: Player, arg: String): String {
        val api = Guilds.getApi() ?: return ""
        return when (arg.toLowerCase()) {
            "id" -> api.getGuild(p).id.toString()
            "name" -> api.getGuild(p).name
            "master" -> api.getGuild(p).guildMaster.asOfflinePlayer.name.toString()
            "member_count" -> api.getGuild(p).members.size.toString()
            "prefix" -> api.getGuild(p).prefix
            "members_online" -> api.getGuild(p).onlineMembers.size.toString()
            "status" -> api.getGuild(p).status.name
            "role" -> api.getGuildRole(p).name
            "tier" -> api.getGuild(p).tier.level.toString()
            "tier_name" -> api.getGuild(p).tier.name
            "balance" -> df.format(api.getGuild(p).balance)
            "code_amount" -> api.getGuild(p).codes.size.toString()
            "max_members" -> api.getGuild(p).tier.maxMembers.toString()
            "max_balance" -> api.getGuild(p).tier.maxBankBalance.toString()
            "formatted" -> guildHandler.getFormattedPlaceholder(p)
            "challenge_wins" -> java.lang.String.valueOf(api.getGuild(p).guildScore.wins)
            "challenge_loses" -> java.lang.String.valueOf(api.getGuild(p).guildScore.loses)
            "motd" -> if (api.getGuild(p).motd == null) "" else api.getGuild(p).motd
            else -> ""
        }
    }

}
