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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public List<String> getAllGuildIds(@Nullable String tablePrefix) {
        List<String> loadedGuildIds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedGuildIds.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return loadedGuildIds;
    }

    @Override
    public List<Guild> getAllGuilds(@Nullable String tablePrefix) {
        List<Guild> loadedGuilds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            try {
                Guild guild = JsonFileUtils.readJson(file, gson, Guild.class, "Guild");
                guild.getId();
                loadedGuilds.add(guild);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading a Guild from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Guild has been prevented from loading.");
            }
        }

        return loadedGuilds;
    }

    @Override
    public Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, Guild.class, "Guild");
    }

    @Override
    public void createGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        if (guildExists(tablePrefix, id)) return;
        writeGuildFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeGuildFile(new File(dataFolder, id + ".json"), data);
    }

    private void writeGuildFile(File file, String data) throws IOException {
        JsonFileUtils.writeAtomically(file, data);
    }

    @Override
    public void deleteGuild(@Nullable String tablePrefix, @NotNull String id) {
        deleteGuild(new File(dataFolder, id + ".json"));
    }

    private void deleteGuild(File file) {
        if (file.exists()) file.delete();
    }
}
