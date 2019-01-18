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

import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;
import net.reflxction.simplejson.configuration.DirectConfiguration;
import net.reflxction.simplejson.json.JsonFile;
import net.reflxction.simplejson.json.JsonReader;
import net.reflxction.simplejson.json.JsonWriter;

import java.io.*;
import java.util.*;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class JsonProvider implements DatabaseProvider {

    private File dataFolder;

    //todo asyncify? xD this.

    public JsonProvider(File dataFolder) {
        this.dataFolder = new File(dataFolder, "data");
        //noinspection ResultOfMethodCallIgnored
        this.dataFolder.mkdir();

    }

    @Override
    public List<Guild> loadGuilds() throws IOException {
        List<Guild> loadedGuilds = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())){
            JsonReader reader = new JsonReader(new JsonFile(file));
            loadedGuilds.add(reader.deserializeAs(Guild.class));
            reader.close();
        }

        return loadedGuilds;
    }

    @Override
    public void saveGuilds(List<Guild> guilds) throws IOException {
        for (Guild guild : guilds){
            JsonWriter writer = new JsonWriter(new JsonFile(new File(dataFolder, guild.getId() + ".json")));
            writer.writeAndOverride(guild, true);
        }
    }

}
