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

package me.glaremasters.guilds.database.cooldowns;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Glare
 * Date: 5/14/2019
 * Time: 9:25 PM
 */
public class CooldownsProvider {

    private final File dataFolder;
    private Gson gson;
    private Guilds guilds;

    public CooldownsProvider(Guilds guilds) {
        this.dataFolder = new File(guilds.getDataFolder(), "cooldowns");
        this.dataFolder.mkdir();
        this.gson = guilds.getGson();
    }

    /**
     * Load the cooldowns from the file
     * @return cooldowns
     * @throws FileNotFoundException
     */
    public Map<String, Cooldown> loadCooldowns() throws FileNotFoundException {
        Map<String, Cooldown> cooldowns = new HashMap<>();

        for (File file: Objects.requireNonNull(dataFolder.listFiles())) {
            String name = FilenameUtils.removeExtension(file.getName());
            cooldowns.put(name, gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Cooldown.class));
        }

        return cooldowns;
    }

    /**
     * Save the list of cooldowns
     * @param cooldowns list of cooldowns
     * @throws IOException
     */
    public void saveCooldowns(Map<String, Cooldown> cooldowns) throws IOException {

        for (Map.Entry<String, Cooldown> entry : cooldowns.entrySet()) {
            File file = new File(dataFolder, entry.getKey() + ".json");
            Files.write(Paths.get(file.getPath()), gson.toJson(entry.getValue()).getBytes(StandardCharsets.UTF_8));
        }
    }

}
