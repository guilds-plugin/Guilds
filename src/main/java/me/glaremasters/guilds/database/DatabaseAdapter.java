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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.arenas.ArenaAdapter;
import me.glaremasters.guilds.database.challenges.ChallengeAdapter;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.utils.LoggingUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * A class that implements the DatabaseAdapter interface.
 * This class is responsible for creating and managing various adapters to access the data stored in the backend database.
 * The backend database can be either JSON or SQL (MySQL, SQLite, MariaDB).
 */
public final class DatabaseAdapter implements AutoCloseable {
    private final Guilds guilds;
    private final SettingsManager settings;
    private DatabaseBackend backend;
    private GuildAdapter guildAdapter;
    private ChallengeAdapter challengeAdapter;
    private ArenaAdapter arenaAdapter;
    private CooldownAdapter cooldownAdapter;
    private DatabaseManager databaseManager;
    private String sqlTablePrefix;
    private String sqlHost;
    private String sqlPort;
    private String sqlDatabase;

    /**
     * Creates a new instance of the DatabaseAdapter class.
     *
     * @param guilds   The Guilds instance that will use this database adapter.
     * @param settings The SettingsManager instance that stores the settings for this database adapter.
     * @throws IOException if there is an issue setting up the backend database.
     */
    public DatabaseAdapter(Guilds guilds, SettingsManager settings) throws IOException {
        this(guilds, settings, true);
    }

    /**
     * Creates a new instance of the DatabaseAdapter class.
     *
     * @param guilds    The Guilds instance that will use this database adapter.
     * @param settings  The SettingsManager instance that stores the settings for this database adapter.
     * @param doConnect Specifies whether the database connection should be established immediately or not.
     * @throws IOException if there is an issue setting up the backend database.
     */
    public DatabaseAdapter(Guilds guilds, SettingsManager settings, boolean doConnect) throws IOException {
        this.guilds = guilds;
        this.settings = settings;

        if (doConnect) {
            setUpBackend(getConfiguredBackend());
        }
    }

    /**
     * Returns whether the database connection is established or not.
     *
     * @return true if the database connection is established, false otherwise.
     */
    public boolean isConnected() {
        return getBackend() == DatabaseBackend.JSON ||
                (databaseManager != null && databaseManager.isConnected());
    }

    /**
     * Establishes the database connection if it's not already established.
     */
    public void open() {
        DatabaseBackend configuredBackend = getConfiguredBackend();

        if (configuredBackend == DatabaseBackend.JSON || isConnected()) {
            return;
        }

        try {
            databaseManager = new DatabaseManager(settings, configuredBackend);
        } catch (IOException ex) {
            LoggingUtils.severe(
                    "Failed to reopen the " + configuredBackend.getBackendName() + " database connection.",
                    ex
            );
        }
    }

