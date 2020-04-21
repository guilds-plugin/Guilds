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
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.VaultPickerSettings
import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.entity.Player

class VaultGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {

    fun get(guild: Guild, player: Player): Gui {
        val name = settingsManager.getProperty(VaultPickerSettings.GUI_NAME).replace("{name}", guild.name)
        val rows = settingsManager.getProperty(VaultPickerSettings.GUI_SIZE)
        val gui = GuiBuilder(guilds).setName(name).setRows(rows).disableGlobalClicking().build()

        // Prevent players from being able to items into the GUIs
        gui.setOutsideClickAction { event ->
            event.isCancelled = true
            guilds.guiHandler.info.get(guild, player).open(event.whoClicked)
        }

        addItems(gui, guild, player)
        addBackground(gui)

        num = 0
        return gui
    }

    /**
     * Create the regular items that will be on the GUI
     *
     * @param pane  the pane to be added to
     * @param guild the guild of the player
     */
    private fun addItems(gui: Gui, guild: Guild, player: Player) {
        val max = guildHandler.getGuildTier(guild.tier.level).vaultAmount
        for (i in 0 until max) {
            val status = if (guildHandler.hasVaultUnlocked(i + 1, guild)) settingsManager.getProperty(VaultPickerSettings.PICKER_UNLOCKED) else settingsManager.getProperty(VaultPickerSettings.PICKER_LOCKED)

            val lore = settingsManager.getProperty(VaultPickerSettings.PICKER_LORE)
            val updated = mutableListOf<String>()

            lore.forEach { line ->
                updated.add(StringUtils.color(line
                        .replace("{number}", (num + 1).toString())
                        .replace("{status}", status)
                ))
            }

            val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(VaultPickerSettings.PICKER_MATERIAL), settingsManager.getProperty(VaultPickerSettings.PICKER_NAME), updated))

            item.setAction { event ->
                event.isCancelled = true
                try {
                    guildHandler.getGuildVault(guild, event.rawSlot + 1)
                } catch (ex: IndexOutOfBoundsException) {
                    guildHandler.vaults[guild]?.add(guildHandler.createNewVault(settingsManager))
                }
                player.openInventory(guildHandler.getGuildVault(guild, event.rawSlot + 1))
                guildHandler.opened.add(player)
            }

            gui.addItem(item)
            num++
        }
    }

    companion object {
        private var num = 0
    }

}
