/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
        List<Arena> availableArenas = getArenas().stream().filter(Objects::nonNull).filter(a -> !a.getInUse()).collect(Collectors.toList());
        if (availableArenas.isEmpty()) {
            return null;
        }
        return availableArenas.get(ThreadLocalRandom.current().nextInt(availableArenas.size()));
    }

    public List<Arena> getArenas() {
        return this.arenas;
    }
}
