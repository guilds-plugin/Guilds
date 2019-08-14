package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.providers.DatabaseProvider;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.database.providers.MySQLProvider;

import java.util.Arrays;

public enum DatabaseBackend {
    JSON("json", JsonProvider.class),
    MYSQL("mysql", MySQLProvider.class);

    private final String backendName;
    private final Class<? extends DatabaseProvider> provider;

    DatabaseBackend(String backendName, Class<? extends DatabaseProvider> provider) {
        this.backendName = backendName;
        this.provider = provider;
    }

    public String getBackendName() {
        return backendName;
    }

    public Class<? extends DatabaseProvider> getProvider() {
        return provider;
    }

    public static DatabaseBackend getByBackendName(String backendName) {
        return Arrays.stream(values()).filter(n -> n.backendName.toLowerCase().equals(backendName)).findFirst().orElse(null);
    }
}
