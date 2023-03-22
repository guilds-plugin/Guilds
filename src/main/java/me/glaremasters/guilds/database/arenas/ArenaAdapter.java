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

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
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

        for (Arena arena : arenas) {
            saveArena(arena);
            savedIds.add(arena.getId().toString());
        }

        for (String arenaId : getAllArenaIds()) {
            boolean keep = savedIds.stream().anyMatch(id -> id.equals(arenaId));
            if (!keep) {
                deleteArena(arenaId);
            }
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
