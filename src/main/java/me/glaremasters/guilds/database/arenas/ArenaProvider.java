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
package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.arena.Arena;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * An interface that provides methods for accessing and managing arenas in the database.
 */
public interface ArenaProvider {

    /**
     * Creates the container that will hold arenas in the database.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @throws IOException if an I/O error occurs while creating the container.
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether an arena with the specified ID exists in the database.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @param id          the ID of the arena to check for.
     * @return `true` if an arena with the specified ID exists, `false` otherwise.
     * @throws IOException if an I/O error occurs while checking for the arena.
     */
    boolean arenaExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Gets a list of all arena IDs from the database.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @return a list of all arena IDs in the database.
     * @throws IOException if an I/O error occurs while retrieving the arena IDs.
     */
    List<String> getAllArenaIds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a list of all arenas from the database.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @return a list of all arenas in the database.
     * @throws IOException if an I/O error occurs while retrieving the arenas.
     */
    List<Arena> getAllArenas(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a single arena from the database by its ID.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @param id          the ID of the arena to retrieve.
     * @return the retrieved arena, or `null` if an arena with the specified ID does not exist.
     * @throws IOException if an I/O error occurs while retrieving the arena.
     */
    Arena getArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Saves a new arena to the database.
     *
     * @param tablePrefix the prefix, if any, to use for the container.
     * @param id          the ID of the new arena.
     * @param data        the data of the new arena.
     * @throws IOException if an I/O error occurs while saving the new arena.
     */
    void createArena(@Nullable String tablePrefix, String id, String data) throws IOException;

    /**
     * Updates an arena in the database
     *
     * @param tablePrefix the prefix, if any, to be used
     * @param id          the non-null id of the arena to be updated
     * @param data        the non-null updated data of the arena
     * @throws IOException if an I/O error occurs
     */
    void updateArena(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    /**
     * Deletes an arena from the database
     *
     * @param tablePrefix the prefix, if any, to be used
     * @param id          the non-null id of the arena to be deleted
     * @throws IOException if an I/O error occurs
     */
    void deleteArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}