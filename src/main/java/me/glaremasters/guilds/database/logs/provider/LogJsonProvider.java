package me.glaremasters.guilds.database.logs.provider;

import com.google.gson.Gson;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.logs.LogProvider;
import me.glaremasters.guilds.guild.GuildLog;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LogJsonProvider implements LogProvider {
    private final File dataFolder;
    private Gson gson;

    public LogJsonProvider(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = Guilds.getGson();
    }

    @Override
    public void createContainer(@Nullable String tablePrefix) throws Exception {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdir();
        }
    }

    @Override
    public boolean logExists(@Nullable String tablePrefix, @Nullable String id) throws Exception {
        return false;
    }

    @Override
    public List<GuildLog> getAllLogs(@Nullable String tablePrefix) throws Exception {
        return null;
    }

    @Override
    public GuildLog getLog(@Nullable String tablePrefix, @Nullable String id) throws Exception {
        return null;
    }

    @Override
    public void createLog(@Nullable String tablePrefix, @Nullable GuildLog log) throws Exception {
        // Create the log file
        File file = new File(dataFolder, log.getId() + ".json");
        // Write the log to the file
        Files.write(Paths.get(file.toURI()), gson.toJson(log).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void updateLog(@Nullable String tablePrefix, @Nullable GuildLog log) throws Exception {
        // Update the log file if it exists
        if (logExists(tablePrefix, log.getId())) {
            createLog(tablePrefix, log);
        }
    }

    @Override
    public void deleteLog(@Nullable String tablePrefix, @Nullable String id) throws Exception {

    }
}
