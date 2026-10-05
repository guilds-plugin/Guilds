package me.glaremasters.guilds.listeners

import ch.jalu.configme.SettingsManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.configuration.sections.TicketSettings
import me.glaremasters.guilds.configuration.sections.TierSettings
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class TicketListener(private val guilds: Guilds, private val guildHandler: GuildHandler, private val settingsManager: SettingsManager) : Listener {

    @EventHandler
    fun PlayerInteractEvent.onUpgrade() {
        val interactItem = item ?: return
        val interactPlayer = player ?: return

        if (!settingsManager.getProperty(TicketSettings.TICKET_ENABLED)) {
            return
        }

        val guild = guildHandler.getGuild(interactPlayer) ?: return

        if (!interactItem.isSimilar(guildHandler.matchTicket(settingsManager))) {
            return
        }

        // Resolve the target tier before the ticket is taken. The ticket used to be consumed first, so a
        // guild that could not be upgraded lost the ticket and the upgrade at the same time.
        if (guildHandler.getNextGuildTier(guild) == null) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__TIER_MAX)
            return
        }

        if (interactItem.amount > 1) interactItem.amount = interactItem.amount - 1 else player.inventory.setItemInHand(ItemStack(Material.AIR))
        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__SUCCESS)

        val previousTier = guild.tier
        guildHandler.upgradeTier(guild)
        guildHandler.applyTierPerms(guilds.permissions, guild, previousTier)
    }
}
