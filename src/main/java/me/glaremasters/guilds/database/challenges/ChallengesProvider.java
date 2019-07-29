package me.glaremasters.guilds.database.challenges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.guild.GuildChallenge;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ChallengesProvider {

    private final File dataFolder;
    private Gson gson;

    public ChallengesProvider(File dataFolder) {
        this.dataFolder = new File(dataFolder, "challenges");
        this.dataFolder.mkdir();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Save the challenge
     * @param challenge challenge to save
     * @throws IOException
     */
    public void saveChallenge(GuildChallenge challenge) throws IOException {
        File file = new File(dataFolder, challenge.getId() + ".json");
        Files.write(Paths.get(file.getPath()), gson.toJson(challenge).getBytes(StandardCharsets.UTF_8));
    }
}
