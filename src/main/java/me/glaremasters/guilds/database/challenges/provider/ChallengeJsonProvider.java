package me.glaremasters.guilds.database.challenges.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    public void createChallenge(@Nullable String tablePrefix, String id, String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + ".json"), data);
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
