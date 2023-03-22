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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Utility class for working with players in Minecraft.
 */
public class PlayerUtils {

    /**
     * Check if a player exists with the specified name.
     *
     * @param target the name of the player to check
     * @return {@code true} if the player exists, {@code false} otherwise
     */
    public static boolean doesExist(String target) {
        return Bukkit.getOfflinePlayer(target) != null;
    }

    /**
     * Retrieve a player object from their name.
     *
     * @param target the name of the player
     * @return the player object for the specified name
     */
    public static OfflinePlayer getPlayer(String target) {
        return Bukkit.getOfflinePlayer(target);
    }

    /**
     * Retrieve a player object from their UUID.
     *
     * @param uuid the UUID of the player
     * @return the player object for the specified UUID
     */
    public static OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }
}
