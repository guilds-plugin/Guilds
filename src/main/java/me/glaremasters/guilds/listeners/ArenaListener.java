package me.glaremasters.guilds.listeners;

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
                challenge.getArena().setInUse(false);
                challengeHandler.announceWinner(challenge, guilds.getCommandManager());
                challengeHandler.teleportRemaining(challenge);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        // Check to make sure both parties are players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        // Get a copy of the killer and player being killed
        Player entity = (Player) event.getEntity();
        Player killer = (Player) event.getDamager();

        // Check to make sure this damage would kill them to prevent excess checking
        if (entity.getHealth() - event.getFinalDamage() > 1) {
            return;
        }

        // Cancel the last damage so they don't die
        event.setCancelled(true);

        // Check to make sure they have a challenge
        GuildChallenge challenge = challengeHandler.getChallenge(entity);

        // Make sure it's not null
        if (challenge != null) {
            // Teleport them out of the arena
            challengeHandler.exitArena(entity, challenge);
            // Remove them
            challengeHandler.removePlayer(entity);
            // Check if they were last person or not
            if (challengeHandler.checkIfOver(challenge)) {
                // Do something
                challenge.setStarted(false);
                challenge.getArena().setInUse(false);
                challengeHandler.announceWinner(challenge, guilds.getCommandManager());
                challengeHandler.teleportRemaining(challenge);
            }
        }
    }

}
