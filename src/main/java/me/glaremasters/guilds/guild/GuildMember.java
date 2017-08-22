package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;

import java.util.UUID;

public class GuildMember {

    @Expose
    private UUID uuid;

    @Expose
    private int role;

    public GuildMember(UUID uuid, int role) {
        this.uuid = uuid;
        this.role = role;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getRole() {
        return role;
    }

    public void setRole(GuildRole role) {
        this.role = role.getLevel();
    }
}
