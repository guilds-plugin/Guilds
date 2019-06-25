package me.glaremasters.guilds.arena;

import lombok.Getter;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.arenas.ArenasProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class ArenaHandler {

    @Getter private List<Arena> arenas;
    private final ArenasProvider arenasProvider;

    public ArenaHandler(ArenasProvider arenasProvider) {
        this.arenasProvider = arenasProvider;

        Guilds.newChain().async(() -> {
            try {
                arenas = arenasProvider.loadArenas();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).execute();
    }

    /**
     * Save the data of the arenas
     * @throws IOException
     */
    public void saveArenas() throws IOException {
        arenasProvider.saveArenas(arenas);
    }

    /**
     * Add an arena to the data
     * @param arena arena to add
     */
    public void addArena(@NotNull Arena arena) {
        arenas.add(arena);
    }

    /**
     * Remove an arena from the data
     * @param arena arena to remove
     */
    public void removeArena(@NotNull Arena arena) {
        arenas.remove(arena);
    }

    /**
     * Get an arena from the list
     * @param name the name of the arena
     * @return
     */
    public Arena getArena(@NotNull String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
