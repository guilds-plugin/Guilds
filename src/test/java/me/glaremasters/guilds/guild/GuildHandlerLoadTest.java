package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers what the plugin does when the guild data cannot be read.
 *
 * <p>{@link GuildHandler} loads every guild during construction, and the load used to swallow an
 * {@link IOException}: the handler logged it and carried on with an empty or half-filled guild
 * map. The plugin lifecycle never heard about it, so the server came up as if nothing was wrong,
 * and the first save then wrote that empty map back over the data the plugin had failed to read.
 *
 * <p>These tests pin the other half of that contract down too: a load that works must still work,
 * and a load that fails must be visible to whoever starts the plugin.
 */
class GuildHandlerLoadTest {

    /** The id of the guild the fake database hands back. */
    private static final String GUILD_ID = "11111111-1111-1111-1111-111111111111";

    /** The id of the guild whose stored data cannot be used. */
    private static final String BROKEN_ID = "22222222-2222-2222-2222-222222222222";

    @TempDir
    Path tempDir;

    private Guilds plugin;
    private GuildAdapter guildAdapter;

    @BeforeEach
    void setUp() throws IOException {
        writeRoles();
        writeTiers();

        plugin = mock(Guilds.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());

        final DatabaseAdapter database = mock(DatabaseAdapter.class);
        guildAdapter = mock(GuildAdapter.class);
        when(database.getGuildAdapter()).thenReturn(guildAdapter);
        when(plugin.getDatabase()).thenReturn(database);
    }

    /**
     * Writes the roles table the handler reads at startup.
     *
     * <p>The plugin generates this file on the server; the handler reads it with the plain YAML
     * reader rather than through the settings manager, so the test has to put one there.
     *
     * @throws IOException if the file cannot be written
     */
    private void writeRoles() throws IOException {
        Files.write(tempDir.resolve("roles.yml"), String.join("\n",
                "roles:",
                "  0:",
                "    name: GuildMaster",
                "    permission-node: guilds.roles.master",
                "  1:",
                "    name: Officer",
                "    permission-node: guilds.roles.officer",
                ""
        ).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes the tiers table the handler reads at startup.
     *
     * @throws IOException if the file cannot be written
     */
    private void writeTiers() throws IOException {
        Files.write(tempDir.resolve("tiers.yml"), String.join("\n",
                "tiers:",
                "  list:",
                "    1:",
                "      level: 1",
                "      name: Bronze",
                "      cost: 0.0",
                "      max-members: 15",
                "      vault-amount: 1",
                "      mob-xp-multiplier: 1.0",
                "      damage-multiplier: 1.0",
                "      max-bank-balance: 10000.0",
                "      members-to-rankup: 0",
                "      max-allies: 5",
                "      use-buffs: true",
                "      permissions: []",
                ""
        ).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return a guild as the fake database hands it over
     */
    private Guild guild() {
        final Guild guild = new Guild(UUID.fromString(GUILD_ID));
        guild.setName("GoodGuild");
        guild.setTier(GuildTier.builder().level(1).name("Bronze").build());
        guild.setMembers(new ArrayList<>());
        return guild;
    }

    /**
     * @return a guild whose stored data cannot be used, the way a hand-edited file cannot be
     */
    private Guild brokenGuild() {
        final Guild guild = new Guild(UUID.fromString(BROKEN_ID));
        guild.setName("BrokenGuild");
        guild.setTier(GuildTier.builder().level(1).name("Bronze").build());

        // Gson leaves a field null when the stored file does not have it, and the handler reads a
        // member's role without a null check.
        final GuildMember member = new GsonBuilder().create().fromJson(
                "{\"uuid\": \"33333333-3333-3333-3333-333333333333\"}", GuildMember.class);
        guild.setMembers(Collections.singletonList(member));

        return guild;
    }

    @Test
    @DisplayName("a guild data read failure is reported to whoever started the plugin")
    void loadFailureIsNotSwallowed() throws IOException {
        when(guildAdapter.getAllGuilds()).thenThrow(new IOException("the database is unreachable"));

        final SettingsManager settings = mock(SettingsManager.class);

        assertThrows(IOException.class, () -> new GuildHandler(plugin, settings),
                "a failed guild load has to reach the plugin lifecycle, or the plugin starts up "
                        + "with an empty guild map and overwrites the data it could not read");
    }

    @Test
    @DisplayName("a guild data read failure is not swallowed even when nothing else failed")
    void loadFailureIsNotSwallowedSilently() throws IOException {
        when(guildAdapter.getAllGuilds()).thenThrow(new IOException("the database is unreachable"));

        final SettingsManager settings = mock(SettingsManager.class);

        // The handler used to log the failure and return normally. Nothing was thrown, so the
        // plugin started up and the scheduled save wrote the empty map back over the guild data.
        final IOException thrown = assertThrows(IOException.class, () -> new GuildHandler(plugin, settings));
        assertEquals("the database is unreachable", thrown.getMessage(),
                "the original failure has to reach the plugin lifecycle unchanged");
    }

    @Test
    @DisplayName("a load where every guild file is unreadable stops the plugin")
    void aLoadWhereNothingCanBeReadStopsThePlugin() throws IOException {
        // What the JSON provider does when it finds files and cannot read one of them.
        when(guildAdapter.getAllGuilds()).thenThrow(new IOException(
                "None of the 3 files in /plugins/Guilds/data could be read as a guild, so the guild data could not be loaded."));

        final SettingsManager settings = mock(SettingsManager.class);

        assertThrows(IOException.class, () -> new GuildHandler(plugin, settings),
                "a server that comes up with no guilds at all hides that the guild data could not be read");
    }

    @Test
    @DisplayName("a guild data load that works still loads the guilds")
    void successfulLoadStillWorks() throws IOException {
        when(guildAdapter.getAllGuilds()).thenReturn(Collections.singletonList(guild()));

        final SettingsManager settings = mock(SettingsManager.class);
        final GuildHandler handler = new GuildHandler(plugin, settings);

        assertEquals(1, handler.getGuildsSize(), "a working load has to keep working");
    }

    @Test
    @DisplayName("one guild that cannot be set up does not cost the server the other guilds")
    void oneBrokenGuildDoesNotStopTheOthers() throws IOException {
        when(guildAdapter.getAllGuilds()).thenReturn(Arrays.asList(guild(), brokenGuild()));

        final SettingsManager settings = mock(SettingsManager.class);
        final GuildHandler handler = new GuildHandler(plugin, settings);

        assertEquals(1, handler.getGuildsSize(), "the guild that reads has to stay available");
        assertNotNull(handler.getGuild(UUID.fromString(GUILD_ID)));
        assertNull(handler.getGuild(UUID.fromString(BROKEN_ID)),
                "the guild that could not be read must not be in the running plugin");
    }

    @Test
    @DisplayName("when no guild at all can be set up, the plugin refuses to start")
    void noUsableGuildIsFatal() throws IOException {
        when(guildAdapter.getAllGuilds()).thenReturn(Collections.singletonList(brokenGuild()));

        final SettingsManager settings = mock(SettingsManager.class);

        assertThrows(IOException.class, () -> new GuildHandler(plugin, settings),
                "a plugin that came up with no guilds at all would write that back over the data");
    }
}
