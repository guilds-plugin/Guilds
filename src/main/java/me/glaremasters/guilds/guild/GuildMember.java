package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@AllArgsConstructor
@Getter
public class GuildMember {

    private UUID uuid;
    @Setter private GuildRole role;
}