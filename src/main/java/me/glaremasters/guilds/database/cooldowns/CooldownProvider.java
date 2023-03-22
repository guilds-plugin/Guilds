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
package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Provides methods for accessing and manipulating cooldowns in the database.
 */
public interface CooldownProvider {

    /**
     * Creates the container that will hold the cooldowns.
     *
     * @param tablePrefix the prefix to be used for the table name, if any
     * @throws IOException if there is an error creating the container
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks if a cooldown with the given type and owner exists in the database.
     *
     * @param tablePrefix   the prefix to be used for the table name, if any
     * @param cooldownType  the type of the cooldown
     * @param cooldownOwner the owner of the cooldown as a UUID
     * @return `true` if a cooldown with the given type and owner exists, `false` otherwise
     * @throws IOException if there is an error checking for the cooldown
     */
    boolean cooldownExists(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;

    /**
     * Retrieves all cooldowns from the database.
     *
     * @param tablePrefix the prefix to be used for the table name, if any
     * @return a list of all the cooldowns in the database
     * @throws IOException if there is an error retrieving the cooldowns
     */
    List<Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException;

    /**
     * Creates a new cooldown with a random UUID in the database.
     *
     * @param tablePrefix    the prefix to be used for the table name, if any
     * @param cooldownType   the type of the cooldown
     * @param cooldownOwner  the owner of the cooldown as a UUID
     * @param cooldownExpiry the time when the cooldown will expire in milliseconds since the epoch
     * @throws IOException if there is an error creating the cooldown
     */
    default void createCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException {
        createCooldown(tablePrefix, UUID.randomUUID().toString(), cooldownType, cooldownOwner, cooldownExpiry);
    }

    /**
     * Creates a new cooldown with the given UUID in the database.
     *
     * @param tablePrefix    the prefix to be used for the table name, if any
     * @param id             the UUID for the cooldown
     * @param cooldownType   the type of the cooldown
     * @param cooldownOwner  the owner of the cooldown as a UUID
     * @param cooldownExpiry the time when the cooldown will expire in milliseconds since the epoch
     * @throws IOException if there is an error creating the cooldown
     */
    void createCooldown(@Nullable String tablePrefix, @NotNull String id, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException;

    /**
     * Delete a cooldown from the database
     *
     * @param tablePrefix   the table prefix
     * @param cooldownType  the type of cooldown
     * @param cooldownOwner the owner UUID of the cooldown
     * @throws IOException if there is an error when deleting the cooldown from the database
     */
    void deleteCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;
}
