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

import me.mattstudios.mfgui.gui.guis.Gui;
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
        gui.updateTitle(StringUtils.color(name));
        return this;
    }

    @NotNull
    public GuiBuilder setRows(int rows) {
        gui.setRows(rows);
        return this;
    }

    @NotNull
    public GuiBuilder disableGlobalClicking() {
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        return this;
    }
}
