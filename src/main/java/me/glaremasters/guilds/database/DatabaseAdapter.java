package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.guild.GuildAdapter;

public final class DatabaseAdapter implements AutoCloseable {
    private final Guilds guilds;
    private final SettingsManager settings;
    private DatabaseBackend backend;
    private GuildAdapter guildAdapter;
    private DatabaseManager databaseManager;
    private String sqlTablePrefix;

    public DatabaseAdapter(Guilds guilds, SettingsManager settings) {
        this(guilds, settings, true);
    }

    public DatabaseAdapter(Guilds guilds, SettingsManager settings, boolean doConnect) {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();
        DatabaseBackend backend = DatabaseBackend.getByBackendName(backendName);

        if (backend == null) {
            backend = DatabaseBackend.JSON;
        }

        if (doConnect) {
            setUpBackend(settings, backend);
        }

        this.guilds = guilds;
        this.settings = settings;
    }

    public boolean isConnected() {
        return getBackend() == DatabaseBackend.JSON ||
                (databaseManager != null && databaseManager.isConnected());
    }

    public void open() {
        if (databaseManager != null && !databaseManager.isConnected()) {
            databaseManager = new DatabaseManager(settings);
        }
    }

    @Override
    public void close() {
        if (databaseManager != null && databaseManager.isConnected()) {
            // TODO: do you want to save the guilds here?
            databaseManager.getHikari().close();
        }
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

    public DatabaseAdapter cloneWith(DatabaseBackend backend) throws IllegalArgumentException {
        if (this.backend.equals(backend)) {
            throw new IllegalArgumentException("Given backend matches current backend. Use this backend.");
        }

        DatabaseAdapter cloned = new DatabaseAdapter(this.guilds, this.settings, false);
        cloned.setUpBackend(this.settings, backend);
        return cloned;
    }

    private void setUpBackend(SettingsManager settings, DatabaseBackend backend) {
        if (isConnected()) return;

        if (backend != DatabaseBackend.JSON) {
            this.databaseManager = new DatabaseManager(settings);
            this.sqlTablePrefix = settings.getProperty(StorageSettings.SQL_TABLE_PREFIX).toLowerCase();
        }

        this.guildAdapter = new GuildAdapter(guilds, this);
        this.backend = backend;
    }
}
