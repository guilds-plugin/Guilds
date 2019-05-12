/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;
import net.reflxction.simplejson.json.JsonFile;
import net.reflxction.simplejson.json.JsonReader;
import net.reflxction.simplejson.json.JsonWriter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
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
    private Guilds guilds;
    private final List<String> ids = new ArrayList<>();

    public JsonProvider(File dataFolder, Guilds guilds) {
        this.dataFolder = new File(dataFolder, "data");
        this.guilds = guilds;
        //noinspection ResultOfMethodCallIgnored
        this.dataFolder.mkdir();

    }

    @Override
    public List<Guild> loadGuilds() throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>(guilds.getOldGuilds());


        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            JsonFile jsonFile = new JsonFile(file);

            JsonReader reader = new JsonReader(jsonFile);

            reader.setFile(jsonFile);

            loadedGuilds.add(reader.deserializeAs(Guild.class));

            reader.close();
        }

        return loadedGuilds;
    }

    @Override
    public void saveGuilds(List<Guild> guilds) throws IOException {

        for (Guild guild : guilds) {
            JsonFile jsonFile = new JsonFile(new File(dataFolder, guild.getId() + ".json"));

            JsonWriter writer = new JsonWriter(jsonFile);

            writer.setFile(jsonFile);

            writer.writeAndOverride(guild, true);

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

        // Note JsonWriter (writer) does not need to be closed this should only be done if it was instantiated using the BufferedWriter.
        // See JsonWriter#close() for more info.
    }

}
