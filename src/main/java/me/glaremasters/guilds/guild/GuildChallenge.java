package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class GuildChallenge {

    private UUID id;
    private long initiateTime;
    private UUID challenger;
    private UUID defender;
    private boolean accepted;

}
