package me.glaremasters.guilds.database.arenas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.arena.Arena;
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

public class ArenasProvider {

    private final File dataFolder;
    private final List<String> ids = new ArrayList<>();
    private Gson gson;

    public ArenasProvider(File dataFolder) {
        this.dataFolder = new File(dataFolder, "arenas");
        this.dataFolder.mkdir();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Load all the arenas from the files
     * @return loaded list of arenas
     * @throws IOException
     */
    public List<Arena> loadArenas() throws IOException {
        List<Arena> loadedArenas = new ArrayList<>();
        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedArenas.add(gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Arena.class));
        }
        return loadedArenas;
    }

    /**
     * Save all the arenas to their files
     * @param arenas the arenas to save
     * @throws IOException
     */
    public void saveArenas(List<Arena> arenas) throws IOException {
        for (Arena arena : arenas) {
            File file = new File(dataFolder, arena.getId() + ".json");
            Files.write(Paths.get(file.getPath()), gson.toJson(arena).getBytes(StandardCharsets.UTF_8));
            ids.add(arena.getId().toString());
        }
        for (File file : dataFolder.listFiles()) {
            String name = FilenameUtils.removeExtension(file.getName());
            boolean keep = ids.stream().anyMatch(str -> str.equalsIgnoreCase(name));
            if (!keep) {
                file.delete();
            }
        }
        ids.clear();
    }
}
