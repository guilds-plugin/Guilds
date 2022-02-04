/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class DatabaseManager {
    private final Jdbi jdbi;
    private final HikariDataSource hikari;

    public DatabaseManager(SettingsManager settingsManager, DatabaseBackend backend) {
        this.hikari = new HikariDataSource();

        final String databaseName = settingsManager.getProperty(StorageSettings.SQL_DATABASE);

        hikari.setMaximumPoolSize(settingsManager.getProperty(StorageSettings.SQL_POOL_SIZE));
        hikari.setMinimumIdle(settingsManager.getProperty(StorageSettings.SQL_POOL_IDLE));
        hikari.setMaxLifetime(settingsManager.getProperty(StorageSettings.SQL_POOL_LIFETIME));
        hikari.setConnectionTimeout(settingsManager.getProperty(StorageSettings.SQL_POOL_TIMEOUT));

        if (backend == DatabaseBackend.SQLITE) {
            hikari.setPoolName("Guilds SQLite Connection Pool");
            hikari.setJdbcUrl("jdbc:sqlite:plugins/Guilds/guilds.db");
        } else {
            hikari.setPoolName("Guilds MySQL Pool");
            hikari.setJdbcUrl("jdbc:mysql://" + settingsManager.getProperty(StorageSettings.SQL_HOST) + ":" + settingsManager.getProperty(StorageSettings.SQL_PORT) + "/" + databaseName);
            hikari.setUsername(settingsManager.getProperty(StorageSettings.SQL_USERNAME));
            hikari.setPassword(settingsManager.getProperty(StorageSettings.SQL_PASSWORD));
            hikari.addDataSourceProperty("useSSL", settingsManager.getProperty(StorageSettings.SQL_ENABLE_SSL));
            hikari.addDataSourceProperty("characterEncoding", "utf8");
            hikari.addDataSourceProperty("encoding", "UTF-8");
            hikari.addDataSourceProperty("useUnicode", "true");

            // Random stuff
            hikari.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikari.addDataSourceProperty("jdbcCompliantTruncation", "false");

            // Caching
            hikari.addDataSourceProperty("cachePrepStmts", "true");
            hikari.addDataSourceProperty("prepStmtCacheSize", "275");
            hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            hikari.addDataSourceProperty("allowPublicKeyRetrieval", true);

            if (backend == DatabaseBackend.MYSQL) {
                hikari.setDriverClassName("me.glaremasters.guilds.libs.mysql.cj.jdbc.Driver");
            } else if (backend == DatabaseBackend.MARIADB) {
                hikari.setDriverClassName("me.glaremasters.guilds.libs.jdbc.Driver");
            }
        }

        this.jdbi = Jdbi.create(hikari).installPlugin(new SqlObjectPlugin());
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
