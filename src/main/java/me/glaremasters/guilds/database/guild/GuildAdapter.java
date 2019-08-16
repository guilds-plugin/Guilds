package me.glaremasters.guilds.database.guild;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.guild.Guild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuildAdapter {
    private final GuildProvider provider;
    private String sqlTablePrefix;

    public GuildAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        if (backend == DatabaseBackend.JSON) {
            File fileDataFolder = new File(guilds.getDataFolder(), "data");
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

    public List<String> getAllGuildIds() throws IOException {
        return provider.getAllGuildIds(sqlTablePrefix);
    }

    public List<Guild> getAllGuilds() throws IOException {
        return provider.getAllGuilds(sqlTablePrefix);
    }

    public Guild getGuild(@NotNull String id) throws IOException {
        return provider.getGuild(sqlTablePrefix, id);
    }

    public void saveGuilds(@NotNull List<Guild> guilds) throws IOException {
        List<String> savedIds = new ArrayList<>();

        for (Guild guild : guilds) {
            saveGuild(guild);
            savedIds.add(guild.getId().toString());
        }

        for (String guildId : getAllGuildIds()) { // This may be slow on SQL-based backends, need benchmarking
            boolean keep = savedIds.stream().anyMatch(id -> id.equals(guildId));
            if (!keep) {
                deleteGuild(guildId);
            }
        }

        savedIds.clear();
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
