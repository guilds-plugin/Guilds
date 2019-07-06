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

package me.glaremasters.guilds.tasks;

import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.utils.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Created by Glare
 * Date: 7/5/2019
 * Time: 8:06 PM
 */
public class GuildWarTimeTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private int timeLeft;
    private List<Player> players;
    private String message;
    private GuildChallenge challenge;

    public GuildWarTimeTask(JavaPlugin plugin, int timeLeft, List<Player> players, String message, GuildChallenge challenge) {
        this.plugin = plugin;
        this.timeLeft = timeLeft;
        this.players = players;
        this.message = message;
        this.challenge = challenge;
    }

    @Override
    public void run() {
        players.forEach(p -> JSONMessage.actionbar(message.replace("{amount}", String.valueOf(timeLeft)), p));
        timeLeft--;
        if (timeLeft == 0) {
            challenge.setJoinble(false);
            cancel();
        }
    }
}
