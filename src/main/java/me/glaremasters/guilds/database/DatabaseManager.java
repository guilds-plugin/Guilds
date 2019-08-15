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

    public DatabaseManager(SettingsManager settingsManager) {
        HikariConfig config = new HikariConfig();
        // Set the pool name
        config.setPoolName("Guilds Connection Pool");
        // Set the datasource
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", settingsManager.getProperty(StorageSettings.SQL_HOST));
        config.addDataSourceProperty("port", settingsManager.getProperty(StorageSettings.SQL_PORT));
        config.addDataSourceProperty("databaseName", settingsManager.getProperty(StorageSettings.SQL_DATABASE));
        config.addDataSourceProperty("user", settingsManager.getProperty(StorageSettings.SQL_USERNAME));
        config.addDataSourceProperty("password", settingsManager.getProperty(StorageSettings.SQL_PASSWORD));
        config.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        config.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        config.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        config.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

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
