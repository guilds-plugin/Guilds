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
    private final File dataFolder;
    private final Gson gson;

    public ChallengeJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());
    }

    @Override
    public Set<GuildChallenge> getAllChallenges(@Nullable String tablePrefix) {
        final Set<GuildChallenge> challenges = new HashSet<>();

        for (File file : JsonFileUtils.listDataFiles(this.dataFolder)) {
            try {
                GuildChallenge challenge = JsonFileUtils.readJson(file, gson, GuildChallenge.class, "GuildChallenge");

                // The id is what a challenge is looked up and removed by. A record without one
                // cannot be worked with, so it is not loaded.
                if (challenge == null || challenge.getId() == null) {
                    LoggingUtils.severe("The GuildChallenge file " + file.getAbsolutePath() + " has no id, so it has been prevented from loading.");
                    LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
                    continue;
                }

                challenges.add(challenge);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading a GuildChallenge from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this GuildChallenge has been prevented from loading.");
                LoggingUtils.severe("The file has been left where it is, so it can be repaired by hand.");
            }
        }

        return challenges;
    }

    @Override
    public boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) {
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

    @Override
    public void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) {
        deleteChallenge(new File(dataFolder, id + JsonFileUtils.DATA_FILE_EXTENSION));
    }

    private void writeChallengeFile(File file, String data) throws IOException {
        JsonFileUtils.writeAtomically(file, data);
    }

    private void deleteChallenge(File file) {
        if (file.exists()) file.delete();
    }
}
