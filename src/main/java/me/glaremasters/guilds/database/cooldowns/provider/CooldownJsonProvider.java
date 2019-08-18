package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.cooldowns.CooldownProvider;
import org.apache.commons.io.FilenameUtils;
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

public class CooldownJsonProvider implements CooldownProvider {
    private final File dataFolder;
    private Gson gson;

    public CooldownJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdir();
        }
    }

    @Override
    public boolean cooldownExists(@Nullable String tablePrefix, @NotNull String name) throws IOException {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .map(f -> FilenameUtils.removeExtension(f.getName()))
                .anyMatch(n -> n.equals(name));
    }

    @Override
    public List<Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException {
        List<Cooldown> loadedCooldowns = new ArrayList<>();

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            loadedCooldowns.add(gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Cooldown.class));
        }

        return loadedCooldowns;
    }

    @Override
    public Cooldown getCooldown(@Nullable String tablePrefix, @NotNull String name) throws IOException {
        File data = Arrays.stream(Objects.requireNonNull(dataFolder.listFiles()))
                .filter(f -> FilenameUtils.removeExtension(f.getName()).equals(name))
                .findFirst()
                .orElse(null);

        if (data == null) return null;

        return gson.fromJson(new InputStreamReader(new FileInputStream(data), StandardCharsets.UTF_8), Cooldown.class);
    }

    @Override
    public void createCooldown(@Nullable String tablePrefix, String name, String data) throws IOException {
        if (cooldownExists(tablePrefix, name)) return;
        writeCooldownFile(new File(dataFolder, name + ".json"), data);
    }

    @Override
    public void updateCooldown(@Nullable String tablePrefix, @NotNull String name, @NotNull String data) throws IOException {
        File file = new File(dataFolder, name + ".json");
        deleteCooldown(file);
        writeCooldownFile(file, data);
    }

    private void writeCooldownFile(File file, String data) throws IOException {
        Files.write(Paths.get(file.getPath()), data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void deleteCooldown(@Nullable String tablePrefix, @NotNull String name) throws IOException {
        deleteCooldown(new File(dataFolder, name + ".json"));
    }

    private void deleteCooldown(File file) {
        if (file.exists()) file.delete();
    }
}
