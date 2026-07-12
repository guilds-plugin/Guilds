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
package me.glaremasters.guilds.guis

import ch.jalu.configme.SettingsManager
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.VaultPickerSettings
import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player

class VaultGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {

    fun get(guild: Guild, player: Player): Gui {
        val name = settingsManager.getProperty(VaultPickerSettings.GUI_NAME).replace("{name}", guild.name)
        val rows = settingsManager.getProperty(VaultPickerSettings.GUI_SIZE)
        val gui = Gui(rows, StringUtils.color(name))

        // Prevent players from being able to items into the GUIs
        gui.setOutsideClickAction { event ->
            event.isCancelled = true
            guilds.guiHandler.info.get(guild, player).open(event.whoClicked)
        }

        addItems(gui, guild, player)
        addBackground(gui)

        return gui
    }

    /**
     * Opens a specific guild vault, creating any missing cached vaults up to that number.
     *
     * @param guild the guild that owns the vault
     * @param player the player opening the vault
     * @param vaultNumber the one-based vault number
     * @return true when the vault was valid and opened, otherwise false
     */
    fun open(guild: Guild, player: Player, vaultNumber: Int): Boolean {
        if (vaultNumber < 1 || !guildHandler.hasVaultUnlocked(vaultNumber, guild)) {
            return false
        }

        val vaults = guildHandler.vaults[guild] ?: return false
        while (vaults.size < vaultNumber) {
            vaults.add(guildHandler.createNewVault(settingsManager))
        }

        player.openInventory(guildHandler.getGuildVault(guild, vaultNumber))
        guildHandler.opened.add(player)
        return true
    }

    /**
     * Create the regular items that will be on the GUI
     *
     * @param gui the GUI to add items to
     * @param guild the guild of the player
     */
    private fun addItems(gui: Gui, guild: Guild, player: Player) {
        val max = guildHandler.getGuildTier(guild.tier.level)?.vaultAmount!!
        for (i in 0 until max) {
            val vaultNumber = i + 1
            val status = if (guildHandler.hasVaultUnlocked(vaultNumber, guild)) settingsManager.getProperty(VaultPickerSettings.PICKER_UNLOCKED) else settingsManager.getProperty(VaultPickerSettings.PICKER_LOCKED)

            val lore = settingsManager.getProperty(VaultPickerSettings.PICKER_LORE)
            val updated = mutableListOf<String>()

            lore.forEach { line ->
                updated.add(StringUtils.color(line
                        .replace("{number}", vaultNumber.toString())
                        .replace("{status}", status)
                ))
            }

            val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(VaultPickerSettings.PICKER_MATERIAL), settingsManager.getProperty(VaultPickerSettings.PICKER_NAME), updated))

            item.setAction { event ->
                event.isCancelled = true
                open(guild, player, vaultNumber)
            }

            gui.addItem(item)
        }
    }
}
