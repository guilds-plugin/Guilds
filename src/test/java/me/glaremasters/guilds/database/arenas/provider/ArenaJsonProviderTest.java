package me.glaremasters.guilds.database.arenas.provider;

import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.CorruptBackups;
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
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which files the JSON arena storage treats as arenas, and what happens to the ones it
 * cannot read.
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
        Files.createDirectories(dataFolder());
        provider = new ArenaJsonProvider(dataFolder().toFile(), pluginFolder().toFile());

        write(ARENA_ID + ".json", arenaJson(ARENA_ID, "The Real Arena"));
        write(ARENA_ID + ".json" + "1234567890" + ".tmp", arenaJson(ARENA_ID, "The Real Arena"));
        write("notes.txt", "this is not an arena");
        write("README", "nothing to see here");

        Files.createDirectory(dataFolder().resolve("backup"));
        write(dataFolder().resolve("backup").resolve("old-arena.json"), arenaJson(ARENA_ID, "Old Arena"));
    }

    /**
     * @return the plugin's data folder, the one the backups are kept in
     */
    private Path pluginFolder() {
        return tempDir;
    }

    /**
     * @return the folder the arena files live in
     */
    private Path dataFolder() {
        return tempDir.resolve("arenas");
    }

    private void write(String name, String content) throws IOException {
        write(dataFolder().resolve(name), content);
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

        assertTrue(Files.exists(dataFolder().resolve(ARENA_ID + ".json")));
        assertTrue(Files.exists(dataFolder().resolve(ARENA_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(dataFolder().resolve("notes.txt")));
        assertTrue(Files.exists(dataFolder().resolve("README")));
        assertTrue(Files.isDirectory(dataFolder().resolve("backup")));
        assertTrue(Files.exists(dataFolder().resolve("backup").resolve("old-arena.json")));
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

        assertTrue(Files.exists(dataFolder().resolve("broken-arena.json")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(dataFolder().resolve("broken-arena.json")), StandardCharsets.UTF_8));
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

        assertTrue(Files.exists(dataFolder().resolve("no-id-arena.json")));
    }

    @Test
    @DisplayName("an arena file that could not be read is not written over")
    void anArenaFileThatCouldNotBeReadIsNotWrittenOver() throws IOException {
        write("broken-arena.json", "{ this is not json");

        provider.getAllArenas(null);

        assertThrows(IOException.class, () -> provider.updateArena(null, "broken-arena", arenaJson(ARENA_ID, "Replaced")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(dataFolder().resolve("broken-arena.json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("an arena file that could not be read is not deleted")
    void anArenaFileThatCouldNotBeReadIsNotDeleted() throws IOException {
        write("broken-arena.json", "{ this is not json");

        provider.getAllArenas(null);

        // The arena storage deletes the files it has no record for, so a file it could not read
        // has to survive that pass.
        provider.deleteArena(null, "broken-arena");
        assertTrue(Files.exists(dataFolder().resolve("broken-arena.json")));
    }

    @Test
    @DisplayName("an arena file without an id is not deleted")
    void anArenaFileWithoutAnIdIsNotDeleted() throws IOException {
        write("no-id-arena.json", "{ \"name\": \"No Id Here\" }");

        provider.getAllArenas(null);

        provider.deleteArena(null, "no-id-arena");
        assertTrue(Files.exists(dataFolder().resolve("no-id-arena.json")));
    }

    @Test
    @DisplayName("an arena file that cannot be read is backed up byte for byte")
    void aBrokenArenaFileIsBackedUpByteForByte() throws IOException {
        final String corrupt = "{ this is not json";
        write("broken-arena.json", corrupt);

        provider.getAllArenas(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "arenas");
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));

        assertTrue(Files.exists(dataFolder().resolve("broken-arena.json")));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("broken-arena.json")));
    }

    @Test
    @DisplayName("an arena file without an id is backed up byte for byte")
    void anArenaFileWithoutAnIdIsBackedUpByteForByte() throws IOException {
        final String unusable = "{ \"name\": \"No Id Here\" }";
        write("no-id-arena.json", unusable);

        provider.getAllArenas(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "arenas");
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("no-id-arena.json")));
    }

    @Test
    @DisplayName("the arena backup lands outside the data folder and keeps the file name")
    void theBackupLandsOutsideTheDataFolder() throws IOException {
        write("broken-arena.json", "{ this is not json");

        provider.getAllArenas(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "arenas");

        assertEquals("broken-arena.json", backup.getFileName().toString());
        assertEquals("arenas", backup.getParent().getFileName().toString());
        assertTrue(backup.getParent().getParent().getFileName().toString().matches("\\d{8}-\\d{6}"),
                "the backup folder is not named after a timestamp: " + backup);
        assertEquals("corrupt", backup.getParent().getParent().getParent().getFileName().toString());
        assertEquals("backups", backup.getParent().getParent().getParent().getParent().getFileName().toString());
        assertEquals(pluginFolder(), backup.getParent().getParent().getParent().getParent().getParent());
        assertFalse(backup.startsWith(dataFolder()));
    }

    @Test
    @DisplayName("the log names the arena file, the reason and the backup")
    void theLogNamesTheFileTheReasonAndTheBackup() throws IOException {
        write("broken-arena.json", "{ this is not json");

        try (LogCapture log = new LogCapture()) {
            provider.getAllArenas(null);

            assertTrue(log.contains("broken-arena.json"), "the log did not name the file");
            assertTrue(log.contains("could not be read"), "the log did not say what went wrong");
            assertTrue(log.contains("backups"), "the log did not say where the backup is");
        }
    }

    @Test
    @DisplayName("two corrupt arena files with the same name do not overwrite each other")
    void twoCorruptArenaFilesWithTheSameNameDoNotOverwriteEachOther() throws IOException {
        final Path file = dataFolder().resolve("broken-arena.json");
        final String first = "{ this is not json";
        write("broken-arena.json", first);
        final long modified = Files.getLastModifiedTime(file).toMillis();

        provider.getAllArenas(null);

        final String second = "{ this is not json either";
        write("broken-arena.json", second);
        Files.setLastModifiedTime(file, FileTime.fromMillis(modified));

        provider.getAllArenas(null);

        final List<Path> backups = CorruptBackups.backupFiles(pluginFolder(), "arenas");
        assertEquals(2, backups.size());
        assertEquals(2, backups.stream().map(path -> path.getFileName().toString()).distinct().count());
        assertTrue(backups.stream().anyMatch(path -> {
            try {
                return Arrays.equals(first.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(path));
            } catch (IOException ex) {
                return false;
            }
        }), "the first backup was overwritten");
        assertTrue(backups.stream().anyMatch(path -> {
            try {
                return Arrays.equals(second.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(path));
            } catch (IOException ex) {
                return false;
            }
        }), "the second backup was not written");
    }

    @Test
    @DisplayName("an arena backup that cannot be written is reported and nothing is lost")
    void aBackupThatCannotBeWrittenIsReportedAndNothingIsLost() throws IOException {
        final String corrupt = "{ this is not json";
        write("broken-arena.json", corrupt);

        Files.createFile(pluginFolder().resolve("backups"));

        try (LogCapture log = new LogCapture()) {
            provider.getAllArenas(null);

            assertTrue(log.contains("could not be written"), "the failure to back the file up was not reported");
            assertTrue(log.contains("broken-arena.json"), "the log did not name the file");
        }

        assertTrue(Files.exists(dataFolder().resolve("broken-arena.json")));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("broken-arena.json")));
        assertEquals(1, provider.getAllArenas(null).size());
    }

    @Test
    @DisplayName("the same arena corruption is backed up once, not once per read")
    void theSameCorruptionIsBackedUpOnce() throws IOException {
        write("broken-arena.json", "{ this is not json");

        provider.getAllArenas(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "arenas").size());

        provider.getAllArenas(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "arenas").size());

        final ArenaJsonProvider restarted = new ArenaJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        restarted.getAllArenas(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "arenas").size());
    }
}
