package me.glaremasters.guilds.database.guild.provider;

import me.glaremasters.guilds.database.LogCapture;
import me.glaremasters.guilds.database.TestGson;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which files the JSON guild storage treats as guilds.
 *
 * <p>The guild data folder is a folder an operator can see and touch. Anything can end up in it:
 * a leftover temporary file from a write that was interrupted, a note, a backup folder. The
 * provider used to take every entry it found and call it a guild, which let a stray file stand in
 * for a real guild and made the plugin report a guild that did not exist.
 */
class GuildJsonProviderTest {

    private static final String GUILD_ID = "11111111-1111-1111-1111-111111111111";
    private static final String OTHER_GUILD_ID = "22222222-2222-2222-2222-222222222222";

    @TempDir
    Path tempDir;

    private GuildJsonProvider provider;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        provider = new GuildJsonProvider(tempDir.toFile());

        // The one real guild.
        write(GUILD_ID + ".json", guildJson(GUILD_ID, "The Real Guild"));

        // What a write that was killed half way leaves behind: the plugin writes to a temporary
        // file next to the target and moves it into place afterwards.
        write(GUILD_ID + ".json" + "1234567890" + ".tmp", guildJson(GUILD_ID, "The Real Guild"));

        // Files an operator dropped in.
        write("notes.txt", "this is not a guild");
        write("README", "nothing to see here");

        // A folder, because people make backups that way.
        Files.createDirectory(tempDir.resolve("backup"));
        write(tempDir.resolve("backup").resolve("old-guild.json"), guildJson(OTHER_GUILD_ID, "Old Guild"));
    }

    private void write(String name, String content) throws IOException {
        write(tempDir.resolve(name), content);
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

    @Test
    @DisplayName("only the guild data files are enumerated")
    void onlyDataFilesAreEnumerated() {
        assertEquals(List.of(GUILD_ID), provider.getAllGuildIds(null));
    }

    @Test
    @DisplayName("only the guild data files are loaded as guilds")
    void onlyDataFilesAreLoaded() {
        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
        assertEquals("The Real Guild", guilds.get(0).getName());
    }

    @Test
    @DisplayName("a file that is not a guild file is not reported as an error")
    void ignoredFilesAreNotReportedAsErrors() {
        try (LogCapture log = new LogCapture()) {
            provider.getAllGuilds(null);

            assertFalse(log.contains("notes.txt"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("README"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("backup"), "the plugin complained about a folder it should ignore");
        }
    }

    @Test
    @DisplayName("only the guild data files count as an existing guild")
    void onlyDataFilesExist() {
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

        assertTrue(Files.exists(tempDir.resolve(GUILD_ID + ".json")));
        assertTrue(Files.exists(tempDir.resolve(GUILD_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(tempDir.resolve("notes.txt")));
        assertTrue(Files.exists(tempDir.resolve("README")));
        assertTrue(Files.isDirectory(tempDir.resolve("backup")));
        assertTrue(Files.exists(tempDir.resolve("backup").resolve("old-guild.json")));
    }

    @Test
    @DisplayName("a guild file that cannot be parsed does not take the other guilds with it")
    void aBrokenGuildFileDoesNotTakeTheOthersWithIt() throws IOException {
        write(OTHER_GUILD_ID + ".json", "{ this is not json");

        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
    }

    @Test
    @DisplayName("a guild file that cannot be parsed is left on disk")
    void aBrokenGuildFileIsLeftOnDisk() throws IOException {
        write(OTHER_GUILD_ID + ".json", "{ this is not json");

        provider.getAllGuilds(null);

        assertTrue(Files.exists(tempDir.resolve(OTHER_GUILD_ID + ".json")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(tempDir.resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a guild file without an id does not take the other guilds with it")
    void aGuildFileWithoutAnIdDoesNotTakeTheOthersWithIt() throws IOException {
        write(OTHER_GUILD_ID + ".json", "{ \"name\": \"No Id Here\" }");

        final List<Guild> guilds = provider.getAllGuilds(null);

        assertEquals(1, guilds.size());
        assertEquals(GUILD_ID, guilds.get(0).getId().toString());
    }

    @Test
    @DisplayName("a guild file without an id is left on disk")
    void aGuildFileWithoutAnIdIsLeftOnDisk() throws IOException {
        write(OTHER_GUILD_ID + ".json", "{ \"name\": \"No Id Here\" }");

        provider.getAllGuilds(null);

        assertTrue(Files.exists(tempDir.resolve(OTHER_GUILD_ID + ".json")));
        assertEquals("{ \"name\": \"No Id Here\" }", new String(
                Files.readAllBytes(tempDir.resolve(OTHER_GUILD_ID + ".json")), StandardCharsets.UTF_8));
    }
}
