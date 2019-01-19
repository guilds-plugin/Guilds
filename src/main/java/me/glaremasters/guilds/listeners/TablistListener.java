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

package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.handlers.Tablist;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by GlareMasters
 * Date: 11/12/2018
 * Time: 12:33 AM
 */
@AllArgsConstructor
public class TablistListener implements Listener {

    //todo

    private GuildHandler guildHandler;

    /**
     * This will check if the server uses Guild's TablistListener and will add a prefix to their name
     * @param event
     */
    @EventHandler
    public void onTablist(PlayerJoinEvent event) {
        Tablist tablist = new Tablist(guilds, guildHandler);
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);
        if (guild != null) guilds.getServer().getScheduler().scheduleAsyncDelayedTask(guilds, () -> tablist.add(player), 30L);
    }

    // This was the old TabList class's methods.
//    /**
//     * Handles adding a prefix to the player's tablist
//     *
//     * @param player the player being modified
//     */
//    public void add(Player player) {
//        Guild guild = guildHandler.getGuild(player);
//        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
//        player.setPlayerListName(color(guilds.getConfig().getString("tablist")
//                .replace("{guild}", guild.getName())
//                .replace("{prefix}", guild.getPrefix()) + name));
//    }
//
//    /**
//     * Handles removing a prefix from the player's tablist
//     *
//     * @param player the player being modified
//     */
//    public void remove(Player player) {
//        String name = guilds.getConfig().getBoolean("tablist-use-display-name") ? player.getDisplayName() : player.getName();
//        player.setPlayerListName(color(name));
//    }

}
