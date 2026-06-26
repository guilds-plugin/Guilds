/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.database.challenges.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.JsonFileUtils;
import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            try {
                GuildChallenge challenge = JsonFileUtils.readJson(file, gson, GuildChallenge.class, "GuildChallenge");
                challenge.getId();
                challenges.add(challenge);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading a GuildChallenge from the following file: " + file.getAbsolutePath(), ex);
                LoggingUtils.severe("To prevent data loss in the plugin, this GuildChallenge has been prevented from loading.");
            }
        }

        return challenges;
    }

    @Override
    public boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public GuildChallenge getChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return JsonFileUtils.readJson(data, gson, GuildChallenge.class, "GuildChallenge");
    }

    @Override
    public void createChallenge(@Nullable String tablePrefix, String id, String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void updateChallenge(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        writeChallengeFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) {
        deleteChallenge(new File(dataFolder, id + ".json"));
    }

    private void writeChallengeFile(File file, String data) throws IOException {
        JsonFileUtils.writeAtomically(file, data);
    }

    private void deleteChallenge(File file) {
        if (file.exists()) file.delete();
    }
}
