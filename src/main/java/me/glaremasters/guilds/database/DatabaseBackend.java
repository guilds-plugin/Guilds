package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.database.guild.provider.GuildMySQLProvider;

import java.util.Arrays;

public enum DatabaseBackend {
    JSON("json", GuildJsonProvider.class),
    MYSQL("mysql", GuildMySQLProvider.class);

    private final String backendName;
    private final Class<? extends GuildProvider> guildProvider;

    DatabaseBackend(String backendName, Class<? extends GuildProvider> guildProvider) {
        this.backendName = backendName;
        this.guildProvider = guildProvider;
    }

    public String getBackendName() {
        return backendName;
    }

    public Class<? extends GuildProvider> getGuildProvider() {
        return guildProvider;
    }

    public static DatabaseBackend getByBackendName(String backendName) {
        return Arrays.stream(values()).filter(n -> n.backendName.equals(backendName.toLowerCase())).findFirst().orElse(null);
    }
}
