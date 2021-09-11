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
import com.cryptomorin.xseries.SkullUtils
import com.cryptomorin.xseries.XMaterial
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.claim.ClaimRelations
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings
import me.glaremasters.guilds.configuration.sections.GuildListSettings
import me.glaremasters.guilds.configuration.sections.GuildMapSettings

import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildSkull
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.ItemBuilder
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.codemc.worldguardwrapper.WorldGuardWrapper
import java.text.SimpleDateFormat

class MapGUI(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler) {
    private val items: MutableList<GuiItem>

    fun get(player: Player): Gui {
        val name = settingsManager.getProperty(GuildMapSettings.GUILD_MAP_NAME)

        val gui = Gui(5, StringUtils.color(name))

        gui.setDefaultClickAction { event ->
            event.isCancelled = true
        }

        populateMap(gui, player)
        setYouPoint(gui, player)
        addBackground(gui)

        return gui
    }

    private fun setYouPoint(gui: Gui, player: Player) {
        val guild = guildHandler.getGuild(player)

        val defaultUrl = settingsManager.getProperty(GuildMapSettings.GUILD_MAP_CENTER_HEAD_DEFAULT_URL)
        val useDefaultUrl = settingsManager.getProperty(GuildMapSettings.USE_DEFAULT_TEXTURE)
        val item = if (!useDefaultUrl) SkullUtils.getSkull(player.uniqueId) else GuildSkull(defaultUrl).itemStack
        val meta = item.itemMeta

        var name = settingsManager.getProperty(GuildMapSettings.GUILD_MAP_CENTER_NAME)

        name = StringUtils.color(name)
        name = name.replace("{player}", if (guild.guildMaster != null) guild.guildMaster.name.toString() else "Master")
        name = name.replace("{guild}", guild.name)

        meta?.setDisplayName(name)
        meta?.lore = updatedLore(guild, settingsManager.getProperty(GuildMapSettings.GUILD_MAP_ITEM_LORE))

        item.itemMeta = meta

        val guiItem = GuiItem(item)

        guiItem.setAction { event ->
            event.isCancelled = true
            guilds.guiHandler.members.get(guild, player).open(event.whoClicked)
        }

        gui.setItem(3, 5, guiItem)
    }

    private fun populateMap(gui: Gui, player: Player) {
        val wrapper = WorldGuardWrapper.getInstance()

        val claims = ClaimRelations.getMap(wrapper, player, guilds)


        val defaultUrl = settingsManager.getProperty(GuildMapSettings.GUILD_MAP_HEAD_DEFAULT_URL)
        val useDefaultUrl = settingsManager.getProperty(GuildMapSettings.USE_DEFAULT_TEXTURE)

        for (i in 0..4) {
            for (j in 0..8) {

                val claim = claims[i][j]

                if (claim != null) {
                    val guild = claim.getGuild(guilds.guildHandler)

                    val item = if (!useDefaultUrl) guild.skull else GuildSkull(defaultUrl).itemStack
                    val meta = item.itemMeta

                    var name = settingsManager.getProperty(GuildMapSettings.GUILD_MAP_ITEM_NAME)

                    name = StringUtils.color(name)
                    name = name.replace("{player}", if (guild.guildMaster != null) guild.guildMaster.name.toString() else "Master")
                    name = name.replace("{guild}", guild.name)

                    meta?.setDisplayName(name)
                    meta?.lore = updatedLore(guild, settingsManager.getProperty(GuildMapSettings.GUILD_MAP_ITEM_LORE))

                    item.itemMeta = meta

                    val guiItem = GuiItem(item)

                    guiItem.setAction { event ->
                        event.isCancelled = true
                        guilds.guiHandler.members.get(guild, player).open(event.whoClicked)
                    }

                    gui.setItem(i+1, j+1, guiItem)
                }
                else {
                    val builder = ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.GLASS_PANE))
                    builder.setName(StringUtils.color("&r"))
                    val item = GuiItem(builder.build())
                    item.setAction { event ->
                        event.isCancelled = true
                    }
                    gui.setItem(i+1, j+1, item)
                }
            }
        }
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
