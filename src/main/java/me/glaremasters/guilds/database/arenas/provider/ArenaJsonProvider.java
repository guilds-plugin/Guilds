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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArenaJsonProvider implements ArenaProvider {
    private final File dataFolder;
    private Gson gson;

    /**
     * The arena files that could not be read when the arenas were loaded.
     *
     * <p>A file in here holds an arena the plugin could not use: one it could not parse, or one
     * whose record has no id. The arena storage deletes the files it has no record for, so a file
     * it could not read has to be kept out of that pass. The file is the only copy of that
     * arena's data.
     */
    private final Set<String> unreadableFiles = new HashSet<>();

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
        return JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .anyMatch(f -> JsonFileUtils.getRecordId(f).equals(id));
    }

    @Override
    public List<String> getAllArenaIds(@Nullable String tablePrefix) throws IOException {
        List<String> loadedArenaIds = new ArrayList<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            loadedArenaIds.add(JsonFileUtils.getRecordId(file));
        }

        return loadedArenaIds;
    }

    @Override
    public List<Arena> getAllArenas(@Nullable String tablePrefix) throws IOException {
        List<Arena> loadedArenas = new ArrayList<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            try {
                Arena arena = JsonFileUtils.readJson(file, gson, Arena.class, "Arena");

                // The id is the key the arena is stored and looked up under. A record without one
                // cannot be saved, found or removed, so it is not an arena the plugin can work with.
                if (arena == null || arena.getId() == null) {
                    // The file holds the only copy of that arena, so it is remembered and left alone.
                    unreadableFiles.add(file.getName());
                    LoggingUtils.severe("The Arena file " + file.getAbsolutePath() + " has no id, so it has been prevented from loading.");
                    LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                    continue;
                }

                loadedArenas.add(arena);
            } catch (Exception ex) {
                // The file holds the only copy of that arena, so it is remembered and left alone.
                unreadableFiles.add(file.getName());
                LoggingUtils.severe("There was an error loading an Arena from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Arena has been prevented from loading.");
                LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
            }
        }

        return loadedArenas;
    }

    @Override
    public Arena getArena(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .filter(f -> JsonFileUtils.getRecordId(f).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, Arena.class, "Arena");
    }

    @Override
    public void createArena(@Nullable String tablePrefix, String id, String data) throws IOException {
        if (arenaExists(tablePrefix, id)) return;
        writeArenaFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    @Override
    public void updateArena(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeArenaFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    private void writeArenaFile(File file, String data) throws IOException {
        refuseToOverwriteUnreadableFile(file);
        JsonFileUtils.writeAtomically(file, data);
    }

    /**
     * Refuses to write over an arena file that could not be read when the arenas were loaded.
     *
     * <p>That file is the only copy of the arena it holds. The plugin has no arena for it in
     * memory, so anything written there would replace data the plugin never read with data it
     * never had.
     *
     * @param file the file that is about to be written
     * @throws IOException if the file is one the plugin could not read
     */
    private void refuseToOverwriteUnreadableFile(File file) throws IOException {
        if (!unreadableFiles.contains(file.getName())) {
            return;
        }

        throw new IOException("The arena file " + file.getAbsolutePath() + " could not be read when the plugin started, "
                + "so it has not been changed. Fix the file and restart the server.");
    }

    @Override
    public void deleteArena(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        deleteArena(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION));
    }

    /**
     * Deletes an arena file, unless it is one the plugin could not read.
     *
     * <p>The arena storage deletes the files it has no record for, one after the other, so a file
     * that could not be read is skipped rather than refused: it is the only copy of that arena,
     * and one unreadable file must not stop the rest of the batch from being cleaned up. The
     * load already said which files could not be read.
     *
     * @param file the file to delete
     */
    private void deleteArena(File file) {
        if (unreadableFiles.contains(file.getName())) {
            return;
        }

        if (file.exists()) file.delete();
    }
}
