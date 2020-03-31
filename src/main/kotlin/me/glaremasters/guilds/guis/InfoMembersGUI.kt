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
import com.github.stefvanschie.inventoryframework.Gui
import com.github.stefvanschie.inventoryframework.GuiItem
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.Pane
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildInfoMemberSettings
import me.glaremasters.guilds.configuration.sections.GuildListSettings
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildMember
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.text.SimpleDateFormat
import java.util.Comparator
import java.util.Date
import java.util.function.Consumer

class InfoMembersGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {

    fun getInfoMembersGUI(guild: Guild): Gui {
        val name = settingsManager.getProperty(GuildInfoMemberSettings.GUI_NAME).replace("{name}", guild.name)
        val gui = GuiBuilder(guilds).setName(name).setRows(6).addBackground(9, 6).blockGlobalClick().build()

        // Prevent players from being able to items into the GUIs
        gui.setOnOutsideClick { event: InventoryClickEvent ->
            event.isCancelled = true
            val player = event.whoClicked as Player
            val g = guildHandler.getGuild(player)
            if (g == null) {
                guilds.guiHandler.listGUI.listGUI.show(event.whoClicked)
            } else {
                guilds.guiHandler.infoGUI.getInfoGUI(g, player).show(event.whoClicked)
            }
        }

        // Create the pane for the main items
        val foregroundPane = OutlinePane(0, 0, 9, 6, Pane.Priority.NORMAL)

        // Add the items to the foreground pane
        createForegroundItems(foregroundPane, guild)

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
    private fun createForegroundItems(pane: OutlinePane, guild: Guild) {
        val members = guild.members
        when (settingsManager.getProperty(GuildInfoMemberSettings.SORT_ORDER).toUpperCase()) {
            "ROLE" -> members.sortWith(Comparator.comparingInt { g: GuildMember -> g.role.level })
            "NAME" -> members.sortWith(compareBy(GuildMember::name))
            "AGE" -> members.sortWith(Comparator.comparingLong(GuildMember::joinDate))
            else -> members.sortWith(Comparator.comparingInt { g: GuildMember -> g.role.level })
        }
        val sdf = SimpleDateFormat(settingsManager.getProperty(GuildListSettings.GUI_TIME_FORMAT))

        val lore = settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_LORE)

        members.forEach(Consumer { m: GuildMember ->
            val status = if (m.isOnline) settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_ONLINE) else settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_OFFLINE)
            val role = guildHandler.getGuildRole(m.role.level)
            val name = m.asOfflinePlayer.name

            val updated: MutableList<String> = ArrayList()

            lore.forEach(Consumer { line: String ->
                updated.add(StringUtils.color(line
                                .replace("{name}", name.toString()))
                        .replace("{role}", role.name)
                        .replace("{join}", sdf.format(Date(m.joinDate)))
                        .replace("{login}", sdf.format(Date(m.lastLogin)))
                        .replace("{status}", status))
            })

            pane.addItem(GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_MATERIAL),
                    settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_NAME).replace("{player}", name.toString()),
                    updated), Consumer { event: InventoryClickEvent ->
                event.isCancelled = true
            }))
        })
    }

}
