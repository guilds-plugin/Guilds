package me.glaremasters.guilds.database.challenges.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChallengeJsonProvider implements ChallengeProvider {
    private final File dataFolder;
    private Gson gson;

    public ChallengeJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdir();
        }
    }

    @Override
    public List<GuildChallenge> getAllChallenges(@Nullable String tablePrefix) throws IOException {
        List<GuildChallenge> loadedChallenges = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedChallenges.add(gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), GuildChallenge.class));
        }

        return loadedChallenges;
    }

    @Override
    public boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> FilenameUtils.removeExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public GuildChallenge getChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> FilenameUtils.removeExtension(f.getName()).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return gson.fromJson(new InputStreamReader(new FileInputStream(data), StandardCharsets.UTF_8), GuildChallenge.class);
    }

    @Override
    public void createChallenge(@Nullable String tablePrefix, String id, String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void updateChallenge(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        File file = new File(dataFolder, id + ".json");
        deleteChallenge(file);
        writeChallengeFile(file, data);
    }

    @Override
    public void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        deleteChallenge(new File(dataFolder, id + ".json"));
    }

    private void writeChallengeFile(File file, String data) throws IOException {
        Files.write(Paths.get(file.getPath()), data.getBytes(StandardCharsets.UTF_8));
    }

    private void deleteChallenge(File file) {
        if (file.exists()) file.delete();
    }
}
