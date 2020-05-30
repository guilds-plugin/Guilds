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

package me.glaremasters.guilds.guis

import ch.jalu.configme.SettingsManager
import co.aikar.commands.ACFBukkitUtil
import co.aikar.commands.PaperCommandManager
import java.util.concurrent.TimeUnit
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.CooldownSettings
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildRolePerm
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.entity.Player

class InfoGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler, private val cooldownHandler: CooldownHandler, private val manager: PaperCommandManager) {

    fun get(guild: Guild, player: Player): Gui {
        val name = settingsManager.getProperty(GuildInfoSettings.GUI_NAME).replace("{name}", guild.name).replace("{prefix}", guild.prefix)
        val gui = GuiBuilder(guilds).setName(name).setRows(3).disableGlobalClicking().build()

        addItems(gui, guild, player)
        addBackground(gui)
        return gui
    }

    private fun addItems(gui: Gui, guild: Guild, player: Player) {
        val tier = guildHandler.getGuildTier(guild.tier.level)

        val statusMaterial = if (guild.isPrivate) settingsManager.getProperty(GuildInfoSettings.STATUS_MATERIAL_PRIVATE) else settingsManager.getProperty(GuildInfoSettings.STATUS_MATERIAL_PUBLIC)
        val statusString = if (guild.isPrivate) settingsManager.getProperty(GuildInfoSettings.STATUS_PRIVATE) else settingsManager.getProperty(GuildInfoSettings.STATUS_PUBLIC)
        val home = if (guild.home == null) settingsManager.getProperty(GuildInfoSettings.HOME_EMPTY) else ACFBukkitUtil.blockLocationToString(guild.home.asLocation)
        val motd = if (guild.motd == null) "" else guild.motd

        generateItem(gui, settingsManager.getProperty(GuildInfoSettings.TIER_DISPLAY), settingsManager.getProperty(GuildInfoSettings.TIER_MATERIAL), settingsManager.getProperty(GuildInfoSettings.TIER_NAME), settingsManager.getProperty(GuildInfoSettings.TIER_LORE).map { l -> l.replace("{tier}", tier.name) }, 2, 3)
        generateItem(gui, settingsManager.getProperty(GuildInfoSettings.BANK_DISPLAY), settingsManager.getProperty(GuildInfoSettings.BANK_MATERIAL), settingsManager.getProperty(GuildInfoSettings.BANK_NAME), settingsManager.getProperty(GuildInfoSettings.BANK_LORE).map { l -> l.replace("{current}", EconomyUtils.format(guild.balance)).replace("{max}", EconomyUtils.format(tier.maxBankBalance)) }, 2, 4)
        generateMembersItem(gui, guild)
        generateItem(gui, settingsManager.getProperty(GuildInfoSettings.STATUS_DISPLAY), statusMaterial, settingsManager.getProperty(GuildInfoSettings.STATUS_NAME), settingsManager.getProperty(GuildInfoSettings.STATUS_LORE).map { l -> l.replace("{status}", statusString) }, 2, 6)
        generateHomeItem(gui, guild, player, home)
        generateVaultItem(gui, guild, player)
        generateItem(gui, settingsManager.getProperty(GuildInfoSettings.MOTD_DISPLAY), settingsManager.getProperty(GuildInfoSettings.MOTD_MATERIAL), settingsManager.getProperty(GuildInfoSettings.MOTD_NAME), settingsManager.getProperty(GuildInfoSettings.MOTD_LORE).map { l -> l.replace("{motd}", motd) }, 1, 5)
    }

    private fun generateItem(gui: Gui, add: Boolean, material: String, name: String, lore: List<String>, x: Int, y: Int) {
        if (!add) {
            return
        }
        val item = GuiItem(GuiUtils.createItem(material, name, lore))
        item.setAction { event ->
            event.isCancelled = true
        }
        gui.setItem(x, y, item)
    }

    private fun generateMembersItem(gui: Gui, guild: Guild) {
        if (!settingsManager.getProperty(GuildInfoSettings.MEMBERS_DISPLAY)) {
            return
        }
        val tier = guildHandler.getGuildTier(guild.tier.level)
        val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.MEMBERS_MATERIAL), settingsManager.getProperty(GuildInfoSettings.MEMBERS_NAME), settingsManager.getProperty(GuildInfoSettings.MEMBERS_LORE).map { l -> l.replace("{current}", guild.members.size.toString()).replace("{max}", tier.maxMembers.toString()).replace("{online}", guild.onlineMembers.size.toString()) }))
        item.setAction { event ->
            event.isCancelled = true
            guilds.guiHandler.members.get(guild).open(event.whoClicked)
        }
        gui.setItem(2, 5, item)
    }

    private fun generateHomeItem(gui: Gui, guild: Guild, player: Player, home: String) {
        val cooldownName = Cooldown.Type.Home.name
        if (!settingsManager.getProperty(GuildInfoSettings.HOME_DISPLAY)) {
            return
        }
        val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.HOME_MATERIAL), settingsManager.getProperty(GuildInfoSettings.HOME_NAME), settingsManager.getProperty(GuildInfoSettings.HOME_LORE).map { l -> l.replace("{coords}", home) }))
        item.setAction { event ->
            event.isCancelled = true
            if (cooldownHandler.hasCooldown(cooldownName, player.uniqueId)) {
                manager.getCommandIssuer(player).sendInfo(Messages.HOME__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldownName, player.uniqueId).toString())
                return@setAction
            }
            if (!settingsManager.getProperty(GuildInfoSettings.HOME_TELEPORT)) {
                return@setAction
            }
            if (guild.home == null) {
                manager.getCommandIssuer(player).sendInfo(Messages.HOME__NO_HOME_SET)
                return@setAction
            }
            val teleportingTo = guild.home.asLocation
            if (settingsManager.getProperty(CooldownSettings.WU_HOME_ENABLED)) {
                val initial = player.location
                val delay = settingsManager.getProperty(CooldownSettings.WU_HOME)
                manager.getCommandIssuer(player).sendInfo(Messages.HOME__WARMUP, "{amount}", delay.toString())
                Guilds.newChain<Any>().delay(delay, TimeUnit.SECONDS).sync {
                    val curr = player.location
                    if (initial.distance(curr) > 1) {
                        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__CANCELLED)
                    } else {
                        player.teleport(teleportingTo)
                        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED)
                    }
                }.execute()
            } else {
                player.teleport(teleportingTo)
                manager.getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED)
            }
            cooldownHandler.addCooldown(player, cooldownName, settingsManager.getProperty(CooldownSettings.HOME), TimeUnit.SECONDS)
        }
        gui.setItem(2, 7, item)
    }

    private fun generateVaultItem(gui: Gui, guild: Guild, player: Player) {
        if (!settingsManager.getProperty(GuildInfoSettings.VAULT_DISPLAY)) {
            return
        }
        val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoSettings.VAULT_MATERIAL), settingsManager.getProperty(GuildInfoSettings.VAULT_NAME), settingsManager.getProperty(GuildInfoSettings.VAULT_LORE)))
        item.setAction { event ->
            event.isCancelled = true
            if (!guild.memberHasPermission(player, GuildRolePerm.OPEN_VAULT)) {
                return@setAction
            }
            guilds.guiHandler.vaults.get(guild, player).open(event.whoClicked)
        }
        gui.setItem(3, 5, item)
    }
}
