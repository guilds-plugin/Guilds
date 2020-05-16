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
import java.text.SimpleDateFormat
import java.util.Comparator
import java.util.Date
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildInfoMemberSettings
import me.glaremasters.guilds.configuration.sections.GuildListSettings
import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildMember
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.entity.Player

class MembersGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {

    fun get(guild: Guild): Gui {
        val name = settingsManager.getProperty(GuildInfoMemberSettings.GUI_NAME).replace("{name}", guild.name)
        val gui = GuiBuilder(guilds).setName(name).setRows(6).disableGlobalClicking().build()

        gui.setOutsideClickAction { event ->
            event.isCancelled = true
            val player = event.whoClicked as Player
            val playerGuild = guildHandler.getGuild(player)
            if (playerGuild == null) guilds.guiHandler.list.get.open(event.whoClicked) else guilds.guiHandler.info.get(playerGuild, player).open(event.whoClicked)
        }

        addItems(gui, guild)
        addBackground(gui)
        return gui
    }

    /**
     * Create the regular items that will be on the GUI
     *
     * @param pane the pane to be added to
     * @param guild the guild of the player
     */
    private fun addItems(gui: Gui, guild: Guild) {
        val members = guild.members

        when (settingsManager.getProperty(GuildInfoMemberSettings.SORT_ORDER).toUpperCase()) {
            "ROLE" -> members.sortWith(Comparator.comparingInt { g: GuildMember -> g.role.level })
            "NAME" -> members.sortWith(compareBy(GuildMember::name))
            "AGE" -> members.sortWith(Comparator.comparingLong(GuildMember::joinDate))
            else -> members.sortWith(Comparator.comparingInt { g: GuildMember -> g.role.level })
        }

        val sdf = SimpleDateFormat(settingsManager.getProperty(GuildListSettings.GUI_TIME_FORMAT))
        val lore = settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_LORE)

        members.forEach { member ->
            val status = if (member.isOnline) settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_ONLINE) else settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_OFFLINE)
            val role = guildHandler.getGuildRole(member.role.level)
            val name = member.name
            val updated = mutableListOf<String>()

            lore.forEach { line ->
                updated.add(StringUtils.color(line
                        .replace("{name}", name.toString()))
                        .replace("{role}", role.name)
                        .replace("{join}", sdf.format(Date(member.joinDate)))
                        .replace("{login}", sdf.format(Date(member.lastLogin)))
                        .replace("{status}", status))
            }

            val item = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_MATERIAL), settingsManager.getProperty(GuildInfoMemberSettings.MEMBERS_NAME).replace("{player}", name.toString()), updated))
            item.setAction { event ->
                event.isCancelled = true
            }
            gui.addItem(item)
        }
    }
}
