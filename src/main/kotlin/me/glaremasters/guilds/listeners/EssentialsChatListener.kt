package me.glaremasters.guilds.listeners

import me.glaremasters.guilds.guild.GuildHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class EssentialsChatListener(private val guildHandler: GuildHandler) : Listener {

    @EventHandler
    fun AsyncPlayerChatEvent.onChat() {
        val guild = guildHandler.getGuild(player)
        var message = format

        if (guild == null) {
            val regex = "(\\{GUILD(?:.*?)})"
            val formatted = "(\\{GUILD_FORMATTED})"

            // Resolve the placeholder first, otherwise the strip below would eat it
            message = message.replace(formatted.toRegex(), guildHandler.getFormattedPlaceholder(player))
            message = message.replace(regex.toRegex(), "")
            format = message
            return
        }

        message = message
                .replace("{GUILD}", guild.name)
                .replace("{GUILD_PREFIX}", guild.prefix)
                .replace("{GUILD_MASTER}", guild.guildMaster.name.toString())
                .replace("{GUILD_STATUS}", guild.status.name)
                .replace("{GUILD_MEMBER_COUNT}", guild.size.toString())
                .replace("{GUILD_MEMBERS_ONLINE}", guild.onlineMembers.size.toString())
                .replace("{GUILD_ROLE}", guild.getMember(player.uniqueId).role.name)
                .replace("{GUILD_FORMATTED}", guildHandler.getFormattedPlaceholder(player))
                .replace("{GUILD_CHALLENGE_WINS}", guild.guildScore.wins.toString())
                .replace("{GUILD_CHALLENGE_LOSES}", guild.guildScore.loses.toString())
                .replace("{GUILD_TIER_NAME}", guild.tier.name)

        format = message
    }
}
