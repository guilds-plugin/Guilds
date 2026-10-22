package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.CorruptBackups;
import me.glaremasters.guilds.database.LogCapture;
import me.glaremasters.guilds.database.TestGson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what the JSON cooldown storage does with an entry it cannot read, and with the file it
 * cannot read at all.
 *
 * <p>The cooldown file is the one data file that holds a list rather than a single record, so it
 * is the one place where a single broken entry used to cost every other entry as well: the file
 * could not be parsed, no cooldown was loaded, and the plugin logged one error that did not say
 * which entry was at fault.
 */
class CooldownJsonProviderTest {

    private static final UUID COOLDOWN_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID OWNER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @TempDir
    Path tempDir;

    private CooldownJsonProvider provider;

    @BeforeAll
    static void installGson() throws ReflectiveOperationException {
        TestGson.install();
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(dataFolder());
        provider = new CooldownJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        provider.createContainer(null);
    }

    /**
     * @return the plugin's data folder, the one the backups are kept in
     */
    private Path pluginFolder() {
        return tempDir;
    }

    /**
     * @return the folder the cooldown file lives in
     */
    private Path dataFolder() {
        return tempDir.resolve("cooldowns");
    }

    private Path cooldownFile() {
        return dataFolder().resolve("cooldowns.json");
    }

