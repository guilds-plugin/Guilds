package me.glaremasters.guilds.database.logs;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.logs.provider.LogJsonProvider;
import me.glaremasters.guilds.guild.GuildLog;

import java.io.File;
import java.util.List;

public class LogAdapter {
    private final LogProvider provider;
    private String tablePrefix;


    public LogAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch (backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "logs");
                provider = new LogJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
            case MARIADB:
                tablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getLogProvider());
        }
    }

    public void createContainer() throws Exception {
        provider.createContainer(tablePrefix);
    }

    public List<GuildLog> getAllLogs() throws Exception {
        return provider.getAllLogs(tablePrefix);
    }

    public void saveLogs(List<GuildLog> logs) throws Exception {
        for (GuildLog log : logs) {
            saveLog(log);
        }
    }

    // Check if exists
    public boolean logExists(String id) throws Exception {
        return provider.logExists(tablePrefix, id);
    }

    public void saveLog(GuildLog log) throws Exception {
        if (logExists(log.getId())) {
            provider.updateLog(tablePrefix, log);
        } else {
            provider.createLog(tablePrefix, log);
        }
    }
}
