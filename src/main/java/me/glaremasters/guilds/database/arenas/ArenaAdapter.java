package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class serves as an adapter between the {@link Arena} and the underlying data storage mechanism (either JSON or SQL)
 */
public class ArenaAdapter {
    private final ArenaProvider provider;
    private String sqlTablePrefix;

    /**
     * Constructs a new {@link ArenaAdapter}
     *
     * @param guilds  the main {@link Guilds} instance
     * @param adapter the {@link DatabaseAdapter} instance that manages the data storage mechanism for the plugin
     */
    public ArenaAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch (backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "arenas");
                provider = new ArenaJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
            case MARIADB:
                sqlTablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getArenaProvider());
        }
    }

    /**
     * Creates a new container for arenas in the data storage mechanism
     *
     * @throws IOException if an I/O error occurs
     */
    public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
    }

    /**
     * Checks if an arena with the given ID exists in the data storage mechanism
     *
     * @param id the ID of the arena to check for
     * @return {@code true} if an arena with the given ID exists, {@code false} otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean arenaExists(@NotNull String id) throws IOException {
        return provider.arenaExists(sqlTablePrefix, id);
    }

    /**
     * Gets a list of all arena IDs in the data storage mechanism
     *
     * @return a list of all arena IDs
     * @throws IOException if an I/O error occurs
     */
    public List<String> getAllArenaIds() throws IOException {
        return provider.getAllArenaIds(sqlTablePrefix);
    }

    /**
     * Gets a list of all arenas in the data storage mechanism
     *
     * @return a list of all arenas
     * @throws IOException if an I/O error occurs
     */
    public List<Arena> getAllArenas() throws IOException {
        return provider.getAllArenas(sqlTablePrefix);
    }

    /**
     * Saves all the arenas in the collection to the data storage backend.
     * Any arena in the data storage backend that is not present in the collection will be deleted.
     *
     * @param arenas a collection of arenas to be saved
     * @throws IOException if an I/O error occurs
     */
    public void saveArenas(@NotNull Collection<Arena> arenas) throws IOException {
        List<String> savedIds = new ArrayList<>();
        int failed = 0;

        for (Arena arena : arenas) {
            try {
                saveArena(arena);
            } catch (IOException | RuntimeException e) {
                // The id is still recorded, and must stay recorded: the delete pass below removes any
                // row not in savedIds, so dropping it here would delete the arena's existing row
                // because its update failed.
                failed++;
                LoggingUtils.warn("Failed to save arena " + arena.getId() + "; the other arenas are still being saved.", e);
            }

            savedIds.add(arena.getId().toString());
        }

        for (String arenaId : getAllArenaIds()) {
            boolean keep = savedIds.stream().anyMatch(id -> id.equals(arenaId));
            if (!keep) {
                deleteArena(arenaId);
            }
        }

        if (failed > 0) {
            LoggingUtils.severe(failed + " of " + arenas.size() + " arenas failed to save. See the warnings above.");
        }

        savedIds.clear();
    }

    /**
     * Saves an arena to the data storage backend. If the arena already exists, it will be updated.
     * If not, a new arena will be created.
     *
     * @param arena the arena to be saved
     * @throws IOException if an I/O error occurs
     */
    public void saveArena(@NotNull Arena arena) throws IOException {
        if (!arenaExists(arena.getId().toString())) {
            createArena(arena);
        } else {
            updateArena(arena);
        }
    }

    /**
     * Creates a new arena in the data storage backend.
     *
     * @param arena the arena to be created
     * @throws IOException if an I/O error occurs
     */
    public void createArena(@NotNull Arena arena) throws IOException {
        provider.createArena(sqlTablePrefix, arena.getId().toString(), Guilds.getGson().toJson(arena, Arena.class));
    }

    /**
     * Updates an existing arena in the data storage backend.
     *
     * @param arena the arena to be updated
     * @throws IOException if an I/O error occurs
     */
    public void updateArena(@NotNull Arena arena) throws IOException {
        provider.updateArena(sqlTablePrefix, arena.getId().toString(), Guilds.getGson().toJson(arena, Arena.class));
    }

    /**
     * Deletes an arena from the data storage backend.
     *
     * @param id the ID of the arena to be deleted
     * @throws IOException if an I/O error occurs
     */
    public void deleteArena(@NotNull String id) throws IOException {
        provider.deleteArena(sqlTablePrefix, id);
    }
}
