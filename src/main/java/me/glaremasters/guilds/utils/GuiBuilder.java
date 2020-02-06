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

package me.glaremasters.guilds.utils;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GuiBuilder {
    private final Gui gui;

    public GuiBuilder(Plugin plugin) {
        this.gui = new Gui(plugin, 1, "");
    }

    @NotNull
    @Contract(pure = true)
    public Gui build() {
        return gui;
    }

    @NotNull
    public GuiBuilder setName(@NotNull String name) {
        gui.setTitle(StringUtils.color(name));
        return this;
    }

    @NotNull
    public GuiBuilder setRows(int rows) {
        gui.setRows(rows);
        return this;
    }

    @NotNull
    public GuiBuilder addBackground(int length, int height) {
        OutlinePane background = new OutlinePane(0, 0, length, height, Pane.Priority.LOW);
        ItemBuilder builder = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
        builder.setName(StringUtils.color("&r"));
        background.addItem(new GuiItem(builder.build()));
        background.setRepeat(true);
        gui.addPane(background);
        return this;
    }

    @NotNull
    public GuiBuilder blockGlobalClick() {
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        return this;
    }
}
