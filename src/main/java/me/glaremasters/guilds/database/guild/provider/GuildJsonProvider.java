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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class GuildJsonProvider implements GuildProvider {
    /**
     * The kind of record the guild files hold, used as the name of the backup folder.
     */
    private static final String DATA_TYPE = "guilds";

    private final File dataFolder;
    private final File pluginDataFolder;
    private Gson gson;

    /**
     * The guild files that could not be read when the guilds were loaded.
     *
     * <p>A file in here holds a guild the plugin could not use: one it could not parse, or one
     * whose record has no id. Either way the file is the only copy of that guild's data, so the
     * save must never write over it. The plugin does not have the guild in memory, so anything it
     * wrote would be a guess at data it never read.
     */
    private final Set<String> unreadableFiles = new HashSet<>();

    public GuildJsonProvider(File dataFolder) {
        this(dataFolder, dataFolder == null ? null : dataFolder.getParentFile());
    }

    /**
     * @param dataFolder       the folder the guild files live in
     * @param pluginDataFolder the plugin's data folder, where the backups are kept
     */
    public GuildJsonProvider(File dataFolder, File pluginDataFolder) {
        this.dataFolder = dataFolder;
        this.pluginDataFolder = pluginDataFolder;
        this.gson = Guilds.getGson();

        JsonFileUtils.warnIfFoldersAreSwapped(pluginDataFolder, dataFolder, DATA_TYPE);
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());
    }

    @Override
    public boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        return JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .anyMatch(f -> JsonFileUtils.getRecordId(f).equals(id));
    }

    @Override
    public List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException {
        List<String> loadedGuildIds = new ArrayList<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            loadedGuildIds.add(JsonFileUtils.getRecordId(file));
        }

        return loadedGuildIds;
    }

    @Override
    public List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>();
        final List<File> files = JsonFileUtils.listDataFiles(this.dataFolder);
        int unreadable = 0;

        for (File file : files) {
            try {
                Guild guild = JsonFileUtils.readJson(file, gson, Guild.class, "Guild");

                // The id is the key the guild is stored and looked up under. A record without one
                // cannot be saved, found or removed, so it is not a guild the plugin can work with.
                if (guild == null || guild.getId() == null) {
                    unreadable++;
                    // The file holds the only copy of that guild, so it is remembered and left alone.
                    unreadableFiles.add(file.getName());
                    LoggingUtils.severe("The Guild file " + file.getAbsolutePath() + " has no id, so it has been prevented from loading.");
                    LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                    JsonFileUtils.backupCorruptFile(pluginDataFolder, file, DATA_TYPE, "the record has no id");
                    continue;
                }

                loadedGuilds.add(guild);
            } catch (Exception ex) {
                unreadable++;
                // The file holds the only copy of that guild, so it is remembered and left alone.
                unreadableFiles.add(file.getName());
                LoggingUtils.severe("There was an error loading a Guild from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Guild has been prevented from loading.");
                LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                JsonFileUtils.backupCorruptFile(pluginDataFolder, file, DATA_TYPE, ex);
            }
        }

        if (unreadable > 0) {
            LoggingUtils.severe(unreadable + " of " + files.size() + " guild files could not be read. "
                    + "Those guilds are not available until the files are fixed.");
        }

        /*
         * A folder full of files the plugin cannot read is not an empty guild database. It is a
         * guild database the plugin failed to read, and a server that comes up as if it had no
         * guilds at all hides that: the guilds are still on disk, and the first save would write
         * over them. The load fails instead, the way a folder that cannot be listed does.
         */
        if (loadedGuilds.isEmpty() && !files.isEmpty()) {
            throw new IOException("None of the " + files.size() + " guild files in " + dataFolder.getAbsolutePath()
                    + " could be read as a guild, so the guild data could not be loaded. "
                    + "The files have been left where they are. "
                    + "Fix them, or move them out of the data folder, and restart the server.");
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
        refuseToOverwriteUnreadableFile(file);
        JsonFileUtils.writeAtomically(file, data);
    }

    /**
     * Refuses to change a guild file that could not be read when the guilds were loaded.
     *
     * <p>That file is the only copy of the guild it holds. The plugin has no guild for it in
     * memory, so anything written there would replace data the plugin never read with data it
     * never had.
     *
     * @param file the file that is about to be written or deleted
     * @throws IOException if the file is one the plugin could not read
     */
    private void refuseToOverwriteUnreadableFile(File file) throws IOException {
        if (!unreadableFiles.contains(file.getName())) {
            return;
        }

        throw new IOException("The guild file " + file.getAbsolutePath() + " could not be read when the plugin started, "
                + "so it has not been changed. Fix the file and restart the server.");
    }

    @Override
    public void deleteGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        deleteGuild(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION));
    }

    private void deleteGuild(File file) throws IOException {
        refuseToOverwriteUnreadableFile(file);

        if (!file.exists()) return;

        file.delete();

        /*
         * A delete that did not happen has to be reported. The caller has already taken the guild
         * out of the plugin, so the autosave will not touch the file again, and the next restart
         * would load the guild the player was told was deleted.
         */
        if (file.exists()) {
            throw new IOException("The guild file " + file.getAbsolutePath() + " could not be deleted.");
        }
    }
}
