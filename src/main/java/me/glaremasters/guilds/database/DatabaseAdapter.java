package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.providers.DatabaseProvider;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.guild.Guild;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseAdapter {
    private final DatabaseBackend backend;
    private final DatabaseProvider provider;

    private File fileDataFolder;
    private String sqlTablePrefix;

    public DatabaseAdapter(Guilds guilds, SettingsManager settings) {
        String backendName = settings.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();
        DatabaseBackend backend = DatabaseBackend.getByBackendName(backendName);

        if (backend == null) {
            backend = DatabaseBackend.JSON;
        }

        this.backend = backend;

        if (backend == DatabaseBackend.JSON) {
            fileDataFolder = new File(guilds.getDataFolder(), "data");
            provider = new JsonProvider(fileDataFolder);
        } else {
            DatabaseManager manager = new DatabaseManager(settings);
            provider = manager.getJdbi().onDemand(backend.getProvider());
            sqlTablePrefix = settings.getProperty(StorageSettings.SQL_TABLE_PREFIX).toLowerCase();
        }
    }

    public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
    }

    public boolean guildExists(@NotNull String id) throws IOException {
        return provider.guildExists(sqlTablePrefix, id);
    }

    public List<Guild> getAllGuilds() throws IOException {
        return provider.getAllGuilds(sqlTablePrefix);
    }

    public Guild getGuild(@NotNull String id) throws IOException {
        return provider.getGuild(sqlTablePrefix, id);
    }

    public void saveGuilds(@NotNull List<Guild> guilds) throws IOException {
        List<String> ids = new ArrayList<>();

        for (Guild guild : guilds) {
            saveGuild(guild);
            if (backend == DatabaseBackend.JSON) ids.add(guild.getId().toString());
        }

        if (backend == DatabaseBackend.JSON) {
            for (File file : Objects.requireNonNull(fileDataFolder.listFiles())) {
                String name = FilenameUtils.removeExtension(file.getName());
                boolean keep = ids.stream().anyMatch(str -> str.equals(name));
                if (!keep) {
                    file.delete();
                }
            }

            ids.clear();
        }
    }

    public void saveGuild(@NotNull Guild guild) throws IOException {
        if (!guildExists(guild.getId().toString())) {
            createGuild(guild);
        } else {
            updateGuild(guild);
        }
    }

    public void createGuild(@NotNull Guild guild) throws IOException {
        provider.createGuild(sqlTablePrefix, guild.getId().toString(), Guilds.getGson().toJson(guild, Guild.class));
    }

    public void updateGuild(@NotNull Guild guild) throws IOException {
        provider.updateGuild(sqlTablePrefix, guild.getId().toString(), Guilds.getGson().toJson(guild, Guild.class));
    }

    public void deleteGuild(@NotNull String id) throws IOException {
        provider.deleteGuild(sqlTablePrefix, id);
    }
}
