package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.JsonFileUtils;
import me.glaremasters.guilds.database.cooldowns.CooldownProvider;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CooldownJsonProvider implements CooldownProvider {
    /**
     * The kind of record the cooldown file holds, used as the name of the backup folder.
     */
    private static final String DATA_TYPE = "cooldowns";

    private final File dataFolder;
    private final File pluginDataFolder;
    private final File cooldownFile;
    private Gson gson;

    public CooldownJsonProvider(File dataFolder) {
        this(dataFolder, dataFolder.getParentFile());
    }

    /**
     * @param dataFolder       the folder the cooldown file lives in
     * @param pluginDataFolder the plugin's data folder, where the backups are kept
     */
    public CooldownJsonProvider(File dataFolder, File pluginDataFolder) {
        this.dataFolder = dataFolder;
        this.pluginDataFolder = pluginDataFolder;
        this.cooldownFile = new File(dataFolder, "cooldowns.json");
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws IOException {
        Files.createDirectories(this.dataFolder.toPath());

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
        return readCooldownFile().cooldowns;
    }

    @Override
    public void createCooldown(@Nullable String tablePrefix, @NotNull String id, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException {
        final CooldownFile contents = readCooldownFile();

        if (cooldownExists(cooldownType, cooldownOwner, contents.cooldowns)) return;

        contents.cooldowns.add(new Cooldown(UUID.fromString(id), Cooldown.Type.getByTypeName(cooldownType), UUID.fromString(cooldownOwner), cooldownExpiry.getTime()));
        writeCooldownFile(contents);
    }

    @Override
    public void deleteCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException {
        final CooldownFile contents = readCooldownFile();

        if (!cooldownExists(cooldownType, cooldownOwner, contents.cooldowns)) return;

        contents.cooldowns.removeIf(c -> c.getCooldownType().getTypeName().equalsIgnoreCase(cooldownType) && c.getCooldownOwner().toString().equals(cooldownOwner));
        writeCooldownFile(contents);
    }

    /**
     * The contents of the cooldown file.
     *
     * <p>One file holds a list of cooldowns, so one broken entry used to take the whole list with
     * it: the file could not be parsed, every cooldown in it was lost for the session, and the
     * plugin logged a single error with no way to tell which entry was at fault.
     *
     * <p>The entries that cannot be read are kept as they are written, so a later write puts them
     * back unchanged instead of quietly dropping them.
     */
    private static final class CooldownFile {
        private final List<Cooldown> cooldowns = new ArrayList<>();
        private final List<JsonElement> unreadable = new ArrayList<>();
    }

    /**
     * Reads the cooldown file.
     *
     * <p>An entry that cannot be read is skipped and logged, and the entries around it are still
     * returned. The whole file being unreadable is still an error: there is nothing to salvage
     * then, and the caller has to know that no cooldowns were loaded.
     *
     * @return the cooldowns in the file, and the entries that could not be read
     * @throws IOException if the file cannot be read or does not hold a list of cooldowns
     */
    private CooldownFile readCooldownFile() throws IOException {
        final CooldownFile contents = new CooldownFile();

        if (!this.cooldownFile.exists()) {
            return contents;
        }

        final JsonElement root;
        try (Reader reader = Files.newBufferedReader(this.cooldownFile.toPath(), StandardCharsets.UTF_8)) {
            root = new JsonParser().parse(reader);
        } catch (RuntimeException ex) {
            // The file cannot be read at all, so it is kept aside before the caller is told.
            JsonFileUtils.backupCorruptFile(pluginDataFolder, this.cooldownFile, DATA_TYPE, ex);
            throw new IOException("Failed to parse cooldowns JSON file: " + this.cooldownFile.getAbsolutePath(), ex);
        }

        // An empty file is an empty list of cooldowns, which is what the container starts as.
        if (root == null || root.isJsonNull()) {
            return contents;
        }

        if (!root.isJsonArray()) {
            JsonFileUtils.backupCorruptFile(pluginDataFolder, this.cooldownFile, DATA_TYPE, "the file does not hold a list of cooldowns");
            throw new IOException("The cooldowns JSON file does not hold a list of cooldowns: " + this.cooldownFile.getAbsolutePath());
        }

        final JsonArray entries = root.getAsJsonArray();

        for (int i = 0; i < entries.size(); i++) {
            final JsonElement entry = entries.get(i);

            try {
                final Cooldown cooldown = gson.fromJson(entry, Cooldown.class);

                // A cooldown without a type or an owner cannot be used, and asking for one used to
                // throw where the cooldowns are used.
                if (cooldown == null || cooldown.getCooldownType() == null || cooldown.getCooldownOwner() == null) {
                    contents.unreadable.add(entry);
                    LoggingUtils.severe("There was an error loading a Cooldown from the cooldowns file: " + this.cooldownFile.getAbsolutePath());
                    LoggingUtils.severe("The entry at position " + i + " could not be read. To prevent data loss in the plugin, this Cooldown has been prevented from loading. The entry has been left in the file as it is.");
                    continue;
                }

                contents.cooldowns.add(cooldown);
            } catch (RuntimeException ex) {
                contents.unreadable.add(entry);
                LoggingUtils.severe("There was an error loading a Cooldown from the cooldowns file: " + this.cooldownFile.getAbsolutePath(), ex);
                LoggingUtils.severe("The entry at position " + i + " could not be read. To prevent data loss in the plugin, this Cooldown has been prevented from loading. The entry has been left in the file as it is.");
            }
        }

        /*
         * One entry that cannot be read costs every entry in the file, because the file is the
         * only place the cooldowns are. The whole file is kept aside, and the entries that could
         * be read are still loaded.
         */
        if (!contents.unreadable.isEmpty()) {
            JsonFileUtils.backupCorruptFile(pluginDataFolder, this.cooldownFile, DATA_TYPE,
                    contents.unreadable.size() + " of the " + entries.size() + " entries could not be read");
        }

        return contents;
    }

    /**
     * Writes the cooldown file.
     *
     * <p>The file stays a JSON list of cooldowns, which is what it always was. The entries that
     * could not be read are written back unchanged.
     *
     * @param contents the cooldowns to write, and the entries to keep
     * @throws IOException if the file cannot be written
     */
    private void writeCooldownFile(CooldownFile contents) throws IOException {
        final JsonArray entries = new JsonArray();

        for (Cooldown cooldown : contents.cooldowns) {
            entries.add(gson.toJsonTree(cooldown, Cooldown.class));
        }

        for (JsonElement unreadable : contents.unreadable) {
            entries.add(unreadable);
        }

        JsonFileUtils.writeAtomically(this.cooldownFile, gson.toJson(entries));
    }

    private boolean cooldownExists(@NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull List<Cooldown> current) throws IOException {
        return current.stream()
                .filter(c -> c.getCooldownType().getTypeName().equalsIgnoreCase(cooldownType) && c.getCooldownOwner().toString().equals(cooldownOwner))
                .findFirst()
                .orElse(null) != null;
    }

}
