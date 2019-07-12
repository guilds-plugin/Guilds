package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.glaremasters.guilds.arena.Arena;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class GuildChallenge {

    private UUID id;
    private long initiateTime;
    private Guild challenger;
    private Guild defender;
    private boolean accepted;
    private boolean joinble;
    private int minPlayersPerSide;
    private int maxPlayersPerSide;
    private List<UUID> challengePlayers;
    private List<UUID> defendPlayers;
    private Arena arena;

}
