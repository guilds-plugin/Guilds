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
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Glare
 * Date: 7/13/2019
 * Time: 6:23 PM
 */
public class GuildWarChallengeCheckTask extends BukkitRunnable {

    private Guilds guilds;
    private GuildChallenge challenge;
    private ChallengeHandler challengeHandler;

    public GuildWarChallengeCheckTask(Guilds guilds, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
    }


    @Override
    public void run() {
        // Check if it was denied
        if (challengeHandler.getChallenge(challenge.getId()) != null) {
            // War system has already started if it's accepted so don't do anything
            if (challenge.isAccepted()) {
                return;
                // They have not accepted or denied it, so let's auto deny it
            } else {
                // Send message to challenger saying they didn't accept it
                challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__GUILD_EXPIRED_CHALLENGE,
                        "{guild}", challenge.getDefender().getName());
                // Send message to defender saying they didn't accept it
                challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__TARGET_EXPIRED_CHALLENGE);
                // Unreserve arena
                challenge.getArena().setInUse(false);
                // Remove the challenge from the list
                challengeHandler.removeChallenge(challenge);
            }
        }
    }
}
