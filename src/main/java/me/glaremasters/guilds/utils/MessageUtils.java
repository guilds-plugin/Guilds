/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKeyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageUtils {

    /**
     * Helper method to convert a message key from lang files into a String
     *
     * @param issuer the issuer that requested the message
     * @param key    the key to translate
     * @return key translated to string
     */
    public static String asString(@NotNull final CommandIssuer issuer, @NotNull final MessageKeyProvider key) {
        return issuer.getManager().getLocales().getMessage(issuer, key);
    }

    /**
     * Helper method to convert a message key from a lang file into a String
     *
     * @param player  the player to translate for (gets their locale if they aren't using default)
     * @param manager the manager of the plugin
     * @param key     the key to translate
     * @return key translated to string
     */
    public static String asString(@NotNull final Player player, @NotNull final PaperCommandManager manager, @NotNull final MessageKeyProvider key) {
        return manager.getLocales().getMessage(manager.getCommandIssuer(player), key);
    }

    /**
     * Helper method to convert a message key from lang files into a String
     *
     * @param manager the manager of the plugin
     * @param key     the key to translate
     * @return key translated to string
     */
    public static String asString(@NotNull final PaperCommandManager manager, @NotNull final MessageKeyProvider key) {
        return manager.getLocales().getMessage(manager.getCommandIssuer(Bukkit.getConsoleSender()), key);
    }
}
