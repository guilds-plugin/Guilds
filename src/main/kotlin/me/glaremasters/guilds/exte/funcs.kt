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
import me.glaremasters.guilds.utils.LoggingUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * The glass pane every Guilds GUI uses as filler.
 *
 * GRAY_STAINED_GLASS_PANE only exists from Minecraft 1.12, so the fallback has to be resolved
 * through XSeries as well. On 1.8.8 the pane is `Material.THIN_GLASS`, and neither that name nor
 * the modern `Material.GLASS_PANE` exists there: this project compiles against a current Spigot
 * API, so a raw `Material.GLASS_PANE` compiles fine and then throws `NoSuchFieldError` the first
 * time anybody opens a GUI on an old server. XSeries's [XMaterial.GLASS_PANE] maps to `THIN_GLASS`
 * on 1.8.8 and `GLASS_PANE` on 1.12+.
 *
 * This resolves an [ItemStack] rather than a [org.bukkit.Material] on purpose. On pre-flattening
 * versions the grey pane is a stained pane carrying a data value, so taking the Material and
 * building a fresh `ItemStack` from it would drop the grey and hand back a white pane. Only XSeries
 * knows the data value for the running version.
 *
 * Resolved once and cloned per call: [ItemBuilder] mutates the stack it is handed, and two GUIs must
 * never share one instance.
 */
private val fillerPane: ItemStack? by lazy {
    val pane = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem() ?: XMaterial.GLASS_PANE.parseItem()

    if (pane == null) {
        LoggingUtils.warn("Could not resolve a glass pane material on this server. Guilds GUI fillers will be skipped.")
    }

    pane
}

private fun fillerItem(): ItemStack? = fillerPane?.clone()

internal fun addBackground(gui: Gui) {
    val item = fillerItem() ?: return
    val builder = ItemBuilder(item)
    builder.setName(StringUtils.color("&r"))
    val filler = GuiItem(builder.build())
    filler.setAction { event ->
        event.isCancelled = true
    }
    gui.filler.fill(filler)
}

internal fun addBottom(gui: PaginatedGui) {
    val item = fillerItem() ?: return
    val builder = ItemBuilder(item)
    builder.setName(StringUtils.color("&r"))
    val filler = GuiItem(builder.build())
    filler.setAction { event ->
        event.isCancelled = true
    }
    gui.filler.fillBottom(filler)
}

fun Double.rounded(): Double {
    return String.format(Locale.ENGLISH, "%.2f", this).toDouble()
}
