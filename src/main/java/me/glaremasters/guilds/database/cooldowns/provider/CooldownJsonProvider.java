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
package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.cooldowns.CooldownProvider;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        current.add(new Cooldown(UUID.fromString(id), Cooldown.Type.getByTypeName(cooldownType), UUID.fromString(cooldownOwner), cooldownExpiry.getTime()));
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
