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
import me.glaremasters.guilds.configuration.sections.GuildListSettings
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.utils.GuiBuilderTwo
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem

class ListGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {
    private val items: MutableList<GuiItem>

    val listGUI: Gui
        get() {
            val name = settingsManager.getProperty(GuildListSettings.GUILD_LIST_NAME)
            val gui = GuiBuilderTwo(guilds).setName(name).setRows(6).build()
/*
            val paginatedPane = PaginatedPane(0, 0, 9, 5)

            val pane = StaticPane(0, 5, 9, 1)
            createButtons(pane, paginatedPane, gui)

            createListItems(paginatedPane)

            gui.addPane(paginatedPane)
            gui.addPane(pane)*/

            return gui
        }

/*    // Next Button
    private fun createButtons(pane: StaticPane, paginatedPane: PaginatedPane, gui: Gui) {
        pane.addItem(GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildListSettings.GUILD_LIST_NEXT_PAGE_ITEM), settingsManager.getProperty(GuildListSettings.GUILD_LIST_NEXT_PAGE_ITEM_NAME), ArrayList()), Consumer {
            if (paginatedPane.page + 1 + 1 <= paginatedPane.pages) {
                paginatedPane.page = paginatedPane.page + 1
                gui.update()
            }
        }), 8, 0)

        // Back Button
        pane.addItem(GuiItem(GuiUtils.createItem(settingsManager.getProperty(GuildListSettings.GUILD_LIST_PREVIOUS_PAGE_ITEM), settingsManager.getProperty(GuildListSettings.GUILD_LIST_PREVIOUS_PAGE_ITEM_NAME), ArrayList()), Consumer {
            if (paginatedPane.page - 1 >= 0) {
                paginatedPane.page = paginatedPane.page - 1
                gui.update()
            }
        }), 0, 0)
    }*/
/*
    *//**
     * Create all the items for the GUI
     *
     * @param pane the pane to add the items to
     *//*
    private fun createListItems(pane: PaginatedPane) {
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
        // Loop through each guild to create the item
        guilds.forEach(Consumer { guild: Guild -> setListItem(guild) })
        pane.populateWithGuiItems(items)
        items.clear()
    }

    *//**
     * Set the item to the list
     *
     * @param guild the guild of the pane
     *//*
    private fun setListItem(guild: Guild) {
        val listItem = GuiItem(guild.skull, Consumer { event: InventoryClickEvent ->
            guilds.guiHandler.infoMembersGUI.getInfoMembersGUI(guild).open(event.whoClicked)
            event.isCancelled = true
        })
        val meta = listItem.item.itemMeta

        var name = settingsManager.getProperty(GuildListSettings.GUILD_LIST_ITEM_NAME)
        name = StringUtils.color(name)
        name = name.replace("{player}", if (guild.guildMaster != null) guild.guildMaster.asOfflinePlayer.name.toString() else "Master")
        name = name.replace("{guild}", guild.name)

        meta?.setDisplayName(name)
        meta?.lore = updatedLore(guild, settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_LORE))
        listItem.item.itemMeta = meta
        items.add(listItem)
    }

    *//**
     * Update lore with replacements
     *
     * @param guild the guild being edited
     * @param lore  the lore to change
     * @return updated lore
     *//*
    private fun updatedLore(guild: Guild, lore: List<String>): List<String> {
        val sdf = SimpleDateFormat(settingsManager.getProperty(GuildListSettings.GUI_TIME_FORMAT))
        val status = if (guild.isPrivate) settingsManager.getProperty(GuildInfoSettings.STATUS_PRIVATE) else settingsManager.getProperty(GuildInfoSettings.STATUS_PUBLIC)

        val tier = if (guild.tier != null) guild.tier.level.toString() else "1"
        val tierName = if (guild.tier != null) guild.tier.name else "Default"

        val updated: MutableList<String> = ArrayList()
        lore.forEach(Consumer { line: String ->
            updated.add(StringUtils.color(line
                    .replace("{guild-name}", guild.name)
                    .replace("{guild-prefix}", guild.prefix)
                    .replace("{guild-master}", guild.guildMaster.asOfflinePlayer.name.toString())
                    .replace("{guild-status}", status)
                    .replace("{guild-tier}", tier)
                    .replace("{guild-balance}", guild.balance.toString())
                    .replace("{guild-member-count}", guild.size.toString())
                    .replace("{guild-challenge-wins}", java.lang.String.valueOf(guild.guildScore.wins))
                    .replace("{guild-challenge-loses}", java.lang.String.valueOf(guild.guildScore.loses))
                    .replace("{creation}", sdf.format(guild.creationDate))
                    .replace("{guild-tier-name}", tierName)))
        })
        return updated
    }*/

    init {
        items = ArrayList()
    }
}
