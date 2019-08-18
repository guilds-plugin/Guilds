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

import java.io.IOException;
import java.util.List;

public interface CooldownMySQLProvider extends CooldownProvider {

    @Override
    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS <prefix>cooldowns (\n" +
                    "  `name` VARCHAR(36) NOT NULL,\n" +
                    "  `data` JSON NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE (`id`));"
    )
    void createContainer(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT EXISTS(SELECT 1 FROM <prefix>cooldowns WHERE name = :name)")
    boolean cooldownExists(@Define("prefix") @NotNull String prefix, @Bind("name") @NotNull String name) throws IOException;

    @Override
    @SqlQuery("SELECT * FROM <prefix>cooldowns")
    @RegisterRowMapper(CooldownRowMapper.class)
    List<Cooldown> getAllCooldowns(@Define("prefix") @NotNull String prefix);

    @Override
    @SqlQuery("SELECT * FROM <prefix>cooldowns WHERE name = :name")
    @RegisterRowMapper(CooldownRowMapper.class)
    Cooldown getCooldown(@Define("prefix") @NotNull String prefix, @Bind("name") @NotNull String name) throws IOException;

    @Override
    @SqlUpdate("INSERT INTO <prefix>cooldowns(name, data) VALUES (:name, :data)")
    void createCooldown(@Define("prefix") @NotNull String prefix, @Bind("name") String name, @Bind("data") String data);

    @Override
    @SqlUpdate("UPDATE <prefix>cooldowns SET data = :data WHERE name = :name")
    void updateCooldown(@Define("prefix") @NotNull String prefix, @Bind("name") @NotNull String name, @Bind("data") @NotNull String data) throws IOException;

    @Override
    @SqlUpdate("DELETE FROM <prefix>cooldowns WHERE name = :name")
    void deleteCooldown(@Define("prefix") @NotNull String prefix, @Bind("name") @NotNull String name) throws IOException;

}
