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
package me.glaremasters.guilds.arena;

import me.glaremasters.guilds.Guilds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers arena name key normalization.
 *
 * <p>{@code ArenaHandler} keyed its map on {@code name.lowercase(Locale.getDefault())} when adding
 * and looking up, but removed on the raw name. Any arena with an uppercase letter could be found
 * and could never be deleted, which left it permanently reserved and reported as "all arenas full".
 *
 * <p>{@code Locale.getDefault()} was a second, quieter bug: on a Turkish-locale JVM a capital "I"
 * lowercases to a dotless "ı", so the same arena name could resolve on one path and miss on another.
 * The tests below pin the behaviour under a Turkish default locale.
 */
class ArenaHandlerTest {

    /**
     * A bare handler. The Guilds plugin instance is only touched by loadArenas and saveArenas, which
     * these tests do not call, and the field is a non-null Kotlin type, so a mock is the only way to
     * build a handler without a server.
     */
    private ArenaHandler handler() {
        return new ArenaHandler(Mockito.mock(Guilds.class));
    }

    private Locale originalLocale;

    @BeforeEach
    void captureLocale() {
        originalLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    private static Arena arena(String name) {
        return new Arena(UUID.randomUUID(), name);
    }

    private static Optional<Arena> lookup(ArenaHandler handler, String name) {
        return handler.getArena(name);
    }

    @Test
    @DisplayName("add, get, and remove agree on the key for a mixed-case name")
    void addGetRemoveAgree() {
        final ArenaHandler handler = handler();
        final Arena arena = arena("Colosseum");

        handler.addArena(arena);

        assertTrue(lookup(handler, "Colosseum").isPresent());
        assertTrue(lookup(handler, "colosseum").isPresent());
        assertTrue(lookup(handler, "COLOSSEUM").isPresent());
        assertEquals(1, handler.getArenas().size());

        handler.removeArena(arena);

        // This is the regression: the arena stayed in the map forever.
        assertFalse(lookup(handler, "Colosseum").isPresent());
        assertTrue(handler.getArenas().isEmpty());
    }

    @Test
    @DisplayName("a name containing a dotted capital I survives a Turkish default locale")
    void turkishLocaleDoesNotSplitTheKey() {
        Locale.setDefault(new Locale("tr", "TR"));

        final ArenaHandler handler = handler();
        final Arena arena = arena("ISTANBUL");
        handler.addArena(arena);

        // With Locale.getDefault() the key was "ıstanbul" and a lookup for "ISTANBUL" missed,
        // because "ISTANBUL".lowercase(tr) is "ıstanbul" but "Istanbul".lowercase(tr) is also
        // "ıstanbul" while "ISTANBUL" alone never matched a mixed-case spelling.
        assertTrue(lookup(handler, "ISTANBUL").isPresent());
        assertTrue(lookup(handler, "istanbul").isPresent());
        assertTrue(lookup(handler, "Istanbul").isPresent());

        handler.removeArena(arena);
        assertTrue(handler.getArenas().isEmpty());
    }

    @Test
    @DisplayName("an unknown name resolves to an empty optional rather than throwing")
    void unknownName() {
        final ArenaHandler handler = handler();
        handler.addArena(arena("Colosseum"));

        assertFalse(lookup(handler, "Nowhere").isPresent());
    }

    @Test
    @DisplayName("arena names are reported in their original casing")
    void arenaNamesKeepOriginalCasing() {
        final ArenaHandler handler = handler();
        handler.addArena(arena("Colosseum"));

        assertEquals(1, handler.arenaNames().size());
        assertEquals("Colosseum", handler.arenaNames().get(0));
    }
}
