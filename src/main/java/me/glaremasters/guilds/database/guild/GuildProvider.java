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
 * A provider interface for guild data management.
 */
public interface GuildProvider {

    /**
     * Creates the container that will hold guilds.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @throws IOException if an error occurs while creating the container.
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether a guild with the specified id exists.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @param id          the id of the guild to check.
     * @return true if the guild exists, false otherwise.
     * @throws IOException if an error occurs while checking for the guild.
     */
    boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Gets all guild IDs from the database.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @return a list of all guild IDs in the database.
     * @throws IOException if an error occurs while retrieving the guild IDs.
     */

    List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets all guilds from the database.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @return a list of all guilds in the database.
     * @throws IOException if an error occurs while retrieving the guilds.
     */
    List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a single guild by id.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @param id          the id of the guild to retrieve.
     * @return the found guild or null if no guild with the specified id was found.
     * @throws IOException if an error occurs while retrieving the guild.
     */
    Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Saves a new Guild to the database.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @param id          the id of the new guild.
     * @param data        the data of the new guild.
     * @throws IOException if an error occurs while saving the guild.
     */
    void createGuild(@Nullable String tablePrefix, String id, String data) throws IOException;

    /**
     * Updates a Guild in the database.
     *
     * @param tablePrefix the prefix, if any, to use.
     * @param id          the id of the guild to update.
     * @param data        the updated data of the guild.
     * @throws IOException if an error occurs while updating the guild.
     */
    void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    /**
     * Deletes a guild from the database.
     *
     * @param tablePrefix the prefix, if any, to use
     * @param id          the guild id to delete
     * @throws IOException if an I/O error occurs while deleting the guild
     */
    void deleteGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}
