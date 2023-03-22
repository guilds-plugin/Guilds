/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * A class for managing the database connection using JDBI and HikariDataSource.
 */
public class DatabaseManager {
    private Jdbi jdbi;
    private HikariDataSource hikari;
    private String dataSourceName;

    /**
     * Constructs a DatabaseManager object and sets up a database connection using the given settings manager and backend.
     *
     * @param settingsManager the settings manager that provides the database connection information
     * @param backend         the type of database backend to use
     */
    public DatabaseManager(SettingsManager settingsManager, DatabaseBackend backend) {
        // Create a new HikariConfig object
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        config.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        config.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        config.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

        String databaseName = settingsManager.getProperty(StorageSettings.SQL_DATABASE);
        switch (backend) {
            case MYSQL:
                config.setPoolName("Guilds MySQL Connection Pool");
                if (dataSourceName == null) {
                    tryDataSourceName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                }
                if (dataSourceName == null) {
                    tryDataSourceName("com.mysql.cj.jdbc.MysqlDataSource");
                }
                config.setDataSourceClassName(dataSourceName);
                config.addDataSourceProperty("serverName", settingsManager.getProperty(StorageSettings.SQL_HOST));
                config.addDataSourceProperty("port", settingsManager.getProperty(StorageSettings.SQL_PORT));
                config.addDataSourceProperty("databaseName", databaseName);
                config.addDataSourceProperty("user", settingsManager.getProperty(StorageSettings.SQL_USERNAME));
                config.addDataSourceProperty("password", settingsManager.getProperty(StorageSettings.SQL_PASSWORD));
                config.addDataSourceProperty("useSSL", settingsManager.getProperty(StorageSettings.SQL_ENABLE_SSL));

                if (settingsManager.getProperty(StorageSettings.UTF8)) {
                    config.addDataSourceProperty("characterEncoding", "utf8");
                }
                break;
            case SQLITE:
                config.setPoolName("Guilds SQLite Connection Pool");
                config.setJdbcUrl("jdbc:sqlite:plugins/Guilds/guilds.db");
                break;
            case MARIADB:
                config.setPoolName("Guilds MariaDB Connection Pool");
                config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
                config.addDataSourceProperty("serverName", settingsManager.getProperty(StorageSettings.SQL_HOST));
                config.addDataSourceProperty("port", settingsManager.getProperty(StorageSettings.SQL_PORT));
                config.addDataSourceProperty("databaseName", databaseName);
                config.addDataSourceProperty("user", settingsManager.getProperty(StorageSettings.SQL_USERNAME));
                config.addDataSourceProperty("password", settingsManager.getProperty(StorageSettings.SQL_PASSWORD));

                if (settingsManager.getProperty(StorageSettings.UTF8)) {
                    config.addDataSourceProperty("characterEncoding", "utf8");
                }
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

    /**
     * Helper method to try a data source until the right one is found
     *
     * @param className the class to try
     */
    public void tryDataSourceName(final String className) {
        try {
            dataSourceName(className);
        } catch (Exception ignored) {
        }
    }

    /**
     * Helper method to check if a class exists to use for mysql data sourcing
     *
     * @param className the class to check
     */
    private void dataSourceName(final String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.dataSourceName = className;
    }

    /**
     * Returns the status of the connection to the MySQL database.
     *
     * @return true if connected, false otherwise
     */
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
