package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class DatabaseManager {
    private final HikariDataSource hikari;
    private final Jdbi jdbi;

    public DatabaseManager(SettingsManager settingsManager) {
        // Create the Hikari DS
        hikari = new HikariDataSource();
        // Set the pool name
        hikari.setPoolName("Guilds Connection Pool");
        // Set the datasource
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", settingsManager.getProperty(StorageSettings.SQL_HOST));
        hikari.addDataSourceProperty("port", settingsManager.getProperty(StorageSettings.SQL_PORT));
        hikari.addDataSourceProperty("databaseName", settingsManager.getProperty(StorageSettings.SQL_DATABASE));
        hikari.addDataSourceProperty("user", settingsManager.getProperty(StorageSettings.SQL_USERNAME));
        hikari.addDataSourceProperty("password", settingsManager.getProperty(StorageSettings.SQL_PASSWORD));
        hikari.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        hikari.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        hikari.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        hikari.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

        jdbi = Jdbi.create(hikari);
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public HikariDataSource getHikari() {
        return hikari;
    }
}
