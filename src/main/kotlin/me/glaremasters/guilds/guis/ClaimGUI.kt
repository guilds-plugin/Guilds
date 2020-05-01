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

import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import me.mattstudios.mfgui.gui.guis.GuiItem
import me.mattstudios.mfgui.gui.guis.PaginatedGui
import org.bukkit.Material

class ClaimGUI (private val guilds: Guilds) {
    fun get(guild: Guild) : PaginatedGui {
        val gui = PaginatedGui(guilds, 6, 45, StringUtils.color(guild.name + "'s Guild Claim"))
        setClaimItems(gui, guild)
        return gui
    }

    private fun setClaimItems(gui: PaginatedGui, guild: Guild) {
        val claims = guild.claims
        claims.forEach { claim ->
            val item = GuiUtils.createItem(Material.GRASS_BLOCK.name, "Guild Claim", listOf("World:" + claim.world, "X:" + claim.x.toString(), "Z:" + claim.z.toString()))
            val guiItem = GuiItem(item)
            gui.addItem(guiItem)
        }
    }
}