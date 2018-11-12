package me.glaremasters.guilds.guild;

import java.util.UUID;

public class GuildBuilder {
    private String name;
    private String prefix;
    private String status;
    private UUID master;

    /**
     * Set the name of the guild
     * @param name
     * @return
     */
    public GuildBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the prefix of a guild
     * @param prefix
     * @return
     */
    public GuildBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Set the status of a guild
     * @param status
     * @return
     */
    public GuildBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Set the master of a guild
     * @param master
     * @return
     */
    public GuildBuilder setMaster(UUID master) {
        this.master = master;
        return this;
    }

    /**
     * Create the guild
     * @return created guild
     */
    public Guild createGuild() {
        return new Guild(name, prefix, status, master);
    }
}