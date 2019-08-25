package me.glaremasters.guilds.database.guild.provider;

import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.database.guild.GuildRowMapper;
import me.glaremasters.guilds.guild.Guild;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface GuildSQLiteProvider extends GuildProvider {
    @Override
    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS <prefix>guild (\n" +
                    "  `id` VARCHAR(36) NOT NULL,\n" +
                    "  `data` JSON NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE (`id`));"
    )
    void createContainer(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT EXISTS(SELECT 1 FROM <prefix>guild WHERE id = :id)")
    boolean guildExists(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;

    @Override
    @SqlQuery("SELECT id FROM <prefix>guild")
    List<String> getAllGuildIds(@Define("prefix") @NotNull String tablePrefix) throws IOException;

    @Override
    @SqlQuery("SELECT * FROM <prefix>guild")
    @RegisterRowMapper(GuildRowMapper.class)
    List<Guild> getAllGuilds(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT * FROM <prefix>guild WHERE id = :id")
    @RegisterRowMapper(GuildRowMapper.class)
    Guild getGuild(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;

    @Override
    @SqlUpdate("INSERT INTO <prefix>guild(id, data) VALUES (:id, :data)")
    void createGuild(@Define("prefix") @NotNull String prefix, @Bind("id") String id, @Bind("data") String data);

    @Override
    @SqlUpdate("UPDATE <prefix>guild SET data = :data WHERE id = :id")
    void updateGuild(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id, @Bind("data") @NotNull String data) throws IOException;

    @Override
    @SqlUpdate("DELETE FROM <prefix>guild WHERE id = :id")
    void deleteGuild(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;
}
