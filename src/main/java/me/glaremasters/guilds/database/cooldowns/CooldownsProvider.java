package me.glaremasters.guilds.database.cooldowns;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.glaremasters.guilds.cooldowns.Cooldown;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Glare
 * Date: 5/14/2019
 * Time: 9:25 PM
 */
public class CooldownsProvider {

    private final File dataFolder;
    private Gson gson;

    public CooldownsProvider(File dataFolder) throws IOException {
        this.dataFolder = new File(dataFolder, "cooldowns");
        this.dataFolder.mkdir();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Load the cooldowns from the file
     * @return cooldowns
     * @throws FileNotFoundException
     */
    public List<Cooldown> loadCooldowns() throws FileNotFoundException {
        List<Cooldown> cooldowns = new ArrayList<>();

        for (File file: Objects.requireNonNull(dataFolder.listFiles())) {
            cooldowns.add(gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Cooldown.class));
        }

        return cooldowns;
    }

    /**
     * Save the list of cooldowns
     * @param cooldowns list of cooldowns
     * @throws IOException
     */
    public void saveCooldowns(List<Cooldown> cooldowns) throws IOException {

        for (Cooldown cooldown : cooldowns) {
            File file = new File(dataFolder, cooldown.getType() + ".json");

            Files.write(Paths.get(file.getPath()), gson.toJson(cooldown).getBytes(StandardCharsets.UTF_8));
        }
    }

}
