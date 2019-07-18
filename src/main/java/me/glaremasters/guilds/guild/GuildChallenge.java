package me.glaremasters.guilds.guild;

import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.challenges.adapters.WarArenaChallengeAdapter;
import me.glaremasters.guilds.challenges.adapters.WarGuildChallengeAdapater;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class GuildChallenge {

    private UUID id;
    private long initiateTime;
    @JsonAdapter(WarGuildChallengeAdapater.class)
    private Guild challenger;
    @JsonAdapter(WarGuildChallengeAdapater.class)
    private Guild defender;
    private transient boolean accepted;
    private transient boolean joinble;
    private transient boolean started;
    private transient int minPlayersPerSide;
    private transient int maxPlayersPerSide;
    private List<UUID> challengePlayers;
    private List<UUID> defendPlayers;
    @JsonAdapter(WarArenaChallengeAdapter.class)
    private Arena arena;
    private String winner;

    private transient Map<UUID, String> aliveChallengers;
    private transient Map<UUID, String> aliveDefenders;
}