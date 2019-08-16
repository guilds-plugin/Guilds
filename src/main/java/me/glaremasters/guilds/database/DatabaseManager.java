package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class DatabaseManager {
    private Jdbi jdbi = null;
    private HikariDataSource hikari = null;

    public DatabaseManager(SettingsManager settingsManager, DatabaseBackend backend) {
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        config.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        config.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        config.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

        String databaseName = settingsManager.getProperty(StorageSettings.SQL_DATABASE);
        switch (backend) {
            case MYSQL:
                config.setPoolName("Guilds MySQL Connection Pool");
                config.setDataSourceClassName(settingsManager.getProperty(StorageSettings.DATASOURCE));
                config.addDataSourceProperty("serverName", settingsManager.getProperty(StorageSettings.SQL_HOST));
                config.addDataSourceProperty("port", settingsManager.getProperty(StorageSettings.SQL_PORT));
                config.addDataSourceProperty("databaseName", databaseName);
                config.addDataSourceProperty("user", settingsManager.getProperty(StorageSettings.SQL_USERNAME));
                config.addDataSourceProperty("password", settingsManager.getProperty(StorageSettings.SQL_PASSWORD));
                config.addDataSourceProperty("useSSL", settingsManager.getProperty(StorageSettings.SQL_ENABLE_SSL));
                break;
            case SQLITE:
                config.setPoolName("Guilds SQLite Connection Pool");
                config.setJdbcUrl(String.format("jdbc:sqlite:plugins/Guilds/%s.db", databaseName));
                break;
            default:
                throw new IllegalArgumentException("Invalid backend for DatabaseManager setup: " + backend.getBackendName());
        }

        HikariDataSource hikari;
        try {
            hikari = new HikariDataSource(config);
        } catch (Exception ex) {
            // TODO: send that the database connection has failed; probably must check 1) their host or 2) the settings
            return;
        }

        jdbi = Jdbi.create(hikari);
        jdbi.installPlugin(new SqlObjectPlugin());

        this.hikari = hikari;
    }

    public final boolean isConnected() {
        return hikari != null && hikari.isRunning();
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public HikariDataSource getHikari() {
        return hikari;
    }
}
