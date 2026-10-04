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
