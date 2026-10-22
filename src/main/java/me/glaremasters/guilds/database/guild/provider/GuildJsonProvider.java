package me.glaremasters.guilds.database.guild.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.CorruptDataBackup;
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
     * The name the unreadable guild files are copied under, inside the backup folder.
     */
    private static final String BACKUP_DATA_TYPE = "guilds";

    private final File pluginDataFolder;
    private final File dataFolder;
    private Gson gson;

    /**
     * The guild files that could not be read when the guilds were loaded.
     *
     * <p>A file in here holds data the plugin could not parse, which makes it the only copy of that
     * guild. The save must never write over it: the plugin does not have the guild in memory, so
     * anything it wrote would be a guess at data it never read.
     */
    private final Set<String> unreadableFiles = new HashSet<>();

    /**
     * Creates a provider over a guild data folder.
     *
     * <p>The plugin's own data folder is the parent of the guild data folder, which is where the
     * backups of unreadable files go.
     *
     * @param dataFolder the folder the guild files live in
     */
    public GuildJsonProvider(File dataFolder) {
        this(dataFolder == null ? null : dataFolder.getParentFile(), dataFolder);
    }

    /**
     * Creates a provider over a guild data folder.
     *
     * @param pluginDataFolder the plugin's data folder, {@code plugins/Guilds}. The backups of
     *                         unreadable guild files are written below it.
     * @param dataFolder       the folder the guild files live in
     */
    public GuildJsonProvider(File pluginDataFolder, File dataFolder) {
        this.pluginDataFolder = pluginDataFolder;
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());
    }

    @Override
    public boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        return Arrays.stream(listGuildFiles())
                .map(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException {
        List<String> loadedGuildIds = new ArrayList<>();

        for (File file : listGuildFiles()) {
            loadedGuildIds.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return loadedGuildIds;
    }

    /**
     * Reads every guild file in the data folder.
     *
     * <p>A folder that cannot be listed is a problem with the storage, not with one guild, and it
     * used to come out of here as a {@link NullPointerException} that named nothing. It is an
     * {@link IOException} naming the folder instead, so the plugin can say what went wrong.
     *
     * @return the files in the guild data folder
     * @throws IOException if the folder cannot be read
     */
    private File[] listGuildFiles() throws IOException {
        final File[] files = dataFolder.listFiles();

        if (files == null) {
            throw new IOException("The guild data folder could not be read: " + dataFolder.getAbsolutePath());
        }

        return files;
    }

    @Override
    public List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>();
        final File[] files = listGuildFiles();
        int unreadable = 0;

        for (File file : files) {
            try {
                Guild guild = JsonFileUtils.readJson(file, gson, Guild.class, "Guild");
                guild.getId();
                loadedGuilds.add(guild);
            } catch (Exception ex) {
                unreadable++;
                // The file holds the only copy of that guild, so it is remembered and left alone.
                unreadableFiles.add(file.getName());
                LoggingUtils.severe("There was an error loading a Guild from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this Guild has been prevented from loading. "
                        + "The file has been left untouched and will not be written to. "
                        + "Fix the file, or move it out of the data folder, and restart the server.");
                backUp(file);
            }
        }

        if (unreadable > 0) {
            LoggingUtils.severe(unreadable + " of " + (loadedGuilds.size() + unreadable) + " guild files could not be read. "
                    + "Those guilds are not available until the files are fixed.");
        }

        /*
         * A folder full of files the plugin cannot read is not an empty guild database. It is a
         * guild database the plugin failed to read, and a server that comes up as if it had no
         * guilds at all hides that: the guilds are still on disk, and the first save would write
         * over them. The load fails instead, the way a folder that cannot be listed does.
         */
        if (loadedGuilds.isEmpty() && files.length > 0) {
            throw new IOException("None of the " + files.length + " files in " + dataFolder.getAbsolutePath()
                    + " could be read as a guild, so the guild data could not be loaded. "
                    + "The files have been left where they are. "
                    + "Fix them, or move them out of the data folder, and restart the server.");
        }

        return loadedGuilds;
    }

    /**
     * Copies a guild file the plugin could not read into the backup folder.
     *
     * <p>The copy is a safeguard, not a repair. The original stays exactly where the operator put
     * it, and the plugin keeps refusing to write to it.
     *
     * @param file the file that could not be read
     */
    private void backUp(File file) {
        final CorruptDataBackup.Result backup = CorruptDataBackup.backup(pluginDataFolder, file, BACKUP_DATA_TYPE);

        if (backup.isCreated()) {
            LoggingUtils.severe("A copy of the file was taken to " + backup.getBackupFile().getAbsolutePath() + ".");
        } else if (backup.isSkipped()) {
            LoggingUtils.severe("A copy of the file was already taken to " + backup.getBackupFile().getAbsolutePath() + ".");
        } else {
            LoggingUtils.severe("No copy of the file could be taken: " + backup.getFailure() + ". "
                    + "The file itself is still where it was.");
        }
    }

    @Override
    public Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(listGuildFiles())
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
        deleteGuild(new File(dataFolder, id + ".json"));
    }

    private void deleteGuild(File file) throws IOException {
        refuseToOverwriteUnreadableFile(file);

        if (file.exists()) file.delete();
    }
}
