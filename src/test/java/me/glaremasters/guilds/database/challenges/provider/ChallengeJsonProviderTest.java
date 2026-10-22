package me.glaremasters.guilds.database.challenges.provider;

import me.glaremasters.guilds.database.CorruptBackups;
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
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which files the JSON challenge storage treats as challenges, and what happens to the ones
 * it cannot read.
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
        Files.createDirectories(dataFolder());
        provider = new ChallengeJsonProvider(dataFolder().toFile(), pluginFolder().toFile());

        write(CHALLENGE_ID + ".json", challengeJson(CHALLENGE_ID));
        write(CHALLENGE_ID + ".json" + "1234567890" + ".tmp", challengeJson(CHALLENGE_ID));
        write("notes.txt", "this is not a challenge");
        write("README", "nothing to see here");

        Files.createDirectory(dataFolder().resolve("backup"));
        write(dataFolder().resolve("backup").resolve("old-challenge.json"), challengeJson(CHALLENGE_ID));
    }

    /**
     * @return the plugin's data folder, the one the backups are kept in
     */
    private Path pluginFolder() {
        return tempDir;
    }

    /**
     * @return the folder the challenge files live in
     */
    private Path dataFolder() {
        return tempDir.resolve("challenges");
    }

    private void write(String name, String content) throws IOException {
        write(dataFolder().resolve(name), content);
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

        assertTrue(Files.exists(dataFolder().resolve(CHALLENGE_ID + ".json")));
        assertTrue(Files.exists(dataFolder().resolve(CHALLENGE_ID + ".json" + "1234567890" + ".tmp")));
        assertTrue(Files.exists(dataFolder().resolve("notes.txt")));
        assertTrue(Files.exists(dataFolder().resolve("README")));
        assertTrue(Files.isDirectory(dataFolder().resolve("backup")));
        assertTrue(Files.exists(dataFolder().resolve("backup").resolve("old-challenge.json")));
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

        assertTrue(Files.exists(dataFolder().resolve("broken-challenge.json")));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(dataFolder().resolve("broken-challenge.json")), StandardCharsets.UTF_8));
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

        assertTrue(Files.exists(dataFolder().resolve("no-id-challenge.json")));
    }

    @Test
    @DisplayName("a challenge file that could not be read is not written over")
    void aChallengeFileThatCouldNotBeReadIsNotWrittenOver() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        assertThrows(IOException.class, () -> provider.updateChallenge(null, "broken-challenge", challengeJson(CHALLENGE_ID)));
        assertEquals("{ this is not json", new String(
                Files.readAllBytes(dataFolder().resolve("broken-challenge.json")), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a challenge file that could not be read is not deleted")
    void aChallengeFileThatCouldNotBeReadIsNotDeleted() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        assertThrows(IOException.class, () -> provider.deleteChallenge(null, "broken-challenge"));
        assertTrue(Files.exists(dataFolder().resolve("broken-challenge.json")));
    }

    @Test
    @DisplayName("a challenge file that cannot be read is backed up byte for byte")
    void aBrokenChallengeFileIsBackedUpByteForByte() throws IOException {
        final String corrupt = "{ this is not json";
        write("broken-challenge.json", corrupt);

        provider.getAllChallenges(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "challenges");
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));

        assertTrue(Files.exists(dataFolder().resolve("broken-challenge.json")));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("broken-challenge.json")));
    }

    @Test
    @DisplayName("a challenge file without an id is backed up byte for byte")
    void aChallengeFileWithoutAnIdIsBackedUpByteForByte() throws IOException {
        final String unusable = "{ \"isCompleted\": false }";
        write("no-id-challenge.json", unusable);

        provider.getAllChallenges(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "challenges");
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("no-id-challenge.json")));
    }

    @Test
    @DisplayName("the challenge backup lands outside the data folder and keeps the file name")
    void theBackupLandsOutsideTheDataFolder() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "challenges");

        assertEquals("broken-challenge.json", backup.getFileName().toString());
        assertEquals("challenges", backup.getParent().getFileName().toString());
        assertTrue(backup.getParent().getParent().getFileName().toString().matches("\\d{8}-\\d{6}"),
                "the backup folder is not named after a timestamp: " + backup);
        assertEquals("corrupt", backup.getParent().getParent().getParent().getFileName().toString());
        assertEquals("backups", backup.getParent().getParent().getParent().getParent().getFileName().toString());
        assertEquals(pluginFolder(), backup.getParent().getParent().getParent().getParent().getParent());
        assertFalse(backup.startsWith(dataFolder()));
    }

    @Test
    @DisplayName("the log names the challenge file, the reason and the backup")
    void theLogNamesTheFileTheReasonAndTheBackup() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        try (LogCapture log = new LogCapture()) {
            provider.getAllChallenges(null);

            assertTrue(log.contains("broken-challenge.json"), "the log did not name the file");
            assertTrue(log.contains("could not be read"), "the log did not say what went wrong");
            assertTrue(log.contains("backups"), "the log did not say where the backup is");
        }
    }

    @Test
    @DisplayName("two corrupt challenge files with the same name do not overwrite each other")
    void twoCorruptChallengeFilesWithTheSameNameDoNotOverwriteEachOther() throws IOException {
        final Path file = dataFolder().resolve("broken-challenge.json");
        final String first = "{ this is not json";
        write("broken-challenge.json", first);
        final long modified = Files.getLastModifiedTime(file).toMillis();

        provider.getAllChallenges(null);

        final String second = "{ this is not json either";
        write("broken-challenge.json", second);
        Files.setLastModifiedTime(file, FileTime.fromMillis(modified));

        provider.getAllChallenges(null);

        final List<Path> backups = CorruptBackups.backupFiles(pluginFolder(), "challenges");
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
    @DisplayName("a challenge backup that cannot be written is reported and nothing is lost")
    void aBackupThatCannotBeWrittenIsReportedAndNothingIsLost() throws IOException {
        final String corrupt = "{ this is not json";
        write("broken-challenge.json", corrupt);

        Files.createFile(pluginFolder().resolve("backups"));

        try (LogCapture log = new LogCapture()) {
            provider.getAllChallenges(null);

            assertTrue(log.contains("could not be written"), "the failure to back the file up was not reported");
            assertTrue(log.contains("broken-challenge.json"), "the log did not name the file");
        }

        assertTrue(Files.exists(dataFolder().resolve("broken-challenge.json")));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(dataFolder().resolve("broken-challenge.json")));
        assertEquals(1, provider.getAllChallenges(null).size());
    }

    @Test
    @DisplayName("the same challenge corruption is backed up once, not once per read")
    void theSameCorruptionIsBackedUpOnce() throws IOException {
        write("broken-challenge.json", "{ this is not json");

        provider.getAllChallenges(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "challenges").size());

        provider.getAllChallenges(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "challenges").size());

        final ChallengeJsonProvider restarted = new ChallengeJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        restarted.getAllChallenges(null);
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "challenges").size());
    }
}
