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

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Glare
 * Date: 7/13/2019
 * Time: 6:49 PM
 */
public class GuildWarReadyTask extends BukkitRunnable {

    private Guilds guilds;
    private int timeLeft;
    private List<UUID> players;
    private String message;
    private GuildChallenge challenge;
    private ChallengeHandler challengeHandler;

    public GuildWarReadyTask(Guilds guilds, int timeLeft, List<UUID> players, String message, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.timeLeft = timeLeft;
        this.players = players;
        this.message = message;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
    }

    @Override
    public void run() {
        players.forEach(p -> {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                JSONMessage.actionbar(message.replace("{amount}", String.valueOf(timeLeft)), player);
            }
        });
        timeLeft--;
        if (timeLeft == 0) {
            challenge.setJoinble(false);
            if (!challengeHandler.checkEnoughOnline(challenge.getChallenger(), challenge.getDefender(), challenge.getMinPlayersPerSide())) {
                challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_ON);
                challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_ON);
                challenge.getArena().setInUse(false);
                challengeHandler.removeChallenge(challenge);
                cancel();
                return;
            }
            // Create final list for both sides
            challengeHandler.prepareFinalList(challenge.getChallengePlayers(), challenge, "challenger");
            challengeHandler.prepareFinalList(challenge.getDefendPlayers(), challenge, "defender");

            List<String> heldBack = new ArrayList<>();
            // Make sure both are the same size
            if (challenge.getAliveDefenders().size() > challenge.getAliveChallengers().size()) {
                do {
                    UUID last = Iterables.getLast(challenge.getAliveDefenders().entrySet()).getKey();
                    heldBack.add(Bukkit.getPlayer(last).getName());
                    challenge.getAliveDefenders().remove(last);
                } while (challenge.getAliveDefenders().size() != challenge.getAliveChallengers().size());
            } else if (challenge.getAliveChallengers().size() > challenge.getAliveDefenders().size()) {
                do {
                    UUID last = Iterables.getLast(challenge.getAliveChallengers().entrySet()).getKey();
                    heldBack.add(Bukkit.getPlayer(last).getName());
                    challenge.getAliveChallengers().remove(last);
                } while (challenge.getAliveChallengers().size() != challenge.getAliveDefenders().size());
            }

            if (heldBack.size() > 0) {
                String heldBackMessage = Joiner.on(", ").join(heldBack);
                challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__REMOVED_FOR_SIZE, "{players}", heldBackMessage);
                challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__REMOVED_FOR_SIZE, "{players}", heldBackMessage);
            }
            heldBack.clear();
            
            // Send them both to the arena
            challengeHandler.sendToArena(challenge.getAliveChallengers(), challenge.getArena().getChallengerLoc());
            challengeHandler.sendToArena(challenge.getAliveDefenders(), challenge.getArena().getDefenderLoc());
            challenge.setStarted(true);
            challenge.getDefender().setLastDefended(System.currentTimeMillis());
            cancel();
        }
    }
}
