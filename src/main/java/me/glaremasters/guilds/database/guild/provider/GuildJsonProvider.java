package me.glaremasters.guilds.database.guild.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.JsonFileUtils;
import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class GuildJsonProvider implements GuildProvider {
    private final File dataFolder;
    private Gson gson;

    public GuildJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());
    }

    @Override
    public boolean guildExists(@Nullable String tablePrefix, @NotNull String id) {
        return JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .anyMatch(f -> JsonFileUtils.getRecordId(f).equals(id));
    }

    @Override
    public List<String> getAllGuildIds(@Nullable String tablePrefix) {
        List<String> loadedGuildIds = new ArrayList<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            loadedGuildIds.add(JsonFileUtils.getRecordId(file));
        }

        return loadedGuildIds;
    }

    @Override
    public List<Guild> getAllGuilds(@Nullable String tablePrefix) {
        List<Guild> loadedGuilds = new ArrayList<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            try {
                Guild guild = JsonFileUtils.readJson(file, gson, Guild.class, "Guild");

                // The id is the key the guild is stored and looked up under. A record without one
                // cannot be saved, found or removed, so it is not a guild the plugin can work with.
                if (guild == null || guild.getId() == null) {
                    LoggingUtils.severe("The Guild file " + file.getAbsolutePath() + " has no id, so it has been prevented from loading.");
                    LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                    continue;
                }

                loadedGuilds.add(guild);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading a Guild from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Guild has been prevented from loading.");
                LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
            }
        }

        return loadedGuilds;
    }

    @Override
    public Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .filter(f -> JsonFileUtils.getRecordId(f).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, Guild.class, "Guild");
    }

    @Override
    public void createGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        if (guildExists(tablePrefix, id)) return;
        writeGuildFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    @Override
    public void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeGuildFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    private void writeGuildFile(File file, String data) throws IOException {
        JsonFileUtils.writeAtomically(file, data);
    }

    @Override
    public void deleteGuild(@Nullable String tablePrefix, @NotNull String id) {
        deleteGuild(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION));
    }

    private void deleteGuild(File file) {
        if (file.exists()) file.delete();
    }
}
