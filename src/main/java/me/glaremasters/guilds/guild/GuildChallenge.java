package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.glaremasters.guilds.arena.Arena;
import org.bukkit.entity.Player;

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
    private transient List<Player> challengingPlayers;
    private transient List<Player> defendingPlayers;
    private List<UUID> challengingPlayersUUID;
    private List<UUID> defendingPlayersUUID;
    private Arena arena;

}
