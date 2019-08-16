package me.glaremasters.guilds.database.guild;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.guild.Guild;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuildAdapter {
    private final DatabaseBackend backend;
    private final GuildProvider provider;
    private File fileDataFolder;
    private String sqlTablePrefix;

    public GuildAdapter(Guilds guilds, DatabaseAdapter adapter) {
        backend = adapter.getBackend();
        if (backend == DatabaseBackend.JSON) {
            fileDataFolder = new File(guilds.getDataFolder(), "data");
            provider = new GuildJsonProvider(fileDataFolder);
        } else {
            sqlTablePrefix = adapter.getSqlTablePrefix();
            provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getGuildProvider());
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
        // TODO: this whole deal here with the ids and deletion is reimplemented in GuildHandler, but if you want
        //      to keep guild deletion here in the save task, then we are going to have to do some work.

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
                    deleteGuild(name);
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
