package me.glaremasters.guilds.guild;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildMember {

    private UUID uuid;
    private int role;

    public GuildMember(UUID uuid, int role) {
        this.uuid = uuid;
        this.role = role;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getRole() {
        return role;
    }

    // Void for setting role
}
