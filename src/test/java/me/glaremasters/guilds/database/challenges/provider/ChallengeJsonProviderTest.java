package me.glaremasters.guilds.database.challenges.provider;

import me.glaremasters.guilds.database.LogCapture;
import me.glaremasters.guilds.database.TestGson;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which files the JSON challenge storage treats as challenges.
 *
 * <p>The challenge folder has the same problem the guild folder had: everything in it was taken to
 * be a challenge. See {@code GuildJsonProviderTest} for the full story.
 */
class ChallengeJsonProviderTest {

    private static final String CHALLENGE_ID = "44444444-4444-4444-4444-444444444444";

    @TempDir
    Path tempDir;

    private ChallengeJsonProvider provider;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        provider = new ChallengeJsonProvider(tempDir.toFile());

        write(CHALLENGE_ID + ".json", challengeJson(CHALLENGE_ID));
        write(CHALLENGE_ID + ".json" + "1234567890" + ".tmp", challengeJson(CHALLENGE_ID));
        write("notes.txt", "this is not a challenge");
        write("README", "nothing to see here");

        Files.createDirectory(tempDir.resolve("backup"));
        write(tempDir.resolve("backup").resolve("old-challenge.json"), challengeJson(CHALLENGE_ID));
    }

    private void write(String name, String content) throws IOException {
        write(tempDir.resolve(name), content);
    }

    private void write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String challengeJson(String id) {
        return "{\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"initiateTime\": 0,\n"
                + "  \"isCompleted\": false,\n"
                + "  \"challengePlayers\": [],\n"
                + "  \"defendPlayers\": []\n"
                + "}\n";
    }

    @Test
    @DisplayName("only the challenge data files are loaded as challenges")
    void onlyDataFilesAreLoaded() throws IOException {
        final Set<GuildChallenge> challenges = provider.getAllChallenges(null);

        assertEquals(1, challenges.size());
        assertEquals(CHALLENGE_ID, challenges.iterator().next().getId().toString());
    }

    @Test
    @DisplayName("a file that is not a challenge file is not reported as an error")
    void ignoredFilesAreNotReportedAsErrors() throws IOException {
        try (LogCapture log = new LogCapture()) {
            provider.getAllChallenges(null);

            assertFalse(log.contains("notes.txt"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("README"), "the plugin complained about a file it should ignore");
            assertFalse(log.contains("backup"), "the plugin complained about a folder it should ignore");
        }
    }

    @Test
    @DisplayName("only the challenge data files count as an existing challenge")
    void onlyDataFilesExist() throws IOException {
        assertTrue(provider.challengeExists(null, CHALLENGE_ID));
        assertFalse(provider.challengeExists(null, "notes"));
        assertFalse(provider.challengeExists(null, "README"));
        assertFalse(provider.challengeExists(null, "backup"));
        assertFalse(provider.challengeExists(null, "old-challenge"));
    }

    @Test
    @DisplayName("a challenge is only read from a challenge data file")
    void onlyDataFilesAreRead() throws IOException {
        final GuildChallenge challenge = provider.getChallenge(null, CHALLENGE_ID);

        assertNotNull(challenge);
        assertEquals(CHALLENGE_ID, challenge.getId().toString());

        assertNull(provider.getChallenge(null, "notes"));
        assertNull(provider.getChallenge(null, "README"));
        assertNull(provider.getChallenge(null, "old-challenge"));
    }

    @Test
    @DisplayName("the files the plugin ignores are left on disk")
    void ignoredFilesAreLeftAlone() throws IOException {
        provider.getAllChallenges(null);
        provider.challengeExists(null, CHALLENGE_ID);
        provider.getChallenge(null, CHALLENGE_ID);

        assertTrue(Files.exists(tempDir.resolve(CHALLENGE_ID + ".json")));
        assertTrue(Files.exists(tempDir.resolve(CHALLENGE_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(tempDir.resolve("notes.txt")));
        assertTrue(Files.exists(tempDir.resolve("README")));
        assertTrue(Files.isDirectory(tempDir.resolve("backup")));
        assertTrue(Files.exists(tempDir.resolve("backup").resolve("old-challenge.json")));
    }

    @Test
    @DisplayName("a challenge file that cannot be parsed does not take the other challenges with it")
    void aBrokenChallengeFileDoesNotTakeTheOthersWithIt() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        final Set<GuildChallenge> challenges = provider.getAllChallenges(null);

        assertEquals(1, challenges.size());
        assertEquals(CHALLENGE_ID, challenges.iterator().next().getId().toString());
    }

    @Test
    @DisplayName("a challenge file that cannot be parsed is left on disk")
    void aBrokenChallengeFileIsLeftOnDisk() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        assertTrue(Files.exists(tempDir.resolve("broken-challenge.json")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(tempDir.resolve("broken-challenge.json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a challenge file without an id does not take the other challenges with it")
    void aChallengeFileWithoutAnIdDoesNotTakeTheOthersWithIt() throws IOException {
        write("no-id-challenge.json", "{ \"isCompleted\": false }");

        final Set<GuildChallenge> challenges = provider.getAllChallenges(null);

        assertEquals(1, challenges.size());
        assertEquals(CHALLENGE_ID, challenges.iterator().next().getId().toString());
    }

    @Test
    @DisplayName("a challenge file without an id is left on disk")
    void aChallengeFileWithoutAnIdIsLeftOnDisk() throws IOException {
        write("no-id-challenge.json", "{ \"isCompleted\": false }");

        provider.getAllChallenges(null);

        assertTrue(Files.exists(tempDir.resolve("no-id-challenge.json")));
    }

    @Test
    @DisplayName("a challenge file that could not be read is not written over")
    void aChallengeFileThatCouldNotBeReadIsNotWrittenOver() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        assertThrows(IOException.class, () -> provider.updateChallenge(null, "broken-challenge", challengeJson(CHALLENGE_ID)));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(tempDir.resolve("broken-challenge.json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a challenge file that could not be read is not deleted")
    void aChallengeFileThatCouldNotBeReadIsNotDeleted() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        assertThrows(IOException.class, () -> provider.deleteChallenge(null, "broken-challenge"));
        assertTrue(Files.exists(tempDir.resolve("broken-challenge.json")));
    }
}
