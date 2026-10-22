package me.glaremasters.guilds.database.guild.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers what the JSON backend does with a guild file it cannot read.
 *
 * <p>The plugin stores one JSON file per guild, named after the guild's id. A file that does not
 * parse is a file the plugin cannot read, and the plugin must not respond by writing over it: the
 * only copy of that guild's data is in that file.
 *
 * <p>Three properties are asserted here, and they are the ones a server owner cares about:
 *
 * <ul>
 *   <li>the guild is not loaded, so nothing pretends it exists;</li>
 *   <li>the file is left exactly as it was, byte for byte;</li>
 *   <li>the guilds that did load are untouched by the failure.</li>
 * </ul>
 *
 * <p>The third one is the reason the corrupt file has to be protected explicitly. The save writes
 * every guild it has in memory, one file at a time, so a file the plugin could not parse is a file
 * the plugin does not know about. Whether that is safe depends entirely on the save path refusing
 * to write over a file it failed to read.
 *
 * <p>A file that cannot be read is also copied into the backup folder, so that there is a second
 * copy of it somewhere the plugin will never write to.
 *
 * <p>The JSON storage workstream (#846) adds a file of its own at this path. Its cases about the
 * enumeration rule, that only {@code .json} files are records and that a record without an id is
 * skipped, describe rules that land with that branch and are therefore not repeated here. The
 * cases about a file that cannot be parsed are covered above.
 */
class GuildJsonProviderTest {

    /** The id of the guild whose file parses. */
    private static final String GUILD_ID = "11111111-1111-1111-1111-111111111111";

    /** The id of the guild whose file does not parse. */
    private static final String CORRUPT_ID = "22222222-2222-2222-2222-222222222222";

    /** A guild file that is not valid JSON at all. */
    private static final String CORRUPT_CONTENT = "{ this is not json";

    /** A guild file that parses but carries no id. */
    private static final String NO_ID_CONTENT = "{ \"name\": \"No Id Here\" }";

    @TempDir
    Path tempDir;

    private Path dataFolder;
    private GuildJsonProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        setPluginGson(new GsonBuilder().setPrettyPrinting().create());

        dataFolder = tempDir.resolve("data");
        Files.createDirectories(dataFolder);

        // The plugin's data folder is the parent of the guild data folder, and that is where the
        // backups of unreadable files go.
        provider = new GuildJsonProvider(tempDir.toFile(), dataFolder.toFile());

        write(GUILD_ID + ".json", guildJson(GUILD_ID, "The Real Guild"));
    }

    /**
     * Puts a Gson instance into the plugin's static field.
     *
     * <p>{@link Guilds#getGson()} is what every provider uses to read and write, and the plugin
     * fills the field in during startup. A test that does not set it gets a null Gson and a
     * NullPointerException that has nothing to do with what it is testing.
     *
     * @param gson the Gson instance the plugin should use
     * @throws Exception if the field cannot be reached
     */
    private static void setPluginGson(Gson gson) throws Exception {
        final Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        field.set(null, gson);
    }

    /**
     * @param id   the guild's id
     * @param name the guild's name
     * @return a guild file as the plugin writes it
     */
    private static String guildJson(String id, String name) {
        return "{\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"name\": \"" + name + "\",\n"
                + "  \"prefix\": \"Pfx\",\n"
                + "  \"motd\": \"A guild.\",\n"
                + "  \"guildMaster\": {\n"
                + "    \"uuid\": \"33333333-3333-3333-3333-333333333333\",\n"
                + "    \"role\": {\n"
                + "      \"level\": 0\n"
                + "    },\n"
                + "    \"joinDate\": 0,\n"
                + "    \"lastLogin\": 0\n"
                + "  },\n"
                + "  \"status\": \"Public\",\n"
                + "  \"tier\": {\n"
                + "    \"level\": 1\n"
                + "  },\n"
                + "  \"balance\": 0.0,\n"
                + "  \"members\": [\n"
                + "    {\n"
                + "      \"uuid\": \"33333333-3333-3333-3333-333333333333\",\n"
                + "      \"role\": {\n"
                + "        \"level\": 0\n"
                + "      },\n"
                + "      \"joinDate\": 0,\n"
                + "      \"lastLogin\": 0\n"
                + "    }\n"
                + "  ],\n"
                + "  \"invitedMembers\": [],\n"
                + "  \"allies\": [],\n"
                + "  \"pendingAllies\": [],\n"
                + "  \"codes\": [],\n"
                + "  \"vaults\": [],\n"
                + "  \"lastDefended\": 0,\n"
                + "  \"creationDate\": 1700000000000\n"
                + "}";
    }

    /**
     * @param id      the guild's id
     * @param content the file's contents
     * @return the path of the guild file
     * @throws IOException if the file cannot be written
     */
    private Path writeGuildFile(String id, String content) throws IOException {
        final Path file = dataFolder.resolve(id + ".json");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * @param name    the file's name
     * @param content the file's contents
     * @return the path of the file
     * @throws IOException if the file cannot be written
     */
    private Path write(String name, String content) throws IOException {
        final Path file = dataFolder.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * @return an adapter over the same folder the provider reads, so a save goes through the
     *         production path rather than through the provider directly
     */
    private GuildAdapter adapter() {
        final Guilds plugin = mock(Guilds.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());

        final me.glaremasters.guilds.database.DatabaseAdapter database =
                mock(me.glaremasters.guilds.database.DatabaseAdapter.class);
        when(database.getBackend()).thenReturn(DatabaseBackend.JSON);

        return new GuildAdapter(plugin, database);
    }

    /**
     * @param name the file's name
     * @return every copy of that file in the backup folder
     */
    private List<Path> backupsOf(String name) throws IOException {
        final Path backupRoot = tempDir.resolve("backups").resolve("corrupt");
        final List<Path> backups = new ArrayList<>();

        if (!Files.isDirectory(backupRoot)) {
            return backups;
        }

        try (Stream<Path> stamps = Files.list(backupRoot)) {
            for (Path stamp : stamps.toArray(Path[]::new)) {
                final Path copy = stamp.resolve("guilds").resolve(name);
                if (Files.exists(copy)) {
                    backups.add(copy);
                }
            }
        }

        return backups;
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is not loaded")
    void corruptFileIsNotLoaded() throws IOException {
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        final List<Guild> loaded = provider.getAllGuilds(null);

        assertEquals(1, loaded.size(), "only the guild that parsed may be loaded");
        assertEquals(GUILD_ID, loaded.get(0).getId().toString());
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is left exactly as it was")
    void corruptFileIsLeftUntouched() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] before = Files.readAllBytes(corrupt);

        provider.getAllGuilds(null);

        assertTrue(Files.exists(corrupt), "the file that could not be parsed has to stay on disk");
        assertArrayEquals(before, Files.readAllBytes(corrupt), "the file must not be rewritten");
    }

    @Test
    @DisplayName("the guilds that did load are not rewritten by the load")
    void otherFilesAreNotRewrittenByTheLoad() throws IOException {
        final Path good = dataFolder.resolve(GUILD_ID + ".json");
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] before = Files.readAllBytes(good);

        provider.getAllGuilds(null);

        assertArrayEquals(before, Files.readAllBytes(good), "loading must not write anything");
    }

    @Test
    @DisplayName("the save after a partial load does not overwrite the file that could not be parsed")
    void saveDoesNotOverwriteTheCorruptFile() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] before = Files.readAllBytes(corrupt);

        final GuildAdapter guildAdapter = adapter();
        final List<Guild> loaded = guildAdapter.getAllGuilds();

        // This is what the scheduled save task does every minute, and what onDisable does.
        guildAdapter.saveGuilds(loaded);

        assertTrue(Files.exists(corrupt), "the file that could not be parsed has to stay on disk");
        assertArrayEquals(before, Files.readAllBytes(corrupt),
                "the save must not write over a file the plugin could not read");
    }

    @Test
    @DisplayName("the save after a partial load leaves the guilds that did load alone")
    void saveKeepsTheLoadedGuilds() throws IOException {
        final Path good = dataFolder.resolve(GUILD_ID + ".json");
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        final GuildAdapter guildAdapter = adapter();
        final List<Guild> loaded = guildAdapter.getAllGuilds();
        guildAdapter.saveGuilds(loaded);

        final List<Guild> reloaded = guildAdapter.getAllGuilds();
        assertEquals(1, reloaded.size());
        assertEquals(GUILD_ID, reloaded.get(0).getId().toString());
        assertTrue(Files.exists(good), "the guild that loaded has to stay on disk");
    }

    @Test
    @DisplayName("the guilds that could be read are still there after a save and a restart")
    void theHealthyGuildsSurviveASaveAndARestart() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] corruptBefore = Files.readAllBytes(corrupt);

        // The server starts.
        final GuildAdapter guildAdapter = adapter();
        final List<Guild> loaded = guildAdapter.getAllGuilds();
        assertEquals(1, loaded.size());

        // The autosave runs, and the server shuts down, which saves again.
        guildAdapter.saveGuilds(loaded);
        guildAdapter.saveGuilds(loaded);

        // The server starts again, from the same folder.
        final GuildAdapter afterRestart = adapter();
        final List<Guild> reloaded = afterRestart.getAllGuilds();

        assertEquals(1, reloaded.size(), "the guild that could be read has to be there after a restart");
        assertEquals(GUILD_ID, reloaded.get(0).getId().toString());
        assertArrayEquals(corruptBefore, Files.readAllBytes(corrupt),
                "a restart must not change the file the plugin could not read");
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is not written over, even by its own id")
    void corruptFileIsProtectedFromEveryWrite() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] before = Files.readAllBytes(corrupt);

        provider.getAllGuilds(null);

        // The id of a file the plugin could not read is known: it is the file's name. Writing to
        // that id is the one way the corrupt file could be destroyed, so it has to be refused.
        provider.createGuild(null, CORRUPT_ID, guildJson(CORRUPT_ID, "BadGuild"));
        assertThrows(IOException.class,
                () -> provider.updateGuild(null, CORRUPT_ID, guildJson(CORRUPT_ID, "BadGuild")),
                "a file the plugin could not read must not be written over");

        assertArrayEquals(before, Files.readAllBytes(corrupt),
                "a file that could not be parsed must never be written over");
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is not deleted either")
    void corruptFileIsProtectedFromDeletion() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        provider.getAllGuilds(null);

        assertThrows(IOException.class, () -> provider.deleteGuild(null, CORRUPT_ID),
                "a file the plugin could not read must not be deleted");

        assertTrue(Files.exists(corrupt), "the file has to stay on disk");
    }

    @Test
    @DisplayName("a guild file without an id is left on disk")
    void aGuildFileWithoutAnIdIsLeftOnDisk() throws IOException {
        final Path file = writeGuildFile(CORRUPT_ID, NO_ID_CONTENT);

        provider.getAllGuilds(null);

        assertTrue(Files.exists(file), "the file has to stay on disk");
        assertArrayEquals(NO_ID_CONTENT.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(file));
    }

    @Test
    @DisplayName("a data folder that cannot be read is reported as an I/O failure")
    void unreadableDataFolderIsReported() throws IOException {
        final GuildJsonProvider missing = new GuildJsonProvider(tempDir.resolve("gone").toFile());

        final IOException failure = assertThrows(IOException.class, () -> missing.getAllGuilds(null));

        assertTrue(failure.getMessage().contains("gone"),
                "the message has to name the folder that could not be read: " + failure.getMessage());
    }

    @Test
    @DisplayName("a guild file that cannot be parsed does not stop the other guilds from loading")
    void otherGuildsStayAvailable() throws IOException {
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        final List<Guild> loaded = provider.getAllGuilds(null);

        assertFalse(loaded.isEmpty(), "one broken file must not cost the server every guild");
    }

    @Test
    @DisplayName("a folder where no guild file can be read is a load failure")
    void aFolderWhereNothingCanBeReadIsALoadFailure() throws IOException {
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        Files.delete(dataFolder.resolve(GUILD_ID + ".json"));

        final IOException failure = assertThrows(IOException.class, () -> provider.getAllGuilds(null));

        assertTrue(failure.getMessage().contains("the guild data could not be loaded"),
                "the message has to say that the guild data could not be loaded: " + failure.getMessage());
    }

    @Test
    @DisplayName("a folder with no guild files at all is not a load failure")
    void anEmptyFolderIsNotALoadFailure() throws IOException {
        Files.delete(dataFolder.resolve(GUILD_ID + ".json"));

        assertTrue(provider.getAllGuilds(null).isEmpty(), "a server with no guilds has to start");
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is copied into the backup folder")
    void theCorruptFileIsBackedUp() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        provider.getAllGuilds(null);

        final List<Path> backups = backupsOf(CORRUPT_ID + ".json");
        assertEquals(1, backups.size(), "the file the plugin could not read has to be copied somewhere");
        assertArrayEquals(Files.readAllBytes(corrupt), Files.readAllBytes(backups.get(0)),
                "the copy has to be byte for byte the file the plugin could not read");
    }

    @Test
    @DisplayName("the backup of a guild file is outside the data folder")
    void theBackupIsOutsideTheDataFolder() throws IOException {
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        provider.getAllGuilds(null);

        for (Path backup : backupsOf(CORRUPT_ID + ".json")) {
            assertFalse(backup.startsWith(dataFolder),
                    "a backup the plugin enumerates would be read as a guild: " + backup);
        }
    }

    @Test
    @DisplayName("the same corruption read twice does not produce two backups")
    void theSameCorruptionIsNotBackedUpTwice() throws IOException {
        writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);

        provider.getAllGuilds(null);
        provider.getAllGuilds(null);

        assertEquals(1, backupsOf(CORRUPT_ID + ".json").size(),
                "reading the same corruption again has to reuse the copy that is already there");
    }

    @Test
    @DisplayName("a backup that cannot be written is reported and the original file is left alone")
    void aBackupThatCannotBeWrittenIsReported() throws IOException {
        final Path corrupt = writeGuildFile(CORRUPT_ID, CORRUPT_CONTENT);
        final byte[] before = Files.readAllBytes(corrupt);

        // A file where the backup folder has to be makes every backup fail.
        Files.write(tempDir.resolve("backups"), "not a folder".getBytes(StandardCharsets.UTF_8));

        try (LogCapture log = new LogCapture()) {
            final List<Guild> loaded = provider.getAllGuilds(null);

            assertEquals(1, loaded.size(), "a backup that cannot be written must not stop the load");
            assertTrue(log.contains("No copy of the file could be taken"),
                    "the operator has to be told that the backup failed");
        }

        assertArrayEquals(before, Files.readAllBytes(corrupt),
                "a failed backup must not change the original file");
        assertTrue(Files.exists(corrupt));
    }

    /**
     * Captures what the plugin logs while a test runs.
     */
    private static final class LogCapture implements AutoCloseable {

        private final List<String> messages = new ArrayList<>();

        private final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record != null && record.getMessage() != null) {
                    messages.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        LogCapture() {
            Logger.getLogger("Guilds").addHandler(handler);
        }

        /**
         * @param fragment the text to look for
         * @return true when the plugin logged a line carrying that text
         */
        boolean contains(String fragment) {
            return messages.stream().anyMatch(message -> message.contains(fragment));
        }

        @Override
        public void close() {
            Logger.getLogger("Guilds").removeHandler(handler);
        }
    }
}
