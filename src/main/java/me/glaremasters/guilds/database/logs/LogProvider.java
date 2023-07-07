package me.glaremasters.guilds.database.logs;

import me.glaremasters.guilds.guild.GuildLog;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LogProvider {

    void createContainer(@Nullable String tablePrefix) throws Exception;

    boolean logExists(@Nullable String tablePrefix, @Nullable String id) throws Exception;

    List<GuildLog> getAllLogs(@Nullable String tablePrefix) throws Exception;

    GuildLog getLog(@Nullable String tablePrefix, @Nullable String id) throws Exception;

    void createLog(@Nullable String tablePrefix, @Nullable GuildLog log) throws Exception;

    void updateLog(@Nullable String tablePrefix, @Nullable GuildLog log) throws Exception;

    void deleteLog(@Nullable String tablePrefix, @Nullable String id) throws Exception;
}
