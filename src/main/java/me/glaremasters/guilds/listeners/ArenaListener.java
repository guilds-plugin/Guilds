package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class ArenaListener implements Listener {

    private ChallengeHandler challengeHandler;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Check if they are part of an active challenge
        if (challengeHandler.getChallenge(player) != null) {
            // Remove the player from the alive players
            challengeHandler.removePlayer(player);
        }
    }

}
