package me.glaremasters.guilds.database.challenges.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.JsonFileUtils;
import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class ChallengeJsonProvider implements ChallengeProvider {
    /**
     * The kind of record the challenge files hold, used as the name of the backup folder.
     */
    private static final String DATA_TYPE = "challenges";

    private final File dataFolder;
    private final File pluginDataFolder;
    private final Gson gson;

    /**
     * The challenge files that could not be read when the challenges were loaded.
     *
     * <p>A file in here holds a challenge the plugin could not use: one it could not parse, or one
     * whose record has no id. The file is the only copy of that challenge's data, so it is never
     * written to or deleted.
     */
    private final Set<String> unreadableFiles = new HashSet<>();

    public ChallengeJsonProvider(File dataFolder) {
        this(dataFolder, dataFolder.getParentFile());
    }

    /**
     * @param dataFolder       the folder the challenge files live in
     * @param pluginDataFolder the plugin's data folder, where the backups are kept
     */
    public ChallengeJsonProvider(File dataFolder, File pluginDataFolder) {
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
    public Set<GuildChallenge> getAllChallenges(@Nullable String tablePrefix) throws IOException {
        final Set<GuildChallenge> challenges = new HashSet<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            try {
                GuildChallenge challenge = JsonFileUtils.readJson(file, gson, GuildChallenge.class, "GuildChallenge");

                // The id is what a challenge is looked up and removed by. A record without one
                // cannot be worked with, so it is not loaded.
                if (challenge == null || challenge.getId() == null) {
                    // The file holds the only copy of that challenge, so it is remembered and left alone.
                    unreadableFiles.add(file.getName());
                    LoggingUtils.severe("The GuildChallenge file " + file.getAbsolutePath() + " has no id, so it has been prevented from loading.");
                    LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                    JsonFileUtils.backupCorruptFile(pluginDataFolder, file, DATA_TYPE, "the record has no id");
                    continue;
                }

                challenges.add(challenge);
            } catch (Exception ex) {
                // The file holds the only copy of that challenge, so it is remembered and left alone.
                unreadableFiles.add(file.getName());
                LoggingUtils.severe("There was an error loading a GuildChallenge from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this GuildChallenge has been prevented from loading.");
                LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                JsonFileUtils.backupCorruptFile(pluginDataFolder, file, DATA_TYPE, ex);
            }
        }

        return challenges;
    }

    @Override
    public boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        return JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .anyMatch(f -> JsonFileUtils.getRecordId(f).equals(id));
    }

    @Override
    public GuildChallenge getChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = JsonFileUtils.listDataFiles(this.dataFolder).stream()
                .filter(f -> JsonFileUtils.getRecordId(f).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, GuildChallenge.class, "GuildChallenge");
    }

    @Override
    public void createChallenge(@Nullable String tablePrefix, String id, String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    @Override
    public void updateChallenge(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION), data);
    }

    private void writeChallengeFile(File file, String data) throws IOException {
        refuseToOverwriteUnreadableFile(file);
        JsonFileUtils.writeAtomically(file, data);
    }

    /**
     * Refuses to change a challenge file that could not be read when the challenges were loaded.
     *
     * <p>That file is the only copy of the challenge it holds. The plugin has no challenge for it
     * in memory, so anything written there would replace data the plugin never read with data it
     * never had.
     *
     * @param file the file that is about to be written or deleted
     * @throws IOException if the file is one the plugin could not read
     */
    private void refuseToOverwriteUnreadableFile(File file) throws IOException {
        if (!unreadableFiles.contains(file.getName())) {
            return;
        }

        throw new IOException("The challenge file " + file.getAbsolutePath() + " could not be read when the plugin started, "
                + "so it has not been changed. Fix the file and restart the server.");
    }

    @Override
    public void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        deleteChallenge(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION));
    }

    private void deleteChallenge(File file) throws IOException {
        refuseToOverwriteUnreadableFile(file);

        if (!file.exists()) return;

        file.delete();

        /*
         * A delete that did not happen has to be reported. The challenge is gone from the plugin
         * once the delete is asked for, so nothing would try again, and the next restart would
         * load the challenge that was supposed to be gone.
         */
        if (file.exists()) {
            throw new IOException("The challenge file " + file.getAbsolutePath() + " could not be deleted.");
        }
    }
}
