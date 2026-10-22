package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.utils.GuildInputValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link GuildHandler#getGuild(String)} finding a guild by the name a player types.
 *
 * <p>Duplicate-name detection normalises a name before comparing it: the alternate colour codes
 * are translated, every colour code is stripped, and the rest is compared without regard to case.
 * {@code /guild create} therefore refuses "raiders" when a guild called "Raiders" already exists.
 * The lookup used a different rule: it stripped the colour codes from the stored name and compared
 * the result with the typed name exactly. A player who typed "raiders", or who typed a name that
 * still carried its colour codes, got "that guild does not exist" for a guild the server itself
 * said was already taken.
 *
 * <p>Every command that names a guild goes through the ACF guild context, which resolves the
 * argument with this method: {@code /guild accept}, {@code /guild decline}, {@code /guild ally},
 * {@code /guild war challenge} and every {@code /guild admin ... --other} command.
 *
 * <p>Two guilds whose visible names differ only by case or by colour are a broken state that
 * duplicate detection prevents but legacy data can still hold. The lookup must not pick one of them
 * by accident, so an ambiguous name resolves to nothing.
 */
class GuildNameLookupTest {

    /** The section sign, written as an escape so the file stays pure ASCII. */
    private static final String SECTION = "\u00A7";

    @TempDir
    Path tempDir;

    private static Guild guildNamed(String name) {
        final Guild guild = new Guild(UUID.randomUUID());
        guild.setName(name);
        return guild;
    }

    /**
     * Builds a handler holding the given guilds.
     *
     * <p>The plugin instance is a mock, so the handler reads an empty data folder and an empty
     * database. The guilds are added afterwards through the same public method the plugin uses when
     * a guild is created at runtime.
     *
     * @param guilds the guilds the handler should hold
     * @return a handler holding exactly those guilds
     * @throws IOException if the data folder cannot be written
     */
    private GuildHandler handlerWith(Guild... guilds) throws IOException {
        final Path dataFolder = Files.createDirectories(tempDir.resolve("data"));
        Files.write(dataFolder.resolve("roles.yml"), Arrays.asList(
                "roles:",
                "  0:",
                "    name: \"GuildMaster\"",
                "    permission-node: \"guilds.roles.master\"",
                "    permissions:",
                "      chat: true",
                ""
        ));
        Files.write(dataFolder.resolve("tiers.yml"), Arrays.asList(
                "tiers:",
                "  list:",
                "    1:",
                "      level: 1",
                "      name: \"Bronze\"",
                "      cost: 0.0",
                "      max-members: 10",
                "      vault-amount: 1",
                "      mob-xp-multiplier: 1.0",
                "      damage-multiplier: 1.0",
                "      max-bank-balance: 10000.0",
                "      members-to-rankup: 0",
                "      max-allies: 5",
                "      use-buffs: true",
                "      permissions: []",
                ""
        ));

        final GuildAdapter guildAdapter = Mockito.mock(GuildAdapter.class);
        Mockito.when(guildAdapter.getAllGuilds()).thenReturn(Collections.emptyList());

        final DatabaseAdapter database = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(database.getGuildAdapter()).thenReturn(guildAdapter);

        final Guilds plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getDatabase()).thenReturn(database);

        final GuildHandler handler = new GuildHandler(plugin, Mockito.mock(SettingsManager.class));
        for (final Guild guild : guilds) {
            handler.addGuild(guild);
        }
        return handler;
    }

    @Test
    @DisplayName("a guild is found whatever case the name is typed in")
    void findsGuildRegardlessOfCase() throws IOException {
        final Guild guild = guildNamed("Raiders");
        final GuildHandler handler = handlerWith(guild);

        assertSame(guild, handler.getGuild("Raiders"));
        assertSame(guild, handler.getGuild("raiders"));
        assertSame(guild, handler.getGuild("RAIDERS"));
    }

    @Test
    @DisplayName("a guild is found by its plain name and by its coloured name")
    void findsColouredGuildByPlainAndColouredName() throws IOException {
        final Guild guild = guildNamed(SECTION + "cRaiders");
        final GuildHandler handler = handlerWith(guild);

        assertSame(guild, handler.getGuild("Raiders"));
        assertSame(guild, handler.getGuild(SECTION + "cRaiders"));
        assertSame(guild, handler.getGuild("&cRaiders"));
        assertSame(guild, handler.getGuild("raiders"));
    }

    @Test
    @DisplayName("a name that belongs to no guild resolves to nothing")
    void unknownNameResolvesToNothing() throws IOException {
        final GuildHandler handler = handlerWith(guildNamed("Raiders"));

        assertNull(handler.getGuild("Rangers"));
        assertNull(handler.getGuild(""));
    }

    @Test
    @DisplayName("duplicate detection and lookup agree on the same name")
    void duplicateDetectionAndLookupAgree() throws IOException {
        final Guild guild = guildNamed("Raiders");
        final GuildHandler handler = handlerWith(guild);
        final List<Guild> guilds = Collections.singletonList(guild);

        // A name the server refuses to hand out is a name it has to be able to resolve.
        assertTrue(GuildInputValidator.isNameTaken("raiders", guilds));
        assertNotNull(handler.getGuild("raiders"));

        assertTrue(GuildInputValidator.isNameTaken("&cRaiders", guilds));
        assertNotNull(handler.getGuild("&cRaiders"));

        assertTrue(GuildInputValidator.isNameTaken(SECTION + "cRaiders", guilds));
        assertNotNull(handler.getGuild(SECTION + "cRaiders"));

        // And a name that is free is free for both.
        assertFalse(GuildInputValidator.isNameTaken("Rangers", guilds));
        assertNull(handler.getGuild("Rangers"));
    }

    @Test
    @DisplayName("two guilds that differ only by case are not resolved by an ambiguous name")
    void refusesToResolveAnAmbiguousName() throws IOException {
        final Guild upper = guildNamed("Raiders");
        final Guild lower = guildNamed("raiders");
        final GuildHandler handler = handlerWith(upper, lower);

        // Each of them still answers to the name it actually carries.
        assertSame(upper, handler.getGuild("Raiders"));
        assertSame(lower, handler.getGuild("raiders"));

        // A name that fits both of them fits neither, rather than one of them by accident.
        assertNull(handler.getGuild("RAIDERS"));
        assertNull(handler.getGuild("RaiderS"));
    }

    @Test
    @DisplayName("two guilds that differ only by colour are not resolved by an ambiguous name")
    void refusesToResolveAColourAmbiguousName() throws IOException {
        final Guild red = guildNamed(SECTION + "cRaiders");
        final Guild gold = guildNamed(SECTION + "4Raiders");
        final GuildHandler handler = handlerWith(red, gold);

        assertNull(handler.getGuild("Raiders"));
        assertNull(handler.getGuild(SECTION + "cRaiders"));
    }

    @Test
    @DisplayName("the guild name check agrees with the lookup")
    void nameCheckAgreesWithLookup() throws IOException {
        final GuildHandler handler = handlerWith(guildNamed("Raiders"));

        assertTrue(handler.checkGuildNames("raiders"));
        assertTrue(handler.checkGuildNames("&cRaiders"));
        assertFalse(handler.checkGuildNames("Rangers"));
    }
}
