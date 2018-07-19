package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildMember {

    @Expose
    private UUID uuid;

    @Expose
    private int role;

    public GuildMember(UUID uuid, int role) {
        this.uuid = uuid;
        this.role = role;
    }

    /**
     * This method gets the UUID of a GuildMember
     * @return the UUID of the GuildMember
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * This method gets the Role of a GuildMember
     * @return the Role of the GuildMember
     */
    public int getRole() {
        return role;
    }

    /**
     * This method sets the Role of a GuildMember
     * @param role the Role to set the GuildMember
     */
    public void setRole(GuildRole role) {
        this.role = role.getLevel();
    }
}