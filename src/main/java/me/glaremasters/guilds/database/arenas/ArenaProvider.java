package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.arena.Arena;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Stores arenas in whichever backend is configured.
 */
public interface ArenaProvider {

    /** Creates the arena table, or the arena data folder on the JSON backend. */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    boolean arenaExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    List<String> getAllArenaIds(@Nullable String tablePrefix) throws IOException;

    List<Arena> getAllArenas(@Nullable String tablePrefix) throws IOException;

    /**
     * Returns null when no arena has that id, so callers have to null-check even though the
     * return type carries no annotation.
     */
    Arena getArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /** Stores a new arena; data is the arena serialised by Gson. */
    void createArena(@Nullable String tablePrefix, String id, String data) throws IOException;

    /** Replaces an existing arena; data is the arena serialised by Gson. */
    void updateArena(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    void deleteArena(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}
