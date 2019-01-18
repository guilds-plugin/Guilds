/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.handlers;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.entity.Player;

import static co.aikar.commands.ACFBukkitUtil.color;

/**
 * Created by GlareMasters
 * Date: 9/26/2018
 * Time: 8:10 PM
 */
@AllArgsConstructor
public class Tablist {

    //todo

    private Guilds guilds;
    private GuildHandler guildHandler;

    /**
     * Handles adding a prefix to the player's tablist
     *
     * @param player the player being modified
     */
    public void add(Player player) {
        Guild guild = guildHandler.getGuild(player);
        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
        player.setPlayerListName(color(guilds.getConfig().getString("tablist")
                .replace("{guild}", guild.getName())
                .replace("{prefix}", guild.getPrefix()) + name));
    }

    /**
     * Handles removing a prefix from the player's tablist
     *
     * @param player the player being modified
     */
    public void remove(Player player) {
        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
        player.setPlayerListName(color(name));
    }
}
