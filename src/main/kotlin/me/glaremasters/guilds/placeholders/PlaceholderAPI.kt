package me.glaremasters.guilds.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exte.rounded
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guis.guildMasterName
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
        // Locale.ROOT: on a Turkish-locale JVM the default locale lowercases "I" to a dotless "ı"
        if (arg.lowercase(Locale.ROOT) == "formatted") {
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

        // "member_count" also starts with "member_", so this branch has to check the suffix is
        // numeric before it claims the arg. Without that, the "member_count" case in the when below
        // is unreachable and %guilds_member_count% resolves to an empty string.
        if (arg.startsWith("member_") && arg.removePrefix("member_").all(Char::isDigit)) {
            val position = arg.removePrefix("member_").toIntOrNull() ?: return ""
            val member = guild.members.toList().getOrNull(position - 1) ?: return ""
            return member.name ?: ""
        }

        // Locale.ROOT: on a Turkish-locale JVM the default locale lowercases "I" to a dotless "ı", so
        // "ID" would miss the branch below and fall through to ""
        return when (arg.lowercase(Locale.ROOT)) {
            "id" -> guild.id.toString()
            "name" -> guild.name
            "master" -> guild.guildMasterName()
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
