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

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.CommandManager;
import lombok.Getter;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 7/12/2019
 * Time: 2:50 PM
 */
public class ChallengeHandler {

    @Getter private List<GuildChallenge> challenges;

    public ChallengeHandler() {
        this.challenges = new ArrayList<>();
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
                minPlayer, maxPlayers, new ArrayList<>(), new ArrayList<>(),
                arena);
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
        return challenges.stream().filter(c -> c.getChallenger() == guild || c.getDefender() == guild).findFirst().orElse(null);
    }

    /**
     * Get a list of the online war people for your guild
     * @param guild the guild to check
     * @return list of online war people
     */
    public List<Player> getOnlineDefenders(Guild guild) {
        List<GuildMember> members = guild.getOnlineMembers().stream().filter(m -> m.getRole().isInitiateWar()).collect(Collectors.toList());
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
     * Teleport challengers to the arena
     * @param players players to teleport
     * @param arena arena to teleport to
     */
    public void teleportChallenger(List<UUID> players, Arena arena) {
        Location loc = ACFBukkitUtil.stringToLocation(arena.getChallenger());
        players.forEach(p -> {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                player.teleport(loc);
            }
        });
    }

    /**
     * Teleport defenders to the arena
     * @param players players to teleport
     * @param arena arena to teleport to
     */
    public void teleportDefender(List<UUID> players, Arena arena) {
        Location loc = ACFBukkitUtil.stringToLocation(arena.getDefender());
        players.forEach(p -> {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                player.teleport(loc);
            }
        });
    }

}
