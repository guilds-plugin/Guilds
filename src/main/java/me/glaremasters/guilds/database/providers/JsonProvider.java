/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

package me.glaremasters.guilds.database.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class JsonProvider implements DatabaseProvider {

    private final File dataFolder;
    private final List<String> ids = new ArrayList<>();
    private Gson gson;

    public JsonProvider(File dataFolder) {
        this.dataFolder = new File(dataFolder, "data");
        //noinspection ResultOfMethodCallIgnored
        this.dataFolder.mkdir();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Guild> loadGuilds() throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>();


        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {

            loadedGuilds.add(gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Guild.class));

        }

        return loadedGuilds;
    }

    @Override
    public void saveGuilds(List<Guild> guilds) throws IOException {

        for (Guild guild : guilds) {
            File file = new File(dataFolder, guild.getId() + ".json");
            Files.write(Paths.get(file.getPath()), gson.toJson(guild).getBytes(StandardCharsets.UTF_8));

            ids.add(guild.getId().toString());
        }

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            String name = FilenameUtils.removeExtension(file.getName());
            boolean keep = ids.stream().anyMatch(str -> str.equals(name));
            if (!keep) {
                file.delete();
            }
        }
        ids.clear();
    }

}
