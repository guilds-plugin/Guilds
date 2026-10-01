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
package me.glaremasters.guilds.database.guild;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.guild.Guild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GuildAdapter {
    private final GuildProvider provider;
    private String sqlTablePrefix;

    public GuildAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch (backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "data");
                provider = new GuildJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
            case MARIADB:
                sqlTablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getGuildProvider());
                break;
        }
    }

    public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
    }

    public boolean guildExists(@NotNull String id) throws IOException {
        return provider.guildExists(sqlTablePrefix, id);
    }

    public List<String> getAllGuildIds() throws IOException {
        return provider.getAllGuildIds(sqlTablePrefix);
    }

    public List<Guild> getAllGuilds() throws IOException {
        return provider.getAllGuilds(sqlTablePrefix);
    }

    public Guild getGuild(@NotNull String id) throws IOException {
        return provider.getGuild(sqlTablePrefix, id);
    }

    /**
     * Writes already-serialised guild records.
     *
     * <p>This is the write half of a snapshot taken on the main thread. It takes the JSON rather than
     * a {@link Guild} so that the writer cannot observe a guild being mutated: the payload was fixed
     * when the main thread serialised it, and everything after this point is I/O against an immutable
     * string. The providers take a {@code String} either way, so the bytes on disk are unchanged.
     *
     * @param serialized guild id to serialised guild JSON
     * @throws IOException if an I/O error occurs
     */
    public void saveSerialized(@NotNull Map<String, String> serialized) throws IOException {
        for (Map.Entry<String, String> entry : serialized.entrySet()) {
            if (!guildExists(entry.getKey())) {
                provider.createGuild(sqlTablePrefix, entry.getKey(), entry.getValue());
            } else {
                provider.updateGuild(sqlTablePrefix, entry.getKey(), entry.getValue());
            }
        }
    }

    public void saveGuild(@NotNull Guild guild) throws IOException {
        if (!guildExists(guild.getId().toString())) {
            createGuild(guild);
        } else {
            updateGuild(guild);
        }
    }

    public void createGuild(@NotNull Guild guild) throws IOException {
        provider.createGuild(sqlTablePrefix, guild.getId().toString(), Guilds.getGson().toJson(guild, Guild.class));
    }

    public void updateGuild(@NotNull Guild guild) throws IOException {
        provider.updateGuild(sqlTablePrefix, guild.getId().toString(), Guilds.getGson().toJson(guild, Guild.class));
    }

    public void deleteGuild(@NotNull String id) throws IOException {
        provider.deleteGuild(sqlTablePrefix, id);
    }
}
