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
package me.glaremasters.guilds.guild;

import com.google.gson.annotations.JsonAdapter;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.challenges.adapters.WarArenaChallengeAdapter;
import me.glaremasters.guilds.challenges.adapters.WarGuildChallengeAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class GuildChallenge {

    private UUID id;
    private long initiateTime;
    @JsonAdapter(WarGuildChallengeAdapter.class)
    private Guild challenger;
    @JsonAdapter(WarGuildChallengeAdapter.class)
    private Guild defender;
    private transient boolean accepted;
    private transient boolean joinble;
    private transient boolean started;
    private boolean completed;
    private transient int minPlayersPerSide;
    private transient int maxPlayersPerSide;
    private List<UUID> challengePlayers;
    private List<UUID> defendPlayers;
    @JsonAdapter(WarArenaChallengeAdapter.class)
    private Arena arena;
    @JsonAdapter(WarGuildChallengeAdapter.class)
    private Guild winner;
    @JsonAdapter(WarGuildChallengeAdapter.class)
    private Guild loser;
    private transient LinkedHashMap<UUID, String> aliveChallengers;
    private transient LinkedHashMap<UUID, String> aliveDefenders;

    public GuildChallenge(UUID id, long initiateTime, Guild challenger, Guild defender, boolean accepted, boolean joinble, boolean started, boolean completed, int minPlayersPerSide, int maxPlayersPerSide, List<UUID> challengePlayers, List<UUID> defendPlayers, Arena arena, Guild winner, Guild loser, LinkedHashMap<UUID, String> aliveChallengers, LinkedHashMap<UUID, String> aliveDefenders) {
        this.id = id;
        this.initiateTime = initiateTime;
        this.challenger = challenger;
        this.defender = defender;
        this.accepted = accepted;
        this.joinble = joinble;
        this.started = started;
        this.completed = completed;
        this.minPlayersPerSide = minPlayersPerSide;
        this.maxPlayersPerSide = maxPlayersPerSide;
        this.challengePlayers = challengePlayers;
        this.defendPlayers = defendPlayers;
        this.arena = arena;
        this.winner = winner;
        this.loser = loser;
        this.aliveChallengers = aliveChallengers;
        this.aliveDefenders = aliveDefenders;
    }

    public static GuildChallengeBuilder builder() {
        return new GuildChallengeBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public long getInitiateTime() {
        return this.initiateTime;
    }

    public Guild getChallenger() {
        return this.challenger;
    }

    public Guild getDefender() {
        return this.defender;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public boolean isJoinble() {
        return this.joinble;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getMinPlayersPerSide() {
        return this.minPlayersPerSide;
    }

    public int getMaxPlayersPerSide() {
        return this.maxPlayersPerSide;
    }

    public List<UUID> getChallengePlayers() {
        return this.challengePlayers;
    }

    public List<UUID> getDefendPlayers() {
        return this.defendPlayers;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Guild getWinner() {
        return this.winner;
    }


    public void setId(UUID id) {
        this.id = id;
    }

    public void setInitiateTime(long initiateTime) {
        this.initiateTime = initiateTime;
    }

    public void setChallenger(Guild challenger) {
        this.challenger = challenger;
    }

    public void setDefender(Guild defender) {
        this.defender = defender;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setJoinble(boolean joinble) {
        this.joinble = joinble;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setMinPlayersPerSide(int minPlayersPerSide) {
        this.minPlayersPerSide = minPlayersPerSide;
    }

    public void setMaxPlayersPerSide(int maxPlayersPerSide) {
        this.maxPlayersPerSide = maxPlayersPerSide;
    }

    public void setChallengePlayers(List<UUID> challengePlayers) {
        this.challengePlayers = challengePlayers;
    }

    public void setDefendPlayers(List<UUID> defendPlayers) {
        this.defendPlayers = defendPlayers;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public void setWinner(Guild winner) {
        this.winner = winner;
    }

    public Guild getLoser() {
        return loser;
    }

    public void setLoser(Guild loser) {
        this.loser = loser;
    }


    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LinkedHashMap<UUID, String> getAliveChallengers() {
        return aliveChallengers;
    }

    public LinkedHashMap<UUID, String> getAliveDefenders() {
        return aliveDefenders;
    }

    public void setAliveChallengers(LinkedHashMap<UUID, String> aliveChallengers) {
        this.aliveChallengers = aliveChallengers;
    }

    public void setAliveDefenders(LinkedHashMap<UUID, String> aliveDefenders) {
        this.aliveDefenders = aliveDefenders;
    }


    public static class GuildChallengeBuilder {
        private UUID id;
        private long initiateTime;
        private Guild challenger;
        private Guild defender;
        private boolean accepted;
        private boolean joinble;
        private boolean started;
        private boolean completed;
        private int minPlayersPerSide;
        private int maxPlayersPerSide;
        private List<UUID> challengePlayers;
        private List<UUID> defendPlayers;
        private Arena arena;
        private Guild winner;
        private Guild loser;
        private LinkedHashMap<UUID, String> aliveChallengers;
        private LinkedHashMap<UUID, String> aliveDefenders;

        GuildChallengeBuilder() {
        }

        public GuildChallenge.GuildChallengeBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder initiateTime(long initiateTime) {
            this.initiateTime = initiateTime;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder challenger(Guild challenger) {
            this.challenger = challenger;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder defender(Guild defender) {
            this.defender = defender;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder accepted(boolean accepted) {
            this.accepted = accepted;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder joinble(boolean joinble) {
            this.joinble = joinble;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder started(boolean started) {
            this.started = started;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder minPlayersPerSide(int minPlayersPerSide) {
            this.minPlayersPerSide = minPlayersPerSide;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder maxPlayersPerSide(int maxPlayersPerSide) {
            this.maxPlayersPerSide = maxPlayersPerSide;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder challengePlayers(List<UUID> challengePlayers) {
            this.challengePlayers = challengePlayers;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder defendPlayers(List<UUID> defendPlayers) {
            this.defendPlayers = defendPlayers;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder arena(Arena arena) {
            this.arena = arena;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder winner(Guild winner) {
            this.winner = winner;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder loser(Guild loser) {
            this.loser = loser;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder aliveChallengers(LinkedHashMap<UUID, String> aliveChallengers) {
            this.aliveChallengers = aliveChallengers;
            return this;
        }

        public GuildChallenge.GuildChallengeBuilder aliveDefenders(LinkedHashMap<UUID, String> aliveDefenders) {
            this.aliveDefenders = aliveDefenders;
            return this;
        }

        public GuildChallenge build() {
            return new GuildChallenge(id, initiateTime, challenger, defender, accepted, joinble, started, completed, minPlayersPerSide, maxPlayersPerSide, challengePlayers, defendPlayers, arena, winner, loser, aliveChallengers, aliveDefenders);
        }

        public String toString() {
            return "GuildChallenge.GuildChallengeBuilder(id=" + this.id + ", initiateTime=" + this.initiateTime + ", challenger=" + this.challenger + ", defender=" + this.defender + ", accepted=" + this.accepted + ", joinble=" + this.joinble + ", started=" + this.started + ", minPlayersPerSide=" + this.minPlayersPerSide + ", maxPlayersPerSide=" + this.maxPlayersPerSide + ", challengePlayers=" + this.challengePlayers + ", defendPlayers=" + this.defendPlayers + ", arena=" + this.arena + ", winner=" + this.winner + ", loser=" + this.loser + ", aliveChallengers=" + this.aliveChallengers + ", aliveDefenders=" + this.aliveDefenders + ")";
        }
    }
}