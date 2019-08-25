package me.glaremasters.guilds.database.cooldowns.provider;

import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.cooldowns.CooldownProvider;
import me.glaremasters.guilds.database.cooldowns.CooldownRowMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public interface CooldownMySQLProvider extends CooldownProvider {
    @Override
    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS <prefix>cooldowns (\n" +
                    "  `id` VARCHAR(36) NOT NULL,\n" +
                    "  `type` VARCHAR(36) NOT NULL,\n" +
                    "  `owner` VARCHAR(36) NOT NULL,\n" +
                    "  `expiry` TIMESTAMP NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE (`id`));"
    )
    void createContainer(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT EXISTS(SELECT 1 FROM <prefix>cooldowns WHERE type = :type AND owner = :owner)")
    boolean cooldownExists(@Define("prefix") @NotNull String tablePrefix, @NotNull @Bind("type") String cooldownType, @NotNull @Bind("owner") String cooldownOwner) throws IOException;

    @Override
    @SqlQuery("SELECT * FROM <prefix>cooldowns")
    @RegisterRowMapper(CooldownRowMapper.class)
    List<Cooldown> getAllCooldowns(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlUpdate("INSERT INTO <prefix>cooldowns(id, type, owner, expiry) VALUES (:id, :type, :owner, :expiry)")
    void createCooldown(@Define("prefix") @NotNull String prefix, @NotNull @Bind("id") String id, @NotNull @Bind("type") String type, @NotNull @Bind("owner") String owner, @NotNull @Bind("expiry") Timestamp expiry);

    @Override
    @SqlUpdate("DELETE FROM <prefix>cooldowns WHERE type = :type AND owner = :owner")
    void deleteCooldown(@Define("prefix") @NotNull String prefix, @NotNull @Bind("type") String type, @NotNull @Bind("owner") String owner) throws IOException;
}
