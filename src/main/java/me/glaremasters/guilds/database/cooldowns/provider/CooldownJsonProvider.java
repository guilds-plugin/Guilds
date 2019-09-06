package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

public class CooldownJsonProvider implements CooldownProvider {
    private final File dataFolder;
    private final File cooldownFile;
    private Gson gson;
    private Type cooldownCollectionType;

    public CooldownJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.cooldownFile = new File(dataFolder, "cooldowns.json");
        this.gson = Guilds.getGson();
        this.cooldownCollectionType = new TypeToken<List<Cooldown>>(){}.getType(); // needed to serialize a list properly
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdir();
        }

        if (!this.cooldownFile.exists()) {
            this.cooldownFile.createNewFile();
        }
    }

    @Override
    public boolean cooldownExists(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException {
        return cooldownExists(cooldownType, cooldownOwner, getAllCooldowns(tablePrefix));
    }

    @Override
    public List<Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException {
        List<Cooldown> loadedCooldowns = gson.fromJson(
                new InputStreamReader(new FileInputStream(cooldownFile), StandardCharsets.UTF_8),
                cooldownCollectionType
        );

        return loadedCooldowns == null ? new ArrayList<>() : loadedCooldowns;
    }

    @Override
    public void createCooldown(@Nullable String tablePrefix, @NotNull String id, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException {
        List<Cooldown> current = getAllCooldowns(tablePrefix);
        if (cooldownExists(cooldownType, cooldownOwner, current)) return;
        current.add(new Cooldown(id, cooldownType, cooldownOwner, cooldownExpiry.getTime()));
        writeCooldownFile(cooldownFile, current);
    }

    @Override
    public void deleteCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException {
        List<Cooldown> current = getAllCooldowns(tablePrefix);
        if (!cooldownExists(cooldownType, cooldownOwner, current)) return;
        current.removeIf(c -> c.getCooldownType().getTypeName().equalsIgnoreCase(cooldownType) && c.getCooldownOwner().toString().equals(cooldownOwner));
        writeCooldownFile(cooldownFile, current);
    }

    private void writeCooldownFile(File file, List<Cooldown> cooldowns) throws IOException {
        Files.write(Paths.get(file.getPath()), gson.toJson(cooldowns, cooldownCollectionType).getBytes(StandardCharsets.UTF_8));
    }

    private boolean cooldownExists(@NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull List<Cooldown> current) throws IOException {
        return current.stream()
                .filter(c -> c.getCooldownType().getTypeName().equalsIgnoreCase(cooldownType) && c.getCooldownOwner().toString().equals(cooldownOwner))
                .findFirst()
                .orElse(null) != null;
    }

}
