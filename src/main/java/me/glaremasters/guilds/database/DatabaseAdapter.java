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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.arenas.ArenaAdapter;
import me.glaremasters.guilds.database.challenges.ChallengeAdapter;
import me.glaremasters.guilds.database.cooldowns.CooldownAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.bukkit.Bukkit;

import java.io.IOException;

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

    public DatabaseAdapter(Guilds guilds, SettingsManager settings) throws IOException {
        this(guilds, settings, true);
    }

    public DatabaseAdapter(Guilds guilds, SettingsManager settings, boolean doConnect) throws IOException {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();
        DatabaseBackend backend = DatabaseBackend.getByBackendName(backendName);

        if (backend == null) {
            backend = DatabaseBackend.JSON;
        }

        this.guilds = guilds;
        this.settings = settings;

        if (doConnect) {
            setUpBackend(backend);
        }
    }

    public boolean isConnected() {
        return getBackend() == DatabaseBackend.JSON ||
                (databaseManager != null && databaseManager.isConnected());
    }

    public void open() {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();
        DatabaseBackend backend = DatabaseBackend.getByBackendName(backendName);
        if (databaseManager != null && !databaseManager.isConnected()) {
            databaseManager = new DatabaseManager(settings, backend);
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

    public DatabaseAdapter cloneWith(DatabaseBackend backend) throws IllegalArgumentException, IOException {
        if (this.backend.equals(backend)) {
            throw new IllegalArgumentException("Given backend matches current backend. Use this backend.");
        }

        DatabaseAdapter cloned = new DatabaseAdapter(this.guilds, this.settings, false);
        cloned.setUpBackend(backend);
        return cloned;
    }

    private void setUpBackend(DatabaseBackend backend) throws IOException {
        if (isConnected()) return;

        if (backend != DatabaseBackend.JSON) {
            this.databaseManager = new DatabaseManager(settings, backend);
            this.sqlTablePrefix = this.settings.getProperty(StorageSettings.SQL_TABLE_PREFIX).toLowerCase();
        }

        this.backend = backend;

        try {
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
            LoggingUtils.severe("There was an issue setting up the backend database. Shutting down to prevent further issues. If you are using MySQL, make sure your database server is on the latest version!");
            ex.printStackTrace();
            Bukkit.getServer().getPluginManager().disablePlugin(guilds);
        }
    }
}
