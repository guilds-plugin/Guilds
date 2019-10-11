package me.glaremasters.guilds.database.challenges.provider;

import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.database.challenges.ChallengeRowMapper;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface ChallengeMariaDBProvider extends ChallengeProvider {

    @Override
    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS <prefix>challenge (\n" +
                    "  `id` VARCHAR(36) NOT NULL,\n" +
                    "  `data` JSON NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE (`id`));"
    )
    void createContainer(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT EXISTS(SELECT 1 FROM <prefix>challenge WHERE id = :id)")
    boolean challengeExists(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;

    @Override
    @SqlQuery("SELECT * FROM <prefix>challenge")
    @RegisterRowMapper(ChallengeRowMapper.class)
    List<GuildChallenge> getAllChallenges(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT * FROM <prefix>challenge WHERE id = :id")
    @RegisterRowMapper(ChallengeRowMapper.class)
    GuildChallenge getChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;

    @Override
    @SqlUpdate("INSERT INTO <prefix>challenge(id, data) VALUES (:id, :data)")
    void createChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") String id, @Bind("data") String data);

    @Override
    @SqlUpdate("UPDATE <prefix>challenge SET data = :data WHERE id = :id")
    void updateChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id, @Bind("data") @NotNull String data) throws IOException;

    @Override
    @SqlUpdate("DELETE FROM <prefix>challenge WHERE id = :id")
    void deleteChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;
}
