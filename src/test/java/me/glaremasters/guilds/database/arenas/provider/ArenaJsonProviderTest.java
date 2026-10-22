package me.glaremasters.guilds.database.arenas.provider;

import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.LogCapture;
import me.glaremasters.guilds.database.TestGson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which files the JSON arena storage treats as arenas.
 *
 * <p>The arena folder has the same problem the guild folder had: everything in it was taken to be
 * an arena. See {@code GuildJsonProviderTest} for the full story.
 */
class ArenaJsonProviderTest {

    private static final String ARENA_ID = "33333333-3333-3333-3333-333333333333";

    @TempDir
    Path tempDir;

    private ArenaJsonProvider provider;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        provider = new ArenaJsonProvider(tempDir.toFile());

        write(ARENA_ID + ".json", arenaJson(ARENA_ID, "The Real Arena"));
        write(ARENA_ID + ".json" + "1234567890" + ".tmp", arenaJson(ARENA_ID, "The Real Arena"));
        write("notes.txt", "this is not an arena");
        write("README", "nothing to see here");

        Files.createDirectory(tempDir.resolve("backup"));
        write(tempDir.resolve("backup").resolve("old-arena.json"), arenaJson(ARENA_ID, "Old Arena"));
    }

    private void write(String name, String content) throws IOException {
        write(tempDir.resolve(name), content);
    }

    private void write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String arenaJson(String id, String name) {
        return "{\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"name\": \"" + name + "\",\n"
                + "  \"challenger\": null,\n"
                + "  \"defender\": null\n"
                + "}\n";
    }

    @Test
    @DisplayName("only the arena data files are enumerated")
    void onlyDataFilesAreEnumerated() throws IOException {
        assertEquals(List.of(ARENA_ID), provider.getAllArenaIds(null));
    }

    @Test
    @DisplayName("only the arena data files are loaded as arenas")
    void onlyDataFilesAreLoaded() throws IOException {
        final List<Arena> arenas = provider.getAllArenas(null);

        assertEquals(1, arenas.size());
        assertEquals(ARENA_ID, arenas.get(0).getId().toString());
        assertEquals("The Real Arena", arenas.get(0).getName());
    }

    @Test
    @DisplayName("a file that is not an arena file is not reported as an error")
    void ignoredFilesAreNotReportedAsErrors() throws IOException {
        try (LogCapture log = new LogCapture()) {
            provider.getAllArenas(null);

            assertFalse(log.contains("notes.txt"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("README"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("backup"), "the plugin complained about a folder it should ignore");
        }
    }

    @Test
    @DisplayName("only the arena data files count as an existing arena")
    void onlyDataFilesExist() throws IOException {
        assertTrue(provider.arenaExists(null, ARENA_ID));
        assertFalse(provider.arenaExists(null, "notes"));
        assertFalse(provider.arenaExists(null, "README"));
        assertFalse(provider.arenaExists(null, "backup"));
        assertFalse(provider.arenaExists(null, "old-arena"));
    }

    @Test
    @DisplayName("an arena is only read from an arena data file")
    void onlyDataFilesAreRead() throws IOException {
        final Arena arena = provider.getArena(null, ARENA_ID);

        assertNotNull(arena);
        assertEquals(ARENA_ID, arena.getId().toString());
        assertEquals("The Real Arena", arena.getName());

        assertNull(provider.getArena(null, "notes"));
        assertNull(provider.getArena(null, "README"));
        assertNull(provider.getArena(null, "old-arena"));
    }

    @Test
    @DisplayName("the files the plugin ignores are left on disk")
    void ignoredFilesAreLeftAlone() throws IOException {
        provider.getAllArenas(null);
        provider.getAllArenaIds(null);
        provider.arenaExists(null, ARENA_ID);
        provider.getArena(null, ARENA_ID);

        assertTrue(Files.exists(tempDir.resolve(ARENA_ID + ".json")));
        assertTrue(Files.exists(tempDir.resolve(ARENA_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(tempDir.resolve("notes.txt")));
        assertTrue(Files.exists(tempDir.resolve("README")));
        assertTrue(Files.isDirectory(tempDir.resolve("backup")));
        assertTrue(Files.exists(tempDir.resolve("backup").resolve("old-arena.json")));
    }

    @Test
    @DisplayName("an arena file that cannot be parsed does not take the other arenas with it")
    void aBrokenArenaFileDoesNotTakeTheOthersWithIt() throws IOException {
        write("broken-arena.json", "{ this is not json");

        final List<Arena> arenas = provider.getAllArenas(null);

        assertEquals(1, arenas.size());
        assertEquals(ARENA_ID, arenas.get(0).getId().toString());
    }

    @Test
    @DisplayName("an arena file that cannot be parsed is left on disk")
    void aBrokenArenaFileIsLeftOnDisk() throws IOException {
        write("broken-arena.json", "{ this is not json");

        provider.getAllArenas(null);

        assertTrue(Files.exists(tempDir.resolve("broken-arena.json")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(tempDir.resolve("broken-arena.json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("an arena file without an id does not take the other arenas with it")
    void anArenaFileWithoutAnIdDoesNotTakeTheOthersWithIt() throws IOException {
        write("no-id-arena.json", "{ \"name\": \"No Id Here\" }");

        final List<Arena> arenas = provider.getAllArenas(null);

        assertEquals(1, arenas.size());
        assertEquals(ARENA_ID, arenas.get(0).getId().toString());
    }

    @Test
    @DisplayName("an arena file without an id is left on disk")
    void anArenaFileWithoutAnIdIsLeftOnDisk() throws IOException {
        write("no-id-arena.json", "{ \"name\": \"No Id Here\" }");

        provider.getAllArenas(null);

        assertTrue(Files.exists(tempDir.resolve("no-id-arena.json")));
    }
}
