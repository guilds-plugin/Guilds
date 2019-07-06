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

    // The UUID of the challenge, for keeping track of
    private UUID id;
    // The time the challenge was send
    private long initiateTime;
    // The challenging guild
    private Guild challenger;
    // The defending guild
    private Guild defender;
    // If it was accepted or not
    private boolean accepted;
    // If players can /guild war join
    private boolean joinble;
    // The min players on each time
    private int minPlayersPerSide;
    // The max players on each team
    private int maxPlayersPerSide;
    // The list of players joined the challenging team
    private transient List<Player> challengingPlayers;
    // List of players joined defending team
    private transient List<Player> defendingPlayers;
    // UUID of players on challenging side
    private List<UUID> challengingPlayersUUID;
    // UUID of players on defending team
    private List<UUID> defendingPlayersUUID;
    // The arena to be used
    private Arena arena;

}
