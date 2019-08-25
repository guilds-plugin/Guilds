package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.arena.Arena;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface ArenaProvider {
    /**
     * Creates the container that will hold arena
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether or not an arena with the specified id exists
     * @param id the arena id
     * @return true or false
     * @throws IOException
     */
    boolean arenaExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Gets all arena IDs from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of arena ids
     */
    List<String> getAllArenaIds(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets all arenas from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of arenas
     */
    List<Arena> getAllArenas(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets a single arena by id
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the arena to load
     * @return the found arena or null
     * @throws IOException
     */
    Arena getArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Saves a new arena to the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the new arena
     * @param data the data of the new arena
     */
    void createArena(@Nullable String tablePrefix, String id, String data) throws IOException;

    /**
     * Updates a arena in the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the arena to update
     * @param data the updated data of the arena
     * @throws IOException
     */
    void updateArena(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    /**
     * Deletes a arena from the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the arena id to delete
     * @throws IOException
     */
    void deleteArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}