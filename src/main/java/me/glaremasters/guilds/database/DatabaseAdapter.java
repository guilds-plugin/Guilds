package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.guild.GuildAdapter;

public class DatabaseAdapter {
    private final DatabaseBackend backend;
    private final GuildAdapter guildAdapter;
    private DatabaseManager databaseManager;
    private String sqlTablePrefix;

    public DatabaseAdapter(Guilds guilds, SettingsManager settings) {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();
        DatabaseBackend backend = DatabaseBackend.getByBackendName(backendName);

        if (backend == null) {
            backend = DatabaseBackend.JSON;
        }

        this.backend = backend;

        if (backend != DatabaseBackend.JSON) {
            databaseManager = new DatabaseManager(settings);
            sqlTablePrefix = settings.getProperty(StorageSettings.SQL_TABLE_PREFIX).toLowerCase();
        }

        guildAdapter = new GuildAdapter(guilds, this);
    }

    public DatabaseBackend getBackend() {
        return backend;
    }

    public GuildAdapter getGuildAdapter() {
        return guildAdapter;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public String getSqlTablePrefix() {
        return sqlTablePrefix;
    }
}
