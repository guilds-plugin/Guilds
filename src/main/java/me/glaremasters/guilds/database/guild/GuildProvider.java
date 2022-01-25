/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface GuildProvider {
    /**
     * Creates the container that will hold guilds
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether or not a guild with the specified id exists
     * @param id the guild id
     * @return true or false
     * @throws IOException
     */
    boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Gets all guild IDs from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of guild ids
     */
    List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets all guilds from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of guilds
     */
    List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a single guild by id
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the guild to load
     * @return the found guild or null
     * @throws IOException
     */
    Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Saves a new Guild to the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the new guild
     * @param data the data of the new guild
     */
    void createGuild(@Nullable String tablePrefix, String id, String data) throws IOException;

    /**
     * Updates a Guild in the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the guild to update
     * @param data the updated data of the guild
     * @throws IOException
     */
    void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    /**
     * Deletes a guild from the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the guild id to delete
     * @throws IOException
     */
    void deleteGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}
