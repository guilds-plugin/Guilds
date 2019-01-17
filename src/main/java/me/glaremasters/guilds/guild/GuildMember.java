package me.glaremasters.guilds.guild;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildMember {

    private UUID uuid;
    private GuildRole role;

    /**
     * GuildMember object this is a player in a guild
     * @param uuid the player's uuid
     * @param role the player's role in this guild
     */
    public GuildMember(UUID uuid, GuildRole role) {
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
    public GuildRole getRole() {
        return role;
    }

    /**
     * This method sets the Role of a GuildMember
     * @param role the Role to set the GuildMember
     */
    public void setRole(GuildRole role) {
        this.role = role;
    }
}