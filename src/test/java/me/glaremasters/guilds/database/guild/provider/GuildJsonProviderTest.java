package me.glaremasters.guilds.database.guild.provider;

import me.glaremasters.guilds.database.CorruptBackups;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.LogCapture;
import me.glaremasters.guilds.database.TestGson;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.Guild;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers which files the JSON guild storage treats as guilds, and what happens to the ones it
 * cannot read.
 *
 * <p>The guild data folder is a folder an operator can see and touch. Anything can end up in it:
 * a leftover temporary file from a write that was interrupted, a note, a backup folder. The
 * provider used to take every entry it found and call it a guild, which let a stray file stand in
 * for a real guild and made the plugin report a guild that did not exist.
 *
 * <p>A file the plugin cannot read is a different matter. It is the only copy of that guild's
 * data, so it is copied aside and left exactly where it was.
 *
 * <p>And a folder where nothing at all can be read is not an empty guild database. It is a guild
 * database the plugin failed to read, and a server that comes up as if it had no guilds would let
 * the first save write over them.
 */
class GuildJsonProviderTest {

    private static final String GUILD_ID = "11111111-1111-1111-1111-111111111111";
    private static final String OTHER_GUILD_ID = "22222222-2222-2222-2222-222222222222";

    /** A guild file that does not parse. */
    private static final String BROKEN_CONTENT = "{ this is not json";

    /** A guild file that parses but carries no id. */
    private static final String NO_ID_CONTENT = "{ \"name\": \"No Id Here\" }";

    @TempDir
    Path tempDir;

