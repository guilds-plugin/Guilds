package me.glaremasters.guilds.database.arenas.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.JsonFileUtils;
import me.glaremasters.guilds.database.arenas.ArenaProvider;
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

public class ArenaJsonProvider implements ArenaProvider {
    private final File dataFolder;
    private Gson gson;

    public ArenaJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());
    }

    @Override
    public boolean arenaExists(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public List<String> getAllArenaIds(@Nullable String tablePrefix) throws IOException {
        List<String> loadedArenaIds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedArenaIds.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return loadedArenaIds;
    }

    @Override
    public List<Arena> getAllArenas(@Nullable String tablePrefix) throws IOException {
        List<Arena> loadedArenas = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            try {
                Arena arena = JsonFileUtils.readJson(file, gson, Arena.class, "Arena");
                arena.getId();
                loadedArenas.add(arena);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading an Arena from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Arena has been prevented from loading.");
            }
        }

        return loadedArenas;
    }

    @Override
    public Arena getArena(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, Arena.class, "Arena");
    }

    @Override
    public void createArena(@Nullable String tablePrefix, String id, String data) throws IOException {
        if (arenaExists(tablePrefix, id)) return;
        writeArenaFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void updateArena(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeArenaFile(new File(dataFolder, id + ".json"), data);
    }

    private void writeArenaFile(File file, String data) throws IOException {
        JsonFileUtils.writeAtomically(file, data);
    }

    @Override
    public void deleteArena(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        deleteArena(new File(dataFolder, id + ".json"));
    }

    private void deleteArena(File file) {
        if (file.exists()) file.delete();
    }
}
