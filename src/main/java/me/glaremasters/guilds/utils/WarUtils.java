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
package me.glaremasters.guilds.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

/**
 * Created by Glare
 * Date: 1/15/2021
 * Time: 1:26 PM
 */
public class WarUtils {

    /**
     * Special method designed to notify a player based on the specific way set in the config
     *
     * @param type     the type of way to notify the player
     * @param message  the message to send the player
     * @param audience the player to send the message to
     */
    public static void notify(final String type, final String message, final Audience audience) {
        final Component updated = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        switch (type.toLowerCase()) {
            case "title":
                audience.showTitle(Title.title(updated, Component.empty()));
                break;
            case "subtitle":
                audience.showTitle(Title.title(Component.empty(), updated));
                break;
            case "none":
                break;
            default:
            case "actionbar":
                audience.sendActionBar(updated);
                break;
        }
    }
}
