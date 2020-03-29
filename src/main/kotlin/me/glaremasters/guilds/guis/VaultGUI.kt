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
import co.aikar.commands.PaperCommandManager
import com.github.stefvanschie.inventoryframework.Gui
import com.github.stefvanschie.inventoryframework.GuiItem
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.Pane
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.VaultPickerSettings
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.function.Consumer

class VaultGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {

    fun getVaultGUI(guild: Guild, player: Player, commandManager: PaperCommandManager): Gui {
        val name = settingsManager.getProperty(VaultPickerSettings.GUI_NAME).replace("{name}", guild.name)
        val rows = settingsManager.getProperty(VaultPickerSettings.GUI_SIZE)
        val gui = GuiBuilder(guilds).setName(name).setRows(rows).addBackground(9, rows).blockGlobalClick().build()

        // Prevent players from being able to items into the GUIs
        gui.setOnOutsideClick { event: InventoryClickEvent ->
            event.isCancelled = true
            guilds.guiHandler.infoGUI.getInfoGUI(guild, player).show(event.whoClicked)
        }

        // Create the pane for the main items
        val foregroundPane = OutlinePane(0, 0, 9, settingsManager.getProperty(VaultPickerSettings.GUI_SIZE), Pane.Priority.NORMAL)

        // Add the items to the foreground pane
        createForegroundItems(foregroundPane, guild, player)

        // Set it back to 0
        num = 0

        // Add the foreground pane to the GUI
        gui.addPane(foregroundPane)

        // Return the create GUI object
        return gui
    }

    /**
     * Create the regular items that will be on the GUI
     *
     * @param pane  the pane to be added to
     * @param guild the guild of the player
     */
    private fun createForegroundItems(pane: OutlinePane, guild: Guild, player: Player) {
        val max = guildHandler.getGuildTier(guild.tier.level).vaultAmount
        for (i in 0 until max) {
            val status = if (guildHandler.hasVaultUnlocked(i + 1, guild)) settingsManager.getProperty(VaultPickerSettings.PICKER_UNLOCKED) else settingsManager.getProperty(VaultPickerSettings.PICKER_LOCKED)

            val lore = settingsManager.getProperty(VaultPickerSettings.PICKER_LORE)
            val updated: MutableList<String> = ArrayList()

            lore.forEach(Consumer { line: String ->
                updated.add(StringUtils.color(line
                        .replace("{number}", (num + 1).toString())
                        .replace("{status}", status)
                ))
            })

            pane.addItem(GuiItem(GuiUtils.createItem(
                    settingsManager.getProperty(VaultPickerSettings.PICKER_MATERIAL),
                    settingsManager.getProperty(VaultPickerSettings.PICKER_NAME),
                    updated
            ), Consumer { event: InventoryClickEvent ->
                event.isCancelled = true
                try {
                    guildHandler.getGuildVault(guild, event.rawSlot + 1)
                } catch (ex: IndexOutOfBoundsException) {
                    guildHandler.cachedVaults[guild]?.add(guildHandler.createNewVault(settingsManager))
                }
                player.openInventory(guildHandler.getGuildVault(guild, event.rawSlot + 1))
                guildHandler.openedVault.add(player)
            }))
            num++
        }
    }

    companion object {
        private var num = 0
    }

}
