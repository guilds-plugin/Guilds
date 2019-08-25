package me.glaremasters.guilds.database.challenges.provider;

import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ChallengeSQLiteProvider extends ChallengeProvider {

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
    @SqlUpdate("INSERT INTO <prefix>challenge(id, data) VALUES (:id, :data)")
    void createChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") String id, @Bind("data") String data);

    @Override
    @SqlUpdate("DELETE FROM <prefix>challenge WHERE id = :id")
    void deleteChallenge(@Define("prefix") @NotNull String prefix, @Bind("id") @NotNull String id) throws IOException;
}
