package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.database.challenges.ChallengesProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

@AllArgsConstructor
public class ArenaListener implements Listener {

    private Guilds guilds;
    private ChallengeHandler challengeHandler;
    private ChallengesProvider challengesProvider;
    private SettingsManager settingsManager;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GuildChallenge challenge = challengeHandler.getChallenge(player);
        // Check if they are part of an active challenge
        if (challenge != null) {
            // Remove the player from the alive players
            handleExist(player, challenge);
        }
    }

    private void handleExist(Player player, GuildChallenge challenge) {
        challengeHandler.removePlayer(player);
        if (challengeHandler.checkIfOver(challenge)) {
            // Specify the war is over
            challenge.setStarted(false);
            // Open up the arena
            challenge.getArena().setInUse(false);
            // Broadcast the winner
            challengeHandler.announceWinner(challenge, guilds.getCommandManager());
            // Move rest of players out of arena
            challengeHandler.teleportRemaining(challenge);
            // Run the reward commands
            challengeHandler.giveRewards(settingsManager, challenge);
            try {
                // Save the details about the challenge
                challengesProvider.saveChallenge(challenge);
                challengeHandler.removeChallenge(challenge);
            } catch (IOException e) {
                e.printStackTrace();
                challengeHandler.removeChallenge(challenge);
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

        // Check to make sure they have a challenge
        GuildChallenge challenge = challengeHandler.getChallenge(entity);

        // Make sure it's not null
        if (challenge != null) {
            if (challenge.isStarted()) {
                // Cancel the last damage so they don't die
                event.setCancelled(true);
                // Teleport them out of the arena
                challengeHandler.exitArena(entity, challenge);
                // Remove them
                handleExist(entity, challenge);
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        // Check if commands are blocked during arena
        if (settingsManager.getProperty(WarSettings.DISABLE_COMMANDS)) {
            // See if player has challenge
            GuildChallenge challenge = challengeHandler.getChallenge(player);
            // If they have one, continue
            if (challenge != null) {
                // Cancel the command
                event.setCancelled(true);
                // Tell them they can't run commands while in war
                guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.WAR__COMMANDS_BLOCKED);
            }

        }
    }

}
