package me.glaremasters.guilds.database.cooldowns;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public CooldownsProvider(File dataFolder) {
        this.dataFolder = new File(dataFolder, "cooldowns");
        this.dataFolder.mkdir();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
