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
package me.glaremasters.guilds.exte

import com.cryptomorin.xseries.XMaterial
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import me.glaremasters.guilds.utils.ItemBuilder
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

internal fun addBackground(gui: Gui) {
    val builder = ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.GLASS_PANE))
    builder.setName(StringUtils.color("&r"))
    val item = GuiItem(builder.build())
    item.setAction { event ->
        event.isCancelled = true
    }
    gui.filler.fill(item)
}

internal fun addBottom(gui: PaginatedGui) {
    val builder = ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.GLASS_PANE))
    builder.setName(StringUtils.color("&r"))
    val item = GuiItem(builder.build())
    item.setAction { event ->
        event.isCancelled = true
    }
    gui.filler.fillBottom(item)
}

fun Double.rounded(): Double {
    return String.format(Locale.ENGLISH, "%.2f", this).toDouble()
}
