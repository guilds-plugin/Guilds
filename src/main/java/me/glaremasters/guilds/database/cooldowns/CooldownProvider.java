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
package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface CooldownProvider {

    /**
     * Creates the container that will hold cooldowns
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks if a cooldown exists in the database
     * @oaram tablePrefix the prefix, if any, to use
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @return true or false
     */
    boolean cooldownExists(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;

    /**
     * Gets all cooldowns from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of cooldowns
     */
    List<Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException;

    /**
     * Create a new cooldown (with a random ID) in the database
     * @param tablePrefix the table prefix
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @param cooldownExpiry when the cooldown expires in milliseconds
     * @throws IOException
     */
    default void createCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException {
        createCooldown(tablePrefix, UUID.randomUUID().toString(), cooldownType, cooldownOwner, cooldownExpiry);
    }

    /**
     * Create a new cooldown in the database
     * @param tablePrefix the table prefix
     * @param id the UUID id for the cooldown
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @param cooldownExpiry when the cooldown expires in milliseconds
     * @throws IOException
     */
    void createCooldown(@Nullable String tablePrefix, @NotNull String id, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException;

    /**
     * Delete a cooldown from the database
     * @param tablePrefix the table prefix
     * @param cooldownType the type of cooldown
     * @param cooldownOwner the owner UUID of the cooldown
     * @throws IOException
     */
    void deleteCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;
}
