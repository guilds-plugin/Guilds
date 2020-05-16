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
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings
import me.glaremasters.guilds.configuration.sections.GuildListSettings
import me.glaremasters.guilds.exte.addBottom
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildSkull
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import me.mattstudios.mfgui.gui.guis.GuiItem
import me.mattstudios.mfgui.gui.guis.PaginatedGui

class ListGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {
    private val items: MutableList<GuiItem>

    val get: PaginatedGui
        get() {
            val name = settingsManager.getProperty(GuildListSettings.GUILD_LIST_NAME)
            val gui = PaginatedGui(guilds, 6, 45, StringUtils.color(name))

            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }

            createListItems(gui)
            addBottom(gui)
            createButtons(gui)

            return gui
        }

    private fun createButtons(gui: PaginatedGui) {
        val next = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildListSettings.GUILD_LIST_NEXT_PAGE_ITEM), settingsManager.getProperty(GuildListSettings.GUILD_LIST_NEXT_PAGE_ITEM_NAME), emptyList()))
        next.setAction {
            gui.nextPage()
        }

        val back = GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildListSettings.GUILD_LIST_PREVIOUS_PAGE_ITEM), settingsManager.getProperty(GuildListSettings.GUILD_LIST_PREVIOUS_PAGE_ITEM_NAME), emptyList()))
        back.setAction {
            gui.prevPage()
        }

        gui.setItem(6, 9, next)
        gui.setItem(6, 1, back)
    }

    private fun createListItems(gui: PaginatedGui) {
        val guilds = guildHandler.guilds

        when (settingsManager.getProperty(GuildListSettings.GUILD_LIST_SORT).toUpperCase()) {
            "TIER" -> guilds.sortWith(Comparator.comparingInt { g: Guild -> g.tier.level }.reversed())
            "MEMBERS" -> guilds.sortWith(Comparator.comparingInt { g: Guild -> g.members.size }.reversed())
            "BALANCE" -> guilds.sortWith(Comparator.comparingDouble { obj: Guild -> obj.balance }.reversed())
            "WINS" -> guilds.sortWith(Comparator.comparingInt { g: Guild -> g.guildScore.wins }.reversed())
            "NAME" -> guilds.sortWith(Comparator.comparing { obj: Guild -> obj.name })
            "AGE" -> guilds.sortWith(Comparator.comparingLong { obj: Guild -> obj.creationDate })
            "LOADED" -> {
            }
        }

        guilds.forEach { guild ->
            setListItem(guild)
        }

        items.forEach { item ->
            gui.addItem(item)
        }

        items.clear()
    }

    private fun setListItem(guild: Guild) {
        val defaultUrl = settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_DEFAULT_URL)
        val useDefaultUrl = settingsManager.getProperty(GuildListSettings.USE_DEFAULT_TEXTURE)
        val item = if (!useDefaultUrl) guild.skull else GuildSkull(defaultUrl).itemStack
        val meta = item.itemMeta
        var name = settingsManager.getProperty(GuildListSettings.GUILD_LIST_ITEM_NAME)

        name = StringUtils.color(name)
        name = name.replace("{player}", if (guild.guildMaster != null) guild.guildMaster.name.toString() else "Master")
        name = name.replace("{guild}", guild.name)

        meta?.setDisplayName(name)
        meta?.lore = updatedLore(guild, settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_LORE))

        item.itemMeta = meta

        val guiItem = GuiItem(item)

        guiItem.setAction { event ->
            event.isCancelled = true
            guilds.guiHandler.members.get(guild).open(event.whoClicked)
        }

        items.add(guiItem)
    }

    /**
     * Update lore with replacements
     *
     * @param guild the guild being edited
     * @param lore the lore to change
     * @return updated lore
     */
    private fun updatedLore(guild: Guild, lore: List<String>): List<String> {
        val sdf = SimpleDateFormat(settingsManager.getProperty(GuildListSettings.GUI_TIME_FORMAT))
        val status = if (guild.isPrivate) settingsManager.getProperty(GuildInfoSettings.STATUS_PRIVATE) else settingsManager.getProperty(GuildInfoSettings.STATUS_PUBLIC)

        val tier = if (guild.tier != null) guild.tier.level.toString() else "1"
        val tierName = if (guild.tier != null) guild.tier.name else "Default"

        val updated: MutableList<String> = ArrayList()

        lore.forEach { line ->
            updated.add(StringUtils.color(line
                    .replace("{guild-name}", guild.name)
                    .replace("{guild-prefix}", guild.prefix)
                    .replace("{guild-master}", guild.guildMaster.asOfflinePlayer.name.toString())
                    .replace("{guild-status}", status)
                    .replace("{guild-tier}", tier)
                    .replace("{guild-balance}", EconomyUtils.format(guild.balance))
                    .replace("{guild-member-count}", guild.size.toString())
                    .replace("{guild-challenge-wins}", guild.guildScore.wins.toString())
                    .replace("{guild-challenge-loses}", guild.guildScore.loses.toString())
                    .replace("{creation}", sdf.format(guild.creationDate))
                    .replace("{guild-tier-name}", tierName)))
        }

        return updated
    }

    init {
        items = ArrayList()
    }
}
