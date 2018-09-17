package me.glaremasters.guilds.guild;

import java.util.UUID;

public class GuildBuilder {
    private String name;
    private String prefix;
    private String status;
    private UUID master;

    public GuildBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public GuildBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public GuildBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public GuildBuilder setMaster(UUID master) {
        this.master = master;
        return this;
    }

    public Guild createGuild() {
        return new Guild(name, prefix, status, master);
    }
}