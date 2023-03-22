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
package me.glaremasters.guilds.database.guild.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class GuildJsonProvider implements GuildProvider {
    private final File dataFolder;
    private Gson gson;

    public GuildJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdir();
        }
    }

    @Override
    public boolean guildExists(@Nullable String tablePrefix, @NotNull String id) {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()))
                .anyMatch(n -> n.equals(id));
    }

    @Override
    public List<String> getAllGuildIds(@Nullable String tablePrefix) {
        List<String> loadedGuildIds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedGuildIds.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return loadedGuildIds;
    }

    @Override
    public List<Guild> getAllGuilds(@Nullable String tablePrefix) {
        List<Guild> loadedGuilds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            try {
                Guild guild = gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Guild.class);
                guild.getId();
                loadedGuilds.add(guild);
            } catch (Exception ex) {
                LoggingUtils.severe("There was an error loading a Guild from the following file: " + file.getAbsolutePath());
                LoggingUtils.severe("To prevent data loss in the plugin, this Guild has been prevented from loading.");
            }
        }

        return loadedGuilds;
    }

    @Override
    public Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> com.google.common.io.Files.getNameWithoutExtension(f.getName()).equals(id))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return gson.fromJson(new InputStreamReader(new FileInputStream(data), StandardCharsets.UTF_8), Guild.class);
    }

    @Override
    public void createGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        if (guildExists(tablePrefix, id)) return;
        writeGuildFile(new File(dataFolder, id + ".json"), data);
    }

    @Override
    public void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException {
        File file = new File(dataFolder, id + ".json");
        deleteGuild(file);
        writeGuildFile(file, data);
    }

    private void writeGuildFile(File file, String data) throws IOException {
        Files.write(Paths.get(file.getPath()), data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deleteGuild(@Nullable String tablePrefix, @NotNull String id) {
        deleteGuild(new File(dataFolder, id + ".json"));
    }

    private void deleteGuild(File file) {
        if (file.exists()) file.delete();
    }
}
