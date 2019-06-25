package me.glaremasters.guilds.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class Arena {

    private UUID id;
    private String name;
    private String challenger;
    private String defender;

}
