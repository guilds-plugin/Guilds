package me.glaremasters.guilds.arena;

import me.glaremasters.guilds.Guilds;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaHandler {

    private List<Arena> arenas;
    private Guilds guilds;

    public ArenaHandler(Guilds guilds) {
        this.guilds = guilds;

        Guilds.newChain().async(() -> {
            try {
                arenas = guilds.getDatabase().getArenaAdapter().getAllArenas();
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
        guilds.getDatabase().getArenaAdapter().saveArenas(arenas);
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

    /**
     * Get a random open arena
     * @return arena
     */
    public Arena getAvailableArena() {
        List<Arena> availableArenas = getArenas().stream().filter(Objects::nonNull).filter(a -> !a.isInUse()).collect(Collectors.toList());
        if (availableArenas.isEmpty()) {
            return null;
        }
        return availableArenas.get(ThreadLocalRandom.current().nextInt(availableArenas.size()));
    }

    public List<Arena> getArenas() {
        return this.arenas;
    }
}