    /**
     * Closes the database connection if it's established.
     */
    @Override
    public void close() {
        // The field is left in place. `DatabaseManager#close` is idempotent, and keeping the reference means
        // a read after a close fails with the pool's own error rather than with a bare null dereference.
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public DatabaseBackend getBackend() {
        return backend;
    }

    public GuildAdapter getGuildAdapter() {
        return guildAdapter;
    }

    public ChallengeAdapter getChallengeAdapter() {
        return challengeAdapter;
    }

    public ArenaAdapter getArenaAdapter() {
        return arenaAdapter;
    }

    public CooldownAdapter getCooldownAdapter() {
        return cooldownAdapter;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public String getSqlTablePrefix() {
        return sqlTablePrefix;
    }

    /**
     * Creates a clone of the current `DatabaseAdapter` with the given `DatabaseBackend`.
     *
     * @param backend the backend to clone the adapter with
     * @return a cloned instance of `DatabaseAdapter` with the given backend
     * @throws IllegalArgumentException if the given backend matches the current backend
     * @throws IOException              if an I/O error occurs during setup
     */
    public DatabaseAdapter cloneWith(DatabaseBackend backend) throws IllegalArgumentException, IOException {
        if (this.backend.equals(backend)) {
            throw new IllegalArgumentException("Given backend matches current backend. Use this backend.");
        }

        DatabaseAdapter cloned = new DatabaseAdapter(this.guilds, this.settings, false);
        try {
            cloned.setUpBackend(backend);
        } catch (IOException | RuntimeException e) {
            // `setUpBackend` closes the pool it opened, but the clone is discarded either way and must not
            // be left holding one if that ever stops being true.
            cloned.close();
            throw e;
        }
        return cloned;
    }

    /**
     * Whether migrating to {@code backend} would write to the same physical storage this adapter already
     * uses.
     *
     * <p>{@code MYSQL} and {@code MARIADB} are both built from one set of properties, so switching between
     * them can put two pools on one set of tables. The migration would then read and write the rows it is
     * running against, which is a no-op at best. If it fails partway it is worse than a no-op: the cooldown
     * reconciliation has already deleted rows out of the live tables, and there is no second copy to fall
     * back on.
     *
     * <p>Compared on where this adapter's pool was actually opened, not on which backend was asked for. An
     * operator who repoints {@code storage-host} and reloads, then migrates between the two, is moving to a
     * different server and must not be refused. {@code SQLITE} is a fixed file and {@code JSON} is the
     * plugin folder, so neither can collide with a server SQL backend.
     *
     * @param backend the backend being migrated to
     * @return true when both backends address the same tables
     */
    public boolean sharesStorageWith(DatabaseBackend backend) {
        if (!isServerSql(this.backend) || !isServerSql(backend)) {
            return false;
        }
        return Objects.equals(this.sqlHost, settings.getProperty(StorageSettings.SQL_HOST))
                && Objects.equals(this.sqlPort, settings.getProperty(StorageSettings.SQL_PORT))
                && Objects.equals(this.sqlDatabase, settings.getProperty(StorageSettings.SQL_DATABASE))
                && Objects.equals(this.sqlTablePrefix, tablePrefixSetting());
    }

    /**
     * The configured table prefix, normalised the way {@link #setUpBackend} normalises it.
     *
     * @return the lower-cased prefix
     */
    private String tablePrefixSetting() {
        return settings.getProperty(StorageSettings.SQL_TABLE_PREFIX).toLowerCase(Locale.ROOT);
    }

    /**
     * Whether a backend stores its data in the configured SQL server's tables, which every such backend
     * shares with the others.
     *
     * @param backend the backend to classify
     * @return true for {@code MYSQL} and {@code MARIADB}
     */
    private static boolean isServerSql(DatabaseBackend backend) {
        return backend == DatabaseBackend.MYSQL || backend == DatabaseBackend.MARIADB;
    }

    /**
     * Sets up the database backend.
     *
     * @param backend the backend to set up
     * @throws IOException if an I/O error occurs during setup
     */
    private void setUpBackend(DatabaseBackend backend) throws IOException {
        if (this.backend == backend && isConnected()) {
            return;
        }

        this.backend = backend;

        try {
            if (backend != DatabaseBackend.JSON) {
                this.databaseManager = new DatabaseManager(settings, backend);
                this.sqlTablePrefix = tablePrefixSetting();
                this.sqlHost = this.settings.getProperty(StorageSettings.SQL_HOST);
                this.sqlPort = this.settings.getProperty(StorageSettings.SQL_PORT);
                this.sqlDatabase = this.settings.getProperty(StorageSettings.SQL_DATABASE);
            }

            // You may wish to create container(s) elsewhere, but this is an OK spot.
            // In JSON mode, this is equivalent to making the file.
            // In SQL mode, this is equivalent to creating the table.
            this.guildAdapter = new GuildAdapter(guilds, this);
            this.guildAdapter.createContainer();

            this.challengeAdapter = new ChallengeAdapter(guilds, this);
            this.challengeAdapter.createContainer();

            this.arenaAdapter = new ArenaAdapter(guilds, this);
            this.arenaAdapter.createContainer();

            this.cooldownAdapter = new CooldownAdapter(guilds, this);
            this.cooldownAdapter.createContainer();
        } catch (Exception ex) {
            // The pool is already open by the time the first `createContainer` runs, and a failure from
            // there on — a table the database user cannot create, a missing driver — would otherwise abandon
            // it. Both callers discard the adapter when this throws, `cloneWith` and the constructor, so
            // nothing is left that could close it.
            close();
            throw new IOException(
                    "There was an issue setting up the " + backend.getBackendName() +
                            " backend. Shutting down to prevent further issues.",
                    ex
            );
        }
    }

    /**
     * Gets the configured storage backend, defaulting to JSON if the configured value is invalid.
     *
     * @return the configured database backend
     */
    private DatabaseBackend getConfiguredBackend() {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE);

        if (backendName == null) {
            return DatabaseBackend.JSON;
        }

        DatabaseBackend configuredBackend = DatabaseBackend.getByBackendName(backendName.toLowerCase(Locale.ROOT));
        return configuredBackend == null ? DatabaseBackend.JSON : configuredBackend;
    }
}
