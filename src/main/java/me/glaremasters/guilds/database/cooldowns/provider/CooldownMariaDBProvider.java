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
import java.sql.Timestamp;
import java.util.List;

public interface CooldownMariaDBProvider extends CooldownProvider {
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
