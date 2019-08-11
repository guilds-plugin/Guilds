package me.glaremasters.guilds.database.providers;

import ch.jalu.configme.SettingsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.Queries;
import me.glaremasters.guilds.guild.Guild;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLProvider implements DatabaseProvider {

    private Gson gson;
    private HikariDataSource hikari;
    private SettingsManager settingsManager;
    private Queries queries;

    public MySQLProvider(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.queries = new Queries();

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

        // Try to create the table if it doesn't exist
        queries.createTable(hikari);


        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Guild> loadGuilds() throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>();

        try {
            queries.loadGuilds(gson, hikari, loadedGuilds);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loadedGuilds;
    }

    @Override
    public void saveGuilds(List<Guild> guilds) throws IOException {
        for (Guild guild : guilds) {
            try {
                queries.saveGuild(gson, guild, hikari);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
