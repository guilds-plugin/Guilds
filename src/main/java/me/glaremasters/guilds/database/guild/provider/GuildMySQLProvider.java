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

public interface GuildMySQLProvider extends GuildProvider {
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