    private void writeCooldowns(String content) throws IOException {
        Files.write(cooldownFile(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static String cooldownEntry(UUID id, Cooldown.Type type, UUID owner, long expiry) {
        return "  {\n"
                + "    \"cooldownId\": \"" + id + "\",\n"
                + "    \"cooldownType\": \"" + type.name() + "\",\n"
                + "    \"cooldownOwner\": \"" + owner + "\",\n"
                + "    \"cooldownExpiry\": " + expiry + "\n"
                + "  }";
    }

    @Test
    @DisplayName("a cooldown file with one good entry is read")
    void aGoodFileIsRead() throws IOException {
        writeCooldowns("[\n" + cooldownEntry(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L) + "\n]\n");

        final List<Cooldown> cooldowns = provider.getAllCooldowns(null);

        assertEquals(1, cooldowns.size());
        assertEquals(COOLDOWN_ID, cooldowns.get(0).getCooldownId());
        assertEquals(Cooldown.Type.Request, cooldowns.get(0).getCooldownType());
        assertEquals(OWNER_ID, cooldowns.get(0).getCooldownOwner());
    }

    @Test
    @DisplayName("a broken entry does not take the other entries with it")
    void aBrokenEntryDoesNotTakeTheOthersWithIt() throws IOException {
        writeCooldowns("[\n"
                + "  \"this is not a cooldown\",\n"
                + cooldownEntry(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L) + ",\n"
                + "  {\n"
                + "    \"cooldownId\": \"77777777-7777-7777-7777-777777777777\",\n"
                + "    \"cooldownType\": \"NotAType\",\n"
                + "    \"cooldownOwner\": \"" + OWNER_ID + "\",\n"
                + "    \"cooldownExpiry\": 9999999999999\n"
                + "  }\n"
                + "]\n");

        final List<Cooldown> cooldowns = provider.getAllCooldowns(null);

        assertEquals(1, cooldowns.size());
        assertEquals(COOLDOWN_ID, cooldowns.get(0).getCooldownId());
    }

    @Test
    @DisplayName("a broken entry is still in the file after a cooldown is written")
    void aBrokenEntrySurvivesAWrite() throws IOException {
        writeCooldowns("[\n"
                + "  \"this is not a cooldown\",\n"
                + cooldownEntry(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L) + "\n"
                + "]\n");

        provider.createCooldown(null, UUID.randomUUID().toString(), Cooldown.Type.Home.name(), OWNER_ID.toString(), new Timestamp(9999999999999L));

        final String written = new String(Files.readAllBytes(cooldownFile()), StandardCharsets.UTF_8);

        assertTrue(written.contains("this is not a cooldown"), "the entry that could not be read was dropped");
        assertTrue(written.contains(COOLDOWN_ID.toString()), "the cooldown that could be read was dropped");
        assertTrue(written.contains(Cooldown.Type.Home.name()), "the new cooldown was not written");
    }

    @Test
    @DisplayName("the cooldown file stays the list of cooldowns it always was")
    void theFileFormatIsUnchanged() throws IOException {
        final Cooldown cooldown = new Cooldown(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L);

        provider.createCooldown(null, COOLDOWN_ID.toString(), Cooldown.Type.Request.name(), OWNER_ID.toString(), new Timestamp(9999999999999L));

        final Gson gson = TestGson.gson();
        final Type listType = new TypeToken<List<Cooldown>>(){}.getType();
        final String expected = gson.toJson(Arrays.asList(cooldown), listType);

        assertEquals(expected, new String(Files.readAllBytes(cooldownFile()), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("an empty cooldown file holds no cooldowns")
    void anEmptyFileHoldsNoCooldowns() throws IOException {
        assertTrue(provider.getAllCooldowns(null).isEmpty());
    }

    @Test
    @DisplayName("a cooldown file that cannot be read is backed up byte for byte")
    void aBrokenCooldownFileIsBackedUpByteForByte() throws IOException {
        final String corrupt = "{ this is not json";
        writeCooldowns(corrupt);

        assertThrows(IOException.class, () -> provider.getAllCooldowns(null));

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "cooldowns");
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));

        // The original is still there, with the same bytes.
        assertTrue(Files.exists(cooldownFile()));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(cooldownFile()));
    }

    @Test
    @DisplayName("a cooldown file that does not hold a list is backed up")
    void aCooldownFileThatIsNotAListIsBackedUp() throws IOException {
        final String unusable = "{ \"not\": \"a list\" }";
        writeCooldowns(unusable);

        assertThrows(IOException.class, () -> provider.getAllCooldowns(null));

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "cooldowns");
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));
        assertArrayEquals(unusable.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(cooldownFile()));
    }

    @Test
    @DisplayName("a cooldown file with a broken entry is backed up")
    void aCooldownFileWithABrokenEntryIsBackedUp() throws IOException {
        final String broken = "[\n"
                + "  \"this is not a cooldown\",\n"
                + cooldownEntry(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L) + "\n"
                + "]\n";
        writeCooldowns(broken);

        provider.getAllCooldowns(null);

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "cooldowns");
        assertArrayEquals(broken.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(backup));
        assertArrayEquals(broken.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(cooldownFile()));
    }

    @Test
    @DisplayName("the cooldown backup lands outside the data folder and keeps the file name")
    void theBackupLandsOutsideTheDataFolder() throws IOException {
        writeCooldowns("{ this is not json");

        assertThrows(IOException.class, () -> provider.getAllCooldowns(null));

        final Path backup = CorruptBackups.onlyBackup(pluginFolder(), "cooldowns");

        assertEquals("cooldowns.json", backup.getFileName().toString());
        assertEquals("cooldowns", backup.getParent().getFileName().toString());
        assertTrue(backup.getParent().getParent().getFileName().toString().matches("\\d{8}-\\d{6}"),
                "the backup folder is not named after a timestamp: " + backup);
        assertEquals("corrupt", backup.getParent().getParent().getParent().getFileName().toString());
        assertEquals("backups", backup.getParent().getParent().getParent().getParent().getFileName().toString());
        assertEquals(pluginFolder(), backup.getParent().getParent().getParent().getParent().getParent());
        assertTrue(backup.startsWith(pluginFolder()));
    }

    @Test
    @DisplayName("the log names the cooldown file, the reason and the backup")
    void theLogNamesTheFileTheReasonAndTheBackup() throws IOException {
        writeCooldowns("{ this is not json");

        try (LogCapture log = new LogCapture()) {
            assertThrows(IOException.class, () -> provider.getAllCooldowns(null));

            assertTrue(log.contains("cooldowns.json"), "the log did not name the file");
            assertTrue(log.contains("could not be read"), "the log did not say what went wrong");
            assertTrue(log.contains("backups"), "the log did not say where the backup is");
        }
    }

    @Test
    @DisplayName("the log says which entry of the cooldown file could not be read")
    void theLogSaysWhichEntryCouldNotBeRead() throws IOException {
        writeCooldowns("[\n"
                + "  \"this is not a cooldown\",\n"
                + cooldownEntry(COOLDOWN_ID, Cooldown.Type.Request, OWNER_ID, 9999999999999L) + "\n"
                + "]\n");

        try (LogCapture log = new LogCapture()) {
            provider.getAllCooldowns(null);

            assertTrue(log.contains("position 0"), "the log did not say which entry was at fault");
        }
    }

    @Test
    @DisplayName("a cooldown backup that cannot be written is reported and nothing is lost")
    void aBackupThatCannotBeWrittenIsReportedAndNothingIsLost() throws IOException {
        final String corrupt = "{ this is not json";
        writeCooldowns(corrupt);

        Files.createFile(pluginFolder().resolve("backups"));

        try (LogCapture log = new LogCapture()) {
            assertThrows(IOException.class, () -> provider.getAllCooldowns(null));

            assertTrue(log.contains("could not be written"), "the failure to back the file up was not reported");
            assertTrue(log.contains("cooldowns.json"), "the log did not name the file");
        }

        assertTrue(Files.exists(cooldownFile()));
        assertArrayEquals(corrupt.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(cooldownFile()));
    }

    @Test
    @DisplayName("the same cooldown corruption is backed up once, not once per read")
    void theSameCorruptionIsBackedUpOnce() throws IOException {
        writeCooldowns("{ this is not json");

        assertThrows(IOException.class, () -> provider.getAllCooldowns(null));
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "cooldowns").size());

        assertThrows(IOException.class, () -> provider.getAllCooldowns(null));
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "cooldowns").size());

        // A restart: a new provider over the same folder.
        final CooldownJsonProvider restarted = new CooldownJsonProvider(dataFolder().toFile(), pluginFolder().toFile());
        assertThrows(IOException.class, () -> restarted.getAllCooldowns(null));
        assertEquals(1, CorruptBackups.backupFiles(pluginFolder(), "cooldowns").size());
    }
}
