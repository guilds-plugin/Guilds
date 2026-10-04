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

import me.glaremasters.guilds.guild.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Stores guilds in whichever backend is configured.
 */
public interface GuildProvider {

    /** Creates the guild table, or the guild data folder on the JSON backend. */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException;

    List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException;

    /** Returns null when no guild has that id, so callers have to null-check. */
    Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /** Stores a new guild; data is the guild serialised by Gson. */
    void createGuild(@Nullable String tablePrefix, String id, String data) throws IOException;

    /** Replaces an existing guild; data is the guild serialised by Gson. */
    void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    void deleteGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}
