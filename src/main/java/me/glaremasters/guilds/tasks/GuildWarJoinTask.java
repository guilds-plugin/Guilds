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
package me.glaremasters.guilds.tasks;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.WarUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Glare
 * Date: 7/13/2019
 * Time: 6:42 PM
 */
public class GuildWarJoinTask extends BukkitRunnable {

    private final Guilds guilds;
    private int timeLeft;
    private final int readyTime;
    private final List<UUID> players;
    private final String joinMsg;
    private final String readyMsg;
    private final GuildChallenge challenge;
    private final ChallengeHandler challengeHandler;
    private final String notifyType;

    public GuildWarJoinTask(Guilds guilds, int timeLeft, int readyTime, List<UUID> players, String joinMsg, String readyMsg, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.timeLeft = timeLeft;
        this.readyTime = readyTime;
        this.players = players;
        this.joinMsg = joinMsg;
        this.readyMsg = readyMsg;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
        this.notifyType = guilds.getSettingsHandler().getMainConf().getProperty(WarSettings.NOTIFY_TYPE);
    }


    @Override
    public void run() {
        players.forEach(p -> {
            final Player player = Bukkit.getPlayer(p);
            if (player != null) {
                WarUtils.notify(notifyType, joinMsg.replace("{amount}", String.valueOf(timeLeft)), guilds.getAdventure().player(player));
            }
        });
        timeLeft--;
        if (timeLeft == 0) {
            challenge.setJoinble(false);
            if (!challengeHandler.checkEnoughJoined(challenge)) {
                challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
                challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
                challenge.getArena().setInUse(false);
                challengeHandler.removeChallenge(challenge.getId());
                cancel();
                return;
            }
            List<UUID> warReady = Stream.concat(challenge.getChallengePlayers().stream(), challenge.getDefendPlayers().stream()).collect(Collectors.toList());
            new GuildWarReadyTask(guilds, readyTime, warReady, readyMsg, challenge, challengeHandler).runTaskTimer(guilds, 0L, 20L);
            cancel();
        }
    }
}
