package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class ArenaListener implements Listener {

    private Guilds guilds;
    private ChallengeHandler challengeHandler;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GuildChallenge challenge = challengeHandler.getChallenge(player);
        // Check if they are part of an active challenge
        if (challenge != null) {
            // Remove the player from the alive players
            challengeHandler.removePlayer(player);
            if (challengeHandler.checkIfOver(challenge)) {
                // Do something
                challenge.setStarted(false);
                challengeHandler.announceWinner(challenge, guilds.getCommandManager());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GuildChallenge challenge = challengeHandler.getChallenge(player);
        // Check if they are in a war
        if (challenge != null) {
            // Remove them from the alive list
            challengeHandler.removePlayer(player);
            // Check if they were last person or not
            if (challengeHandler.checkIfOver(challenge)) {
                // Do something
                challenge.setStarted(false);
                challengeHandler.announceWinner(challenge, guilds.getCommandManager());
            }
        }
    }

}
