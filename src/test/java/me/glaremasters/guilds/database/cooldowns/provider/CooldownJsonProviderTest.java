package me.glaremasters.guilds.database.cooldowns.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.glaremasters.guilds.cooldowns.Cooldown;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what the JSON cooldown storage does with an entry it cannot read.
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
        provider = new CooldownJsonProvider(tempDir.toFile());
        provider.createContainer(null);
    }

    private Path cooldownFile() {
        return tempDir.resolve("cooldowns.json");
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
}