    private GuildJsonProvider provider;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(dataFolder());
        provider = new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile());

        // The one real guild.
        write(GUILD_ID + ".json", guildJson(GUILD_ID, "The Real Guild"));

        // What a write that was killed half way leaves behind: the plugin writes to a temporary
        // file next to the target and moves it into place afterwards.
        write(GUILD_ID + ".json" + "1234567890" + ".tmp", guildJson(GUILD_ID, "The Real Guild"));

        // Files an operator dropped in.
        write("notes.txt", "this is not a guild");
        write("README", "nothing to see here");

        // A folder, because people make backups that way.
        Files.createDirectory(dataFolder().resolve("backup"));
        write(dataFolder().resolve("backup").resolve("old-guild.json"), guildJson(OTHER_GUILD_ID, "Old Guild"));
    }

    /**
     * @return the plugin's data folder, the one the backups are kept in
     */
    private Path pluginFolder() {
        return tempDir;
    }

    /**
     * @return the folder the guild files live in
     */
    private Path dataFolder() {
        return tempDir.resolve("data");
    }

    private void write(String name, String content) throws IOException {
        write(dataFolder().resolve(name), content);
    }

    private void write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String guildJson(String id, String name) {
        return "{\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"name\": \"" + name + "\",\n"
                + "  \"prefix\": \"TG\",\n"
                + "  \"motd\": \"A guild\",\n"
                + "  \"status\": \"Public\",\n"
                + "  \"balance\": 0.0,\n"
                + "  \"members\": [],\n"
                + "  \"invitedMembers\": [],\n"
                + "  \"allies\": [],\n"
                + "  \"pendingAllies\": [],\n"
                + "  \"codes\": [],\n"
                + "  \"vaults\": [],\n"
                + "  \"lastDefended\": 0,\n"
                + "  \"creationDate\": 0\n"
                + "}\n";
    }

    /**
     * @return an adapter over the same folder the provider reads, so a save goes through the
     *         production path rather than through the provider directly
     */
    private GuildAdapter adapter() {
        final me.glaremasters.guilds.Guilds plugin = mock(me.glaremasters.guilds.Guilds.class);
        when(plugin.getDataFolder()).thenReturn(pluginFolder().toFile());

        final me.glaremasters.guilds.database.DatabaseAdapter database =
                mock(me.glaremasters.guilds.database.DatabaseAdapter.class);
        when(database.getBackend()).thenReturn(DatabaseBackend.JSON);

        return new GuildAdapter(plugin, database);
    }

    @Test
    @DisplayName("only the guild data files are enumerated")
    void onlyDataFilesAreEnumerated() throws IOException {
        assertEquals(List.of(GUILD_ID), provider.getAllGuildIds(null));
    }

    @Test
    @DisplayName("only the guild data files are loaded as guilds")
    void onlyDataFilesAreLoaded() throws IOException {
        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
        assertEquals("The Real Guild", guilds.get(0).getName());
    }

    @Test
    @DisplayName("a file that is not a guild file is not reported as an error")
    void ignoredFilesAreNotReportedAsErrors() throws IOException {
        try (LogCapture log = new LogCapture()) {
            provider.getAllGuilds(null);

            assertFalse(log.contains("notes.txt"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("README"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("backup"), "the plugin complained about a folder it should ignore");
        }
    }

    @Test
    @DisplayName("only the guild data files count as an existing guild")
    void onlyDataFilesExist() throws IOException {
        assertTrue(provider.guildExists(null, GUILD_ID));
        assertFalse(provider.guildExists(null, "notes"));
        assertFalse(provider.guildExists(null, "README"));
        assertFalse(provider.guildExists(null, "backup"));
        assertFalse(provider.guildExists(null, "old-guild"));
    }

    @Test
    @DisplayName("a guild is only read from a guild data file")
    void onlyDataFilesAreRead() throws IOException {
        final Guild guild = provider.getGuild(null, GUILD_ID);

        assertNotNull(guild);
        assertEquals(GUILD_ID, guild.getId().toString());
        assertEquals("The Real Guild", guild.getName());

        assertNull(provider.getGuild(null, "notes"));
        assertNull(provider.getGuild(null, "README"));
        assertNull(provider.getGuild(null, "old-guild"));
    }

    @Test
    @DisplayName("the files the plugin ignores are left on disk")
    void ignoredFilesAreLeftAlone() throws IOException {
        provider.getAllGuilds(null);
        provider.getAllGuildIds(null);
        provider.guildExists(null, GUILD_ID);
        provider.getGuild(null, GUILD_ID);

        assertTrue(Files.exists(dataFolder().resolve(GUILD_ID + ".json")));
        assertTrue(Files.exists(dataFolder().resolve(GUILD_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(dataFolder().resolve("notes.txt")));
        assertTrue(Files.exists(dataFolder().resolve("README")));
        assertTrue(Files.isDirectory(dataFolder().resolve("backup")));
        assertTrue(Files.exists(dataFolder().resolve("backup").resolve("old-guild.json")));
    }

    @Test
    @DisplayName("a guild file that cannot be parsed does not take the other guilds with it")
    void aBrokenGuildFileDoesNotTakeTheOthersWithIt() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is left on disk")
    void aBrokenGuildFileIsLeftOnDisk() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
        assertEquals(BROKEN_CONTENT, new String(
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a guild file without an id does not take the other guilds with it")
    void aGuildFileWithoutAnIdDoesNotTakeTheOthersWithIt() throws IOException {
        write(OTHER_GUILD_ID + ".json", NO_ID_CONTENT);

        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
    }

    @Test
    @DisplayName("a guild file without an id is left on disk")
    void aGuildFileWithoutAnIdIsLeftOnDisk() throws IOException {
        write(OTHER_GUILD_ID + ".json", NO_ID_CONTENT);

        provider.getAllGuilds(null);

        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
        assertEquals(NO_ID_CONTENT, new String(
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a guild file that could not be read is not written over")
    void aGuildFileThatCouldNotBeReadIsNotWrittenOver() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        assertThrows(IOException.class, () -> provider.updateGuild(null, OTHER_GUILD_ID, guildJson(OTHER_GUILD_ID, "Replaced")));
        assertEquals(BROKEN_CONTENT, new String(
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a guild file without an id is not written over")
    void aGuildFileWithoutAnIdIsNotWrittenOver() throws IOException {
        write(OTHER_GUILD_ID + ".json", NO_ID_CONTENT);

        provider.getAllGuilds(null);

        assertThrows(IOException.class, () -> provider.updateGuild(null, OTHER_GUILD_ID, guildJson(OTHER_GUILD_ID, "Replaced")));
        assertEquals(NO_ID_CONTENT, new String(
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a guild file that could not be read is not deleted")
    void aGuildFileThatCouldNotBeReadIsNotDeleted() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        assertThrows(IOException.class, () -> provider.deleteGuild(null, OTHER_GUILD_ID));
        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
    }

    @Test
    @DisplayName("a guild file without an id is not deleted")
    void aGuildFileWithoutAnIdIsNotDeleted() throws IOException {
        write(OTHER_GUILD_ID + ".json", NO_ID_CONTENT);

        provider.getAllGuilds(null);

        assertThrows(IOException.class, () -> provider.deleteGuild(null, OTHER_GUILD_ID));
        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
    }

    @Test
    @DisplayName("a guild file that could not be read is backed up byte for byte")
    void aBrokenGuildFileIsBackedUpByteForByte() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "guilds");
        assertArrayEquals(BROKEN_CONTENT.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));

        // The original is still there, with the same bytes.
        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
        assertArrayEquals(BROKEN_CONTENT.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
    }

    @Test
    @DisplayName("a guild file without an id is backed up byte for byte")
    void aGuildFileWithoutAnIdIsBackedUpByteForByte() throws IOException {
        write(OTHER_GUILD_ID + ".json", NO_ID_CONTENT);

        provider.getAllGuilds(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "guilds");
        assertArrayEquals(NO_ID_CONTENT.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));
        assertArrayEquals(NO_ID_CONTENT.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
    }

    @Test
    @DisplayName("the backup lands outside the data folder and keeps the file name")
    void theBackupLandsOutsideTheDataFolder() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "guilds");

        assertEquals(OTHER_GUILD_ID + ".json", backup.getFileName().toString());
        assertEquals("guilds", backup.getParent().getFileName().toString());
        assertTrue(backup.getParent().getParent().getFileName().toString().matches("\\d{8}-\\d{6}"),
                "the backup folder is not named after a timestamp: " + backup);
        assertEquals("corrupt", backup.getParent().getParent().getParent().getFileName().toString());
        assertEquals("backups", backup.getParent().getParent().getParent().getParent().getFileName().toString());
        assertEquals(pluginFolder(), backup.getParent().getParent().getParent().getParent().getParent());
        assertFalse(backup.startsWith(dataFolder()));
    }

    @Test
    @DisplayName("the log names the file, the reason and the backup")
    void theLogNamesTheFileTheReasonAndTheBackup() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        try (LogCapture log = new LogCapture()) {
            provider.getAllGuilds(null);

            assertTrue(log.contains(OTHER_GUILD_ID + ".json"), "the log did not name the file");
            assertTrue(log.contains("could not be read"), "the log did not say what went wrong");
            assertTrue(log.contains("backups"), "the log did not say where the backup is");
        }
    }

    @Test
    @DisplayName("two corrupt guild files with the same name do not overwrite each other")
    void twoCorruptGuildFilesWithTheSameNameDoNotOverwriteEachOther() throws IOException {
        final Path file = dataFolder().resolve(OTHER_GUILD_ID + ".json");
        final String first = BROKEN_CONTENT;
        write(OTHER_GUILD_ID + ".json", first);
        final long modified = Files.getLastModifiedTime(file).toMillis();

        provider.getAllGuilds(null);

        // The same name, different content, and the same last modified time, so the second copy
        // lands in the same backup folder and has to get a name of its own.
        final String second = "{ this is not json either";
        write(OTHER_GUILD_ID + ".json", second);
        Files.setLastModifiedTime(file, FileTime.fromMillis(modified));

        provider.getAllGuilds(null);

        final List<Path> backups = CorruptBackups.backupFiles(pluginFolder(), "guilds");
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
    @DisplayName("a backup that cannot be written is reported and nothing is lost")
    void aBackupThatCannotBeWrittenIsReportedAndNothingIsLost() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        // A file where the backup folder should be, so the backup cannot be written.
        Files.createFile(pluginFolder().resolve("backups"));

        try (LogCapture log = new LogCapture()) {
            provider.getAllGuilds(null);

            assertTrue(log.contains("could not be written"), "the failure to back the file up was not reported");
            assertTrue(log.contains(OTHER_GUILD_ID + ".json"), "the log did not name the file");
        }

        // The file itself is untouched, and the guild that reads is still loaded.
        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
        assertArrayEquals(BROKEN_CONTENT.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")));
        assertEquals(1, provider.getAllGuilds(null).size());
    }

    @Test
    @DisplayName("the same corruption is backed up once, not once per read")
    void theSameCorruptionIsBackedUpOnce() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "guilds").size());

        provider.getAllGuilds(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "guilds").size());

        // A restart: a new provider over the same folder.
        final GuildJsonProvider restarted = new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        restarted.getAllGuilds(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "guilds").size());
    }

    @Test
    @DisplayName("the load does not write anything")
    void theLoadDoesNotWriteAnything() throws IOException {
        final Path good = dataFolder().resolve(GUILD_ID + ".json");
        final byte[] before = Files.readAllBytes(good);
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);

        provider.getAllGuilds(null);

        assertArrayEquals(before, Files.readAllBytes(good), "loading must not write anything");
    }

    @Test
    @DisplayName("a folder where no guild file can be read is a load failure")
    void aFolderWhereNothingCanBeReadIsALoadFailure() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);
        Files.delete(dataFolder().resolve(GUILD_ID + ".json"));

        final IOException failure = assertThrows(IOException.class, () -> provider.getAllGuilds(null));

        assertTrue(failure.getMessage().contains("the guild data could not be loaded"),
                "the message has to say that the guild data could not be loaded: " + failure.getMessage());
    }

    @Test
    @DisplayName("a folder where every guild file is unreadable is a load failure")
    void aFolderWhereEveryGuildFileIsUnreadableIsALoadFailure() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);
        write("33333333-3333-3333-3333-333333333333.json", NO_ID_CONTENT);
        Files.delete(dataFolder().resolve(GUILD_ID + ".json"));

        assertThrows(IOException.class, () -> provider.getAllGuilds(null),
                "a server that comes up with no guilds at all hides that the guild data could not be read");
    }

    @Test
    @DisplayName("a folder with no guild files at all is not a load failure")
    void anEmptyFolderIsNotALoadFailure() throws IOException {
        Files.delete(dataFolder().resolve(GUILD_ID + ".json"));

        assertTrue(provider.getAllGuilds(null).isEmpty(), "a server with no guilds has to start");
    }

    @Test
    @DisplayName("the save after a partial load does not overwrite the file that could not be parsed")
    void theSaveAfterAPartialLoadDoesNotOverwriteTheCorruptFile() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);
        final byte[] before = Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json"));

        final GuildAdapter guildAdapter = adapter();
        final List<Guild> loaded = guildAdapter.getAllGuilds();

        // This is what the scheduled save task does every minute, and what onDisable does.
        guildAdapter.saveGuilds(loaded);

        assertTrue(Files.exists(dataFolder().resolve(OTHER_GUILD_ID + ".json")),
                "the file that could not be parsed has to stay on disk");
        assertArrayEquals(before, Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")),
                "the save must not write over a file the plugin could not read");
    }

    @Test
    @DisplayName("the guilds that could be read are still there after a save and a restart")
    void theHealthyGuildsSurviveASaveAndARestart() throws IOException {
        write(OTHER_GUILD_ID + ".json", BROKEN_CONTENT);
        final byte[] corruptBefore = Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json"));

        // The server starts.
        final GuildAdapter guildAdapter = adapter();
        final List<Guild> loaded = guildAdapter.getAllGuilds();
        assertEquals(1, loaded.size());

        // The autosave runs, and the server shuts down, which saves again.
        guildAdapter.saveGuilds(loaded);
        guildAdapter.saveGuilds(loaded);

        // The server starts again, from the same folder.
        final List<Guild> reloaded = adapter().getAllGuilds();

        assertEquals(1, reloaded.size(), "the guild that could be read has to be there after a restart");
        assertEquals(GUILD_ID, reloaded.get(0).getId().toString());
        assertArrayEquals(corruptBefore, Files.readAllBytes(dataFolder().resolve(OTHER_GUILD_ID + ".json")),
                "a restart must not change the file the plugin could not read");
    }
}
