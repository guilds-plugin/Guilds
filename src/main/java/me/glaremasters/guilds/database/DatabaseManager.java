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

import java.io.IOException;
import java.util.Arrays;

/**
 * A class for managing the database connection using JDBI and HikariDataSource.
 */
public class DatabaseManager {
    private Jdbi jdbi;
    private HikariDataSource hikari;

    /**
     * Constructs a DatabaseManager object and sets up a database connection using the given settings manager and backend.
     *
     * @param settingsManager the settings manager that provides the database connection information
     * @param backend         the type of database backend to use
     * @throws IOException if the database connection cannot be configured or opened
     */
    public DatabaseManager(SettingsManager settingsManager, DatabaseBackend backend) throws IOException {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        config.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        config.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        config.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

        String databaseName = settingsManager.getProperty(StorageSettings.SQL_DATABASE);

        switch (backend) {
            case MYSQL:
                config.setPoolName("Guilds MySQL Connection Pool");
                config.setDataSourceClassName(requireDataSourceClassName(
                        backend,
                        "com.mysql.cj.jdbc.MysqlDataSource",
                        "com.mysql.jdbc.jdbc2.optional.MysqlDataSource"
                ));
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
                config.setDriverClassName(requireDriverClassName(backend, "org.sqlite.JDBC"));
                config.setJdbcUrl("jdbc:sqlite:plugins/Guilds/guilds.db");
                break;
            case MARIADB:
                config.setPoolName("Guilds MariaDB Connection Pool");
                config.setDataSourceClassName(requireDataSourceClassName(
                        backend,
                        "org.mariadb.jdbc.MariaDbDataSource"
                ));
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

        try {
            hikari = new HikariDataSource(config);
        } catch (Exception ex) {
            throw new IOException(
                    "Failed to open " + backend.getBackendName() + " database connection. " +
                            getConnectionDetails(settingsManager, backend, config),
                    ex
            );
        }

        this.jdbi = Jdbi.create(hikari);
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * Returns the status of the connection to the SQL database.
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

    private static String requireDataSourceClassName(DatabaseBackend backend, String... classNames) throws IOException {
        return requireClassName("datasource", backend, classNames);
    }

    private static String requireDriverClassName(DatabaseBackend backend, String... classNames) throws IOException {
        return requireClassName("driver", backend, classNames);
    }

    private static String requireClassName(String classType, DatabaseBackend backend, String... classNames) throws IOException {
        for (String className : classNames) {
            if (isClassAvailable(className)) {
                return className;
            }
        }

        throw new IOException(
                "No " + classType + " class is available for the " + backend.getBackendName() +
                        " backend. Tried: " + Arrays.toString(classNames) +
                        ". Ensure the selected backend's JDBC driver is available to the server/plugin runtime."
        );
    }

    private static boolean isClassAvailable(String className) {
        return isClassAvailable(className, DatabaseManager.class.getClassLoader()) ||
                isClassAvailable(className, Thread.currentThread().getContextClassLoader()) ||
                isClassAvailable(className, ClassLoader.getSystemClassLoader());
    }

    private static boolean isClassAvailable(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            return false;
        }

        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static String getConnectionDetails(
            SettingsManager settingsManager,
            DatabaseBackend backend,
            HikariConfig config
    ) {
        if (backend == DatabaseBackend.SQLITE) {
            return "jdbcUrl=" + config.getJdbcUrl() +
                    ", driver=" + config.getDriverClassName();
        }

        return "host=" + settingsManager.getProperty(StorageSettings.SQL_HOST) +
                ", port=" + settingsManager.getProperty(StorageSettings.SQL_PORT) +
                ", database=" + settingsManager.getProperty(StorageSettings.SQL_DATABASE) +
                ", username=" + settingsManager.getProperty(StorageSettings.SQL_USERNAME) +
                ", datasource=" + config.getDataSourceClassName();
    }
}
