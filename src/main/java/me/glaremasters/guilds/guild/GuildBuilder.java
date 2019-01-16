package me.glaremasters.guilds.guild;

import lombok.Builder;

import java.util.UUID;

@Builder
public class GuildBuilder {
    private String name;
    private String prefix;
    private String status;
    private String texture;
    private UUID master;

    /**
     * Create the guild
     * @return created guild
     */
    public Guild createGuild() {
        return new Guild(name, prefix, status, texture, master);
    }
}