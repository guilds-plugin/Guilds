package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public interface CooldownProvider {

    /**
     * Creates the container that will hold cooldowns
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether or not a cooldown with the specified same exists
     * @param name the cooldown name
     * @return true or false
     * @throws IOException
     */
    boolean cooldownExists(@Nullable String tablePrefix, @NotNull String name) throws IOException;

    /**
     * Gets all cooldowns from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of cooldowns
     */
    Map<String, Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a single cooldown by name
     * @param tablePrefix the prefix, if any, to use
     * @param name the name of the cooldown to load
     * @return the found cooldown or null
     * @throws IOException
     */
    Cooldown getCooldown(@Nullable String tablePrefix, @NotNull String name) throws IOException;

    /**
     * Create a new cooldown
     * @param tablePrefix the table prefix
     * @param name name of cooldown
     * @param data data of cooldown
     * @throws IOException
     */
    void createCooldown(@Nullable String tablePrefix, String name, String data) throws IOException;

    /**
     * Update an existing cooldown with new data
     * @param tablePrefix the table prefix
     * @param name the name of the cooldown
     * @param data data of cooldown
     * @throws IOException
     */
    void updateCooldown(@Nullable String tablePrefix, @NotNull String name, @NotNull String data) throws IOException;

    /**
     * Delete a cooldown from the database
     * @param tablePrefix the table prefix
     * @param name the name of the cooldown
     * @throws IOException
     */
    void deleteCooldown(@Nullable String tablePrefix, @NotNull String name) throws IOException;

}
