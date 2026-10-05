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
