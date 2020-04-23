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

package me.glaremasters.guilds.challenges;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRolePerm;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Glare
 * Date: 7/12/2019
 * Time: 2:50 PM
 */
public class ChallengeHandler {

    private List<GuildChallenge> challenges;
    private final Guilds guilds;

    public ChallengeHandler(Guilds guilds) {
        this.guilds = guilds;

        Guilds.newChain().async(() -> {
            try {
                challenges = guilds.getDatabase().getChallengeAdapter().getAllChallenges();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).sync(() -> challenges.forEach(c -> {
            if (!c.isCompleted()) {
                c.setCompleted(true);
            }
        })).execute();
    }

    /**
     * Save the data of challenges
     * @throws IOException
     */
    public void saveData() throws IOException {
        guilds.getDatabase().getChallengeAdapter().saveChallenges(challenges);
    }

    /**
     * Create a new Guild Challenge
     * @param challenger the challenging guild
     * @param defender the defending guild
     * @param minPlayer the min amount of players
     * @param maxPlayers the amount of players
     * @return new challenge
     */
    public GuildChallenge createNewChallenge(Guild challenger, Guild defender, int minPlayer, int maxPlayers, Arena arena) {
        return new GuildChallenge(UUID.randomUUID(), System.currentTimeMillis(), challenger,
                defender, false, false,
                false, false, minPlayer, maxPlayers,
                new ArrayList<>(), new ArrayList<>(), arena,
                null, null, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    /**
     * Add a challenge to the list
     * @param challenge challenge
     */
    public void addChallenge(@NotNull GuildChallenge challenge) {
        challenges.add(challenge);
    }

    /**
     * Remove a challenge from the list
     * @param challenge challenge
     */
    public void removeChallenge(@NotNull GuildChallenge challenge) {
        challenges.remove(challenge);
    }

    /**
     * Get a challenge by it's uuid
     * @param uuid the uuid of the challenge
     * @return the challenge
     */
    public GuildChallenge getChallenge(@NotNull UUID uuid) {
        return challenges.stream().filter(c -> c.getId() == uuid).findAny().orElse(null);
    }

    /**
     * Get a guild challenge by a guild
     * @param guild the guild to check
     * @return the challenge
     */
    public GuildChallenge getChallenge(@NotNull Guild guild) {
        return challenges.stream().filter(c -> (c.getChallenger() == guild || c.getDefender() == guild) && !c.isCompleted()).findFirst().orElse(null);
    }

    /**
     * Get a challenge from a player
     * @param player the player to check
     * @return the challenge they are part of
     */
    public GuildChallenge getChallenge(@NotNull Player player) {
        return getActiveChallenges().stream()
                .filter(c -> (c.getAliveChallengers().containsKey(player.getUniqueId()) || c.getAliveDefenders().containsKey(player.getUniqueId())) && !c.isCompleted())
                .findAny().orElse(null);
    }

    /**
     * Get a list of active challenges
     * @return active challenges
     */
    public List<GuildChallenge> getActiveChallenges() {
        return challenges.stream().filter(GuildChallenge::isStarted).collect(Collectors.toList());
    }

    /**
     * Get a list of the online war people for your guild
     * @param guild the guild to check
     * @return list of online war people
     */
    public List<Player> getOnlineDefenders(Guild guild) {
        List<GuildMember> members = guild.getOnlineMembers().stream().filter(m -> m.getRole().hasPerm(GuildRolePerm.INITIATE_WAR)).collect(Collectors.toList());
        return members.stream().map(m -> Bukkit.getPlayer(m.getUuid())).collect(Collectors.toList());
    }

    /**
     * Send a message to all online defenders
     * @param guild the guild defending
     * @param commandManager the command manager
     * @param challenger the guild challenging
     * @param acceptTime
     */
    public void pingOnlineDefenders(Guild guild, CommandManager commandManager, String challenger, int acceptTime) {
        getOnlineDefenders(guild).forEach(m -> commandManager.getCommandIssuer(m).sendInfo(Messages.WAR__INCOMING_CHALLENGE, "{guild}", challenger, "{amount}", String.valueOf(acceptTime)));
    }

    /**
     * Simple method to check if both guilds have enough players online
     * @param challenger challenging guild
     * @param defender defending guild
     * @param amount amount to check
     * @return enough players online
     */
    public boolean checkEnoughOnline(Guild challenger, Guild defender, int amount) {
        return challenger.getOnlineAsPlayers().size() >= amount && defender.getOnlineAsPlayers().size() >= amount;
    }

    /**
     * Make sure enough players joined the challenge
     * @param challenge the challenge to check
     * @return enough joined or not
     */
    public boolean checkEnoughJoined(GuildChallenge challenge) {
        return challenge.getChallengePlayers().size() >= challenge.getMinPlayersPerSide()
                && challenge.getDefendPlayers().size() >= challenge.getMinPlayersPerSide();
    }

    /**
     * Prepare the final list for a challenge
     * @param players the players in the challengeGi
     * @param challenge the challenge this is for
     * @param team the team they are on
     */
    public void prepareFinalList(List<UUID> players, GuildChallenge challenge, String team) {
        LinkedHashMap<UUID, String> finalList = new LinkedHashMap<>();
        players.forEach(p -> {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                finalList.putIfAbsent(p, ACFBukkitUtil.fullLocationToString(player.getLocation()));
            }
        });
        if (team.equalsIgnoreCase("challenger")) {
            challenge.setAliveChallengers(finalList);
        } else {
            challenge.setAliveDefenders(finalList);
        }
    }

    /**
     * Send players to arena
     * @param players the players to send
     * @param location the location to send them to
     */
    public void sendToArena(Map<UUID, String> players, Location location) {
        players.keySet().forEach(p -> {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                player.teleport(location);
            }
        });
    }

    /**
     * Teleport a player out of the arena
     * @param player the player to teleport
     * @param challenge the challenge they are part of
     */
    public void exitArena(Player player, GuildChallenge challenge, Guilds guilds) {
        String location = getAllPlayersAlive(challenge).get(player.getUniqueId());
        if (location != null) {
            player.teleport(ACFBukkitUtil.stringToLocation(location));
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.WAR__TELEPORTED_BACK);
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * Get a combined map of all players
     * @param challenge the challenge to get alive left of
     * @return compiled map
     */
    public Map<UUID, String> getAllPlayersAlive(GuildChallenge challenge) {
        return Stream.of(challenge.getAliveChallengers(), challenge.getAliveDefenders()).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Teleport the remaining players back to their original location
     * @param challenge the challenge to check
     */
    public void teleportRemaining(GuildChallenge challenge) {
        getAllPlayersAlive(challenge).forEach((key, value) -> Bukkit.getPlayer(key).teleport(ACFBukkitUtil.stringToLocation(value)));
    }

    /**
     * Remove a player from a challenge
     * @param player player to remove
     */
    public void removePlayer(Player player) {
        GuildChallenge c = getChallenge(player);
        if (c != null) {
            c.getAliveDefenders().remove(player.getUniqueId());
            c.getAliveChallengers().remove(player.getUniqueId());
        }
    }

    /**
     * Check if a challenge is over because a team won
     * @param challenge the challenge to check
     * @return if it's over or not
     */
    public boolean checkIfOver(GuildChallenge challenge) {
        if (challenge.getAliveChallengers().keySet().size() == 0) {
            challenge.setWinner(challenge.getDefender());
            challenge.setLoser(challenge.getChallenger());
            challenge.getDefender().getGuildScore().addWin();
            challenge.getChallenger().getGuildScore().addLoss();
            return true;
        }
        if (challenge.getAliveDefenders().keySet().size() == 0) {
            challenge.setWinner(challenge.getChallenger());
            challenge.setLoser(challenge.getDefender());
            challenge.getDefender().getGuildScore().addLoss();
            challenge.getChallenger().getGuildScore().addWin();
            return true;
        }
        return false;
    }

    /**
     * Announce the winner to the guild
     * @param challenge the challenge to check
     * @param commandManager the command manager
     */
    public void announceWinner(GuildChallenge challenge, CommandManager commandManager) {
        challenge.getDefender().sendMessage(commandManager, Messages.WAR__WINNER,
                "{guild}", challenge.getWinner().getName());
        challenge.getChallenger().sendMessage(commandManager, Messages.WAR__WINNER,
                "{guild}", challenge.getWinner().getName());
    }

    /**
     * Announce when a player dies
     * @param challenge the challenge to look at
     * @param guilds main class
     * @param player the player that died
     * @param killer possible killer
     * @param cause the reason they died
     */
    public void announceDeath(GuildChallenge challenge, Guilds guilds, Player player, Player killer, Cause cause) {
        Messages message;
        switch (cause.toString()) {
            case "PLAYER_KILLED_PLAYER":
                message = Messages.WAR__PLAYER_KILLED_PLAYER;
                break;
            case "PLAYER_KILLED_UNKNOWN":
                message = Messages.WAR__PLAYER_KILLED_OTHER;
                break;
            case "PLAYER_KILLED_QUIT":
                message = Messages.WAR__PLAYER_KILLED_QUIT;
                break;
            default:
                message = Messages.WAR__PLAYER_KILLED_OTHER;
                break;
        }
        getAllPlayersAlive(challenge).keySet().forEach(p -> guilds.getCommandManager().getCommandIssuer(Bukkit.getPlayer(p))
                .sendInfo(message, "{player}", player.getName(), "{killer}", killer.getName()));
    }

    /**
     * Give the rewards to the winner
     * @param settingsManager the settings manager
     * @param challenge the challenge
     */
    public void giveRewards(SettingsManager settingsManager, GuildChallenge challenge) {
        List<UUID> winners;
        UUID teamWinner = challenge.getWinner().getId();
        if (teamWinner == challenge.getChallenger().getId()) {
            winners = challenge.getChallengePlayers();
        } else {
            winners = challenge.getDefendPlayers();
        }
        List<String> commands = settingsManager.getProperty(WarSettings.WAR_REWARDS);
        if (settingsManager.getProperty(WarSettings.WAR_REWARDS_ENABLED)) {
            winners.forEach(p -> {
                Player player = Bukkit.getPlayer(p);
                if (player != null) {
                    commands.forEach(c -> {
                        c = c.replace("{player}", player.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), c);
                    });
                }
            });
        }
    }

    /**
     * Check to see if a Guild can be challenged
     * @param guild the guild to check
     * @param settingsManager the settings manager
     * @return if they can be challenge or not
     */
    public boolean notOnCooldown(Guild guild, SettingsManager settingsManager) {
        long cooldownTime = TimeUnit.MINUTES.toMillis(settingsManager.getProperty(WarSettings.DEFEND_COOLDOWN));
        long lastDefended = guild.getLastDefended();
        long currentTime = System.currentTimeMillis();

        if (lastDefended == 0) {
            return true;
        }

        return (currentTime - lastDefended > cooldownTime);
    }

    /**
     * Handle finishing of the arena war
     * @param guilds guilds instance
     * @param settingsManager the settings manager
     * @param player player to remove
     * @param challenge the challenge being checked
     */
    public void handleFinish(Guilds guilds, SettingsManager settingsManager, Player player, GuildChallenge challenge) {
        removePlayer(player);
        if (checkIfOver(challenge)) {
            // Specify the war is over
            challenge.setStarted(false);
            challenge.setCompleted(true);
            // Open up the arena
            challenge.getArena().setInUse(false);
            // Broadcast the winner
            announceWinner(challenge, guilds.getCommandManager());
            // Move rest of players out of arena
            teleportRemaining(challenge);
            // Run the reward commands
            giveRewards(settingsManager, challenge);
            // Execute post war commands
            if (settingsManager.getProperty(WarSettings.ENABLE_POST_CHALLENGE_COMMANDS)) {
                settingsManager.getProperty(WarSettings.POST_CHALLENGE_COMMANDS).forEach(c -> {
                        c = c.replace("{challenger}", challenge.getChallenger().getName());
                        c = c.replace("{defender}", challenge.getDefender().getName());
                        c = c.replace("{winner}", challenge.getWinner().getName());
                        c = c.replace("{loser}", challenge.getLoser().getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), c);
                });
            }
            try {
                // Save the details about the challenge
               saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public enum Cause {
        PLAYER_KILLED_PLAYER,
        PLAYER_KILLED_UNKNOWN,
        PLAYER_KILLED_QUIT
    }


    public List<GuildChallenge> getChallenges() {
        return this.challenges;
    }
}
