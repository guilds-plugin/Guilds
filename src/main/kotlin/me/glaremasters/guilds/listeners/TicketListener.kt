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

        if (guildHandler.isMaxTier(guild)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__TIER_MAX)
            return
        }

        if (interactItem.amount > 1) interactItem.amount = interactItem.amount - 1 else player.inventory.setItemInHand(ItemStack(Material.AIR))
        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__SUCCESS)

        guildHandler.removeGuildPermsFromAll(guilds.permissions, guild)
        guildHandler.upgradeTier(guild)
        guildHandler.addGuildPermsToAll(guilds.permissions, guild)
    }
}
