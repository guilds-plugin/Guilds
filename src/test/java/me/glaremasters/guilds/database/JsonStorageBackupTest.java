package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeJsonProvider;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownJsonProvider;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the backups as a whole.
 *
 * <p>The backups live outside the four data folders, so a backup is never loaded as a record,
 * however it is named. Two of them with the same name must not overwrite each other either.
 */
class JsonStorageBackupTest {

    private static final String GUILD_ID = "11111111-1111-1111-1111-111111111111";
    private static final String ARENA_ID = "33333333-3333-3333-3333-333333333333";
    private static final String CHALLENGE_ID = "44444444-4444-4444-4444-444444444444";

    @TempDir
    Path tempDir;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(dataFolder());
        Files.createDirectories(arenaFolder());
        Files.createDirectories(challengeFolder());
        Files.createDirectories(cooldownFolder());

        Files.write(dataFolder().resolve(GUILD_ID + ".json"), ("{ \"id\": \"" + GUILD_ID + "\", \"name\": \"A guild\" }")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(arenaFolder().resolve(ARENA_ID + ".json"), ("{ \"id\": \"" + ARENA_ID + "\", \"name\": \"An arena\" }")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(challengeFolder().resolve(CHALLENGE_ID + ".json"), ("{ \"id\": \"" + CHALLENGE_ID + "\", \"isCompleted\": false }")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(cooldownFolder().resolve("cooldowns.json"), "[]".getBytes(StandardCharsets.UTF_8));
    }

    private Path pluginFolder() {
        return tempDir;
    }

    private Path dataFolder() {
        return tempDir.resolve("data");
    }

    private Path arenaFolder() {
        return tempDir.resolve("arenas");
    }

    private Path challengeFolder() {
        return tempDir.resolve("challenges");
    }

    private Path cooldownFolder() {
        return tempDir.resolve("cooldowns");
    }

    @Test
    @DisplayName("a backup is never enumerated as a record by any of the four providers")
    void aBackupIsNeverEnumeratedAsARecord() throws IOException {
        // A backup, where the helper puts it, and a copy of one an operator left in the backup
        // folder by hand.
        final Path backup = tempDir.resolve("backups").resolve("corrupt").resolve("20200101-000000").resolve("guilds")
                .resolve(GUILD_ID + ".json");
        Files.createDirectories(backup.getParent());
        Files.write(backup, ("{ \"id\": \"" + GUILD_ID + "\", \"name\": \"A guild\" }").getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("backups").resolve(GUILD_ID + ".json"),
                ("{ \"id\": \"" + GUILD_ID + "\", \"name\": \"A guild\" }").getBytes(StandardCharsets.UTF_8));

        final GuildJsonProvider guilds = new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        final ArenaJsonProvider arenas = new ArenaJsonProvider(arenaFolder().toFile(), pluginFolder().toFile());
        final ChallengeJsonProvider challenges = new ChallengeJsonProvider(challengeFolder().toFile(), pluginFolder().toFile());
        final CooldownJsonProvider cooldowns = new CooldownJsonProvider(cooldownFolder().toFile(), pluginFolder().toFile());

        assertEquals(List.of(GUILD_ID), guilds.getAllGuildIds(null));
        assertEquals(List.of(ARENA_ID), arenas.getAllArenaIds(null));

        final Set<GuildChallenge> loaded = challenges.getAllChallenges(null);
        assertEquals(1, loaded.size());
        assertEquals(CHALLENGE_ID, loaded.iterator().next().getId().toString());

        assertTrue(cooldowns.getAllCooldowns(null).isEmpty());

        // The backup is still where it was.
        assertTrue(Files.exists(backup));
    }

    @Test
    @DisplayName("the backup folder sits beside the data folders, not inside one")
    void theBackupFolderSitsBesideTheDataFolders() throws IOException {
        Files.write(dataFolder().resolve("broken.json"), "{ this is not json".getBytes(StandardCharsets.UTF_8));

        new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile()).getAllGuilds(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "guilds");
        assertFalse(backup.startsWith(dataFolder()));
        assertFalse(backup.startsWith(arenaFolder()));
        assertFalse(backup.startsWith(challengeFolder()));
        assertFalse(backup.startsWith(cooldownFolder()));
    }

    @Test
    @DisplayName("two corrupt files with the same name in two storages do not overwrite each other")
    void twoCorruptFilesWithTheSameNameInTwoStorages() throws IOException {
        final String corrupt = "{ this is not json";
        Files.write(dataFolder().resolve("same-name.json"), corrupt.getBytes(StandardCharsets.UTF_8));
        Files.write(arenaFolder().resolve("same-name.json"), corrupt.getBytes(StandardCharsets.UTF_8));

        new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile()).getAllGuilds(null);
        new ArenaJsonProvider(arenaFolder().toFile(), pluginFolder().toFile()).getAllArenas(null);

        final Path guildBackup = CorruptBackups.onlyBackup(pluginFolder(), "guilds");
        final Path arenaBackup = CorruptBackups.onlyBackup(pluginFolder(), "arenas");

        assertEquals("same-name.json", guildBackup.getFileName().toString());
        assertEquals("same-name.json", arenaBackup.getFileName().toString());
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(guildBackup));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(arenaBackup));
    }

    @Test
    @DisplayName("the four providers complain when the two folders are given the wrong way round")
    void theProvidersComplainWhenTheFoldersAreSwapped() throws IOException {
        try (LogCapture log = new LogCapture()) {
            // The plugin's data folder handed over as the record folder, and the record folder as
            // the plugin's folder. Both are File arguments, so the compiler does not notice.
            new GuildJsonProvider(pluginFolder().toFile(), dataFolder().toFile());
            new ArenaJsonProvider(pluginFolder().toFile(), arenaFolder().toFile());
            new ChallengeJsonProvider(pluginFolder().toFile(), challengeFolder().toFile());
            new CooldownJsonProvider(pluginFolder().toFile(), cooldownFolder().toFile());

            assertTrue(log.contains("wrong way round"), "the swap was not reported");
            assertTrue(log.contains(pluginFolder().toString()), "the message did not name the plugin's data folder");
            assertTrue(log.contains(dataFolder().toString()), "the message did not name the record folder");
        }
    }

    @Test
    @DisplayName("the four providers do not complain about the folders the plugin uses")
    void theProvidersDoNotComplainAboutTheRightFolders() throws IOException {
        try (LogCapture log = new LogCapture()) {
            new GuildJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
            new ArenaJsonProvider(arenaFolder().toFile(), pluginFolder().toFile());
            new ChallengeJsonProvider(challengeFolder().toFile(), pluginFolder().toFile());
            new CooldownJsonProvider(cooldownFolder().toFile(), pluginFolder().toFile());

            assertFalse(log.contains("wrong way round"), "the right folders were reported as the wrong way round");
        }
    }
}
