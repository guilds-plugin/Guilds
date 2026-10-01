/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.database;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.Guild;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opens a real database and runs the real adapters against it.
 *
 * <p>Every other SQL test in this suite works against mocks, which cannot tell you whether the adapter
 * lifecycle runs at all: whether the pool opens, whether the schema is created, whether a row written
 * through the adapter is readable by it. Those are the failures that hurt, and they are all invisible to a
 * mock. {@code SQLite} is the backend that needs no server, so this runs in CI without Docker.
 *
 * <p>Deliberately on the real production path: the configured storage type, the public
 * {@link DatabaseAdapter} constructor, and the JDBC URL the plugin hardcodes. Nothing is substituted for
 * convenience, so a change that breaks the SQL path breaks this.
 *
 * <p>{@code SQLite} is the only SQL backend covered. {@code MYSQL} and {@code MARIADB} have no integration
 * test, because they need a server, so a driver or dialect regression on those two would not be caught by
 * anything in this suite. The SQL they share — the JDBI layer and the pool — is what this exercises.
 */
class SqliteSmokeTest {

    /**
     * The URL {@code DatabaseManager} builds for {@code SQLITE}, relative to the working directory.
     *
     * <p>Used as given rather than pointed somewhere temporary, so this test is also the thing that catches
     * that URL moving.
     */
    private static final String SQLITE_PATH = "plugins/Guilds/guilds.db";

    private static Path createdDirectory;
    private static Path databaseFile;

    @BeforeAll
    static void prepareDirectory() throws IOException {
        installGson();

        // The JDBC driver does not create parent directories.
        final Path directory = new File(SQLITE_PATH).toPath().getParent();
        Files.createDirectories(directory);
        createdDirectory = directory;
        databaseFile = new File(SQLITE_PATH).toPath();

        Files.deleteIfExists(databaseFile);
    }

    @AfterAll
    static void removeDatabase() throws IOException {
        if (databaseFile != null) {
            Files.deleteIfExists(databaseFile);
        }
        if (createdDirectory != null) {
            Files.deleteIfExists(createdDirectory);
            Files.deleteIfExists(createdDirectory.getParent());
        }
    }

    private static void installGson() {
        try {
            final java.lang.reflect.Field field = Guilds.class.getDeclaredField("gson");
            field.setAccessible(true);
            field.set(null, new com.google.gson.GsonBuilder().setPrettyPrinting().create());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not install the plugin's Gson for the row mapper", e);
        }
    }

    /** Real settings, with the storage type the plugin would have been configured with. */
    private static SettingsManager settingsForSqlite() {
        // The real configuration data, so the properties carry the plugin's own defaults rather than
        // whatever a bare builder would invent.
        final SettingsManager settings = SettingsManagerBuilder
                .withYamlFile(new File("target/unused-sqlite-smoke-config.yml"))
                .configurationData(me.glaremasters.guilds.configuration.GuildConfigurationBuilder.buildConfigurationData())
                .create();
        settings.setProperty(me.glaremasters.guilds.configuration.sections.StorageSettings.STORAGE_TYPE, "sqlite");
        settings.setProperty(me.glaremasters.guilds.configuration.sections.StorageSettings.SQL_TABLE_PREFIX, "smoke_");
        return settings;
    }

    /** A connected adapter, on the real path: configured type, public constructor, real pool. */
    private static DatabaseAdapter open() throws IOException {
        final Guilds plugin = Mockito.mock(Guilds.class);
        return new DatabaseAdapter(plugin, settingsForSqlite());
    }

    @Test
    @Timeout(60)
    @DisplayName("the SQL stack is loadable on the JVM running this test")
    void theSqlStackIsLoadableOnTheJvmRunningThisTest() throws ClassNotFoundException, IOException {
        // The regression guard for the jdbi version. jdbi 3.50 raised its bytecode to Java 17, and a Java
        // 11 JVM cannot load that at all, so the whole SQL backend went with it.
        //
        // `Class.forName` only fails on a JVM too old for the class, which would miss the problem on JDK 21.
        // So the class file is read as bytes and its version checked, which fails on every JVM if the pin
        // ever moves to a Java 17 build of jdbi again.
        assertEquals(55, classFileMajorVersion("org.jdbi.v3.core.Jdbi"), "jdbi3-core must stay Java 11");
        assertEquals(55, classFileMajorVersion("org.jdbi.v3.sqlobject.SqlObject"), "jdbi3-sqlobject must stay Java 11");

        assertNotNull(Class.forName("org.jdbi.v3.core.Jdbi"));
        assertNotNull(Class.forName("org.jdbi.v3.core.Handle"));
        assertNotNull(Class.forName("com.zaxxer.hikari.HikariDataSource"));
        assertNotNull(Class.forName("org.sqlite.JDBC"));
    }

    /**
     * The class file version of a class on this classpath, 55 being Java 11.
     *
     * <p>Read from the jar rather than inferred from the running JVM, so the assertion holds on any of them.
     */
    private static int classFileMajorVersion(String className) throws IOException {
        final String resource = className.replace('.', '/') + ".class";
        try (InputStream in = SqliteSmokeTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, resource + " should be on the test classpath");
            final byte[] header = new byte[8];
            assertEquals(8, in.readNBytes(header, 0, 8), "truncated class file for " + className);
            return ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------

    @Test
    @Timeout(120)
    @DisplayName("the configured backend opens a pool and reports itself connected")
    void theConfiguredBackendOpensAPoolAndReportsItselfConnected() throws IOException {
        try (DatabaseAdapter adapter = open()) {
            assertEquals(DatabaseBackend.SQLITE, adapter.getBackend());
            assertTrue(adapter.isConnected(), "the pool should be open");
            assertNotNull(adapter.getDatabaseManager(), "a real pool is what this test is about");
            assertTrue(adapter.getDatabaseManager().isConnected());
        }
    }

    @Test
    @Timeout(120)
    @DisplayName("the schema is created in a database that really exists")
    void theSchemaIsCreatedInADatabaseThatReallyExists() throws IOException {
        // A mock cannot show that the DDL ran. This asks the database itself, over a second connection,
        // whether the table is there and the file is on disk.
        try (DatabaseAdapter adapter = open()) {
            final Integer tables = countTables(adapter, "smoke_guild");
            assertEquals(1, tables, "smoke_guild should exist in the real database");
        }

        assertTrue(Files.exists(databaseFile), "the database file should be on disk at " + SQLITE_PATH);
        assertTrue(Files.size(databaseFile) > 0, "the database file should not be empty");
    }

    @Test
    @Timeout(120)
    @DisplayName("every collection's container is created")
    void everyCollectionsContainerIsCreated() throws IOException {
        try (DatabaseAdapter adapter = open()) {
            for (String table : List.of("smoke_guild", "smoke_challenge", "smoke_arena", "smoke_cooldowns")) {
                assertEquals(1, countTables(adapter, table), table + " should exist");
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Read and write, through the real adapters
    // ---------------------------------------------------------------------------------------

    @Test
    @Timeout(120)
    @DisplayName("a guild written through the adapter is read back through the adapter")
    void aGuildWrittenThroughTheAdapterIsReadBackThroughTheAdapter() throws IOException {
        try (DatabaseAdapter adapter = open()) {
            final GuildAdapter guilds = adapter.getGuildAdapter();
            final String id = UUID.randomUUID().toString();

            final Map<String, String> batch = new LinkedHashMap<>();
            batch.put(id, guildData(id, "Test Guild"));

            assertFalse(guilds.guildExists(id), "a fresh database has no such guild");
            guilds.saveSerialized(batch);

            assertTrue(guilds.guildExists(id), "the guild should be there after the write");
            assertTrue(guilds.getAllGuildIds().contains(id), "the id should be listed");

            final Guild loaded = guilds.getGuild(id);
            assertNotNull(loaded, "the guild should be readable");
            assertEquals(UUID.fromString(id), loaded.getId());

            guilds.deleteGuild(id);
            assertFalse(guilds.guildExists(id), "the guild should be gone after the delete");
        }
    }

    @Test
    @Timeout(120)
    @DisplayName("a batch write is visible to a second adapter on the same file")
    void aBatchWriteIsVisibleToASecondAdapterOnTheSameFile() throws IOException {
        // Persistence across an adapter, which is the thing the save path depends on: a write that only the
        // writing process can see is not persistence.
        final String id = UUID.randomUUID().toString();
        final Map<String, String> batch = new LinkedHashMap<>();
        batch.put(id, guildData(id, "Persisted"));

        try (DatabaseAdapter first = open()) {
            first.getGuildAdapter().saveSerialized(batch);
        }

        try (DatabaseAdapter second = open()) {
            assertTrue(second.getGuildAdapter().guildExists(id), "a reopened database should have the guild");
            assertNotNull(second.getGuildAdapter().getGuild(id));
            second.getGuildAdapter().deleteGuild(id);
        }
    }

    @Test
    @Timeout(120)
    @DisplayName("a cooldown survives a write and a read")
    void aCooldownSurvivesAWriteAndARead() throws IOException {
        try (DatabaseAdapter adapter = open()) {
            final UUID owner = UUID.randomUUID();
            final long expiry = System.currentTimeMillis() + 600000L;
            // Id first, then type, then the owner. Read back in the same order by CooldownRowMapper.
            final Cooldown cooldown = new Cooldown(UUID.randomUUID(), Cooldown.Type.Home, owner, expiry);

            adapter.getCooldownAdapter().saveCooldowns(Collections.singletonList(cooldown));

            final List<Cooldown> all = adapter.getCooldownAdapter().getAllCooldowns();
            final Cooldown loaded = all.stream()
                    .filter(c -> c.getCooldownOwner().equals(owner))
                    .findFirst()
                    .orElse(null);
            assertNotNull(loaded, "the cooldown should be readable");
            assertEquals(expiry, loaded.getCooldownExpiry(), "the expiry is what a migration has to reproduce");

            adapter.getCooldownAdapter().deleteCooldown(loaded);
            assertTrue(adapter.getCooldownAdapter().getAllCooldowns().stream()
                    .noneMatch(c -> c.getCooldownOwner().equals(owner)), "the cooldown should be gone");
        }
    }

    // ---------------------------------------------------------------------------------------
    // The lifecycle this branch changed
    // ---------------------------------------------------------------------------------------

    @Test
    @Timeout(120)
    @DisplayName("closing the adapter closes the pool it opened")
    void closingTheAdapterClosesThePoolItOpened() throws IOException {
        // The fix in this branch: a failed setup has to give its pool back, and `close` has to be safe to
        // call more than once. Both were only ever checked against a mock until now.
        final DatabaseAdapter adapter = open();
        assertTrue(adapter.getDatabaseManager().isConnected());

        adapter.close();
        assertFalse(adapter.getDatabaseManager().isConnected(), "the pool should be shut");

        assertDoesNotThrowClose(adapter);
    }

    @Test
    @Timeout(120)
    @DisplayName("an adapter left unclosed does not stop the next one opening")
    void anAdapterLeftUnclosedDoesNotStopTheNextOneOpening() throws IOException {
        // A leaked pool would show up here as the second open failing on a locked file.
        final DatabaseAdapter leaked = open();
        try {
            assertNotNull(leaked.getDatabaseManager());

            try (DatabaseAdapter second = open()) {
                assertTrue(second.isConnected());
            }
        } finally {
            // Closed here rather than left to the end of the class, so cleanup is not racing an open handle.
            leaked.close();
        }
    }

    private static void assertDoesNotThrowClose(DatabaseAdapter adapter) {
        final List<RuntimeException> thrown = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            try {
                adapter.close();
            } catch (RuntimeException e) {
                thrown.add(e);
            }
        }
        assertTrue(thrown.isEmpty(), "closing twice should be safe, but got " + thrown);
    }

    /** How many tables of that name the real database reports, asked over its own connection. */
    private static int countTables(DatabaseAdapter adapter, String name) {
        return adapter.getDatabaseManager().getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = :t")
                        .bind("t", name)
                        .mapTo(Integer.class)
                        .findOne()
                        .orElse(0));
    }

    /** A guild payload the row mapper can read back into a {@link Guild}. */
    private static String guildData(String id, String name) {
        final Guild guild = new Guild(UUID.fromString(id));
        guild.setName(name);
        guild.setVaults(new ArrayList<>());
        return Guilds.getGson().toJson(guild);
    }

    @Test
    @Timeout(120)
    @DisplayName("the pool is not handed out after the adapter is closed")
    void thePoolIsNotHandedOutAfterTheAdapterIsClosed() throws IOException {
        final DatabaseAdapter adapter = open();
        adapter.close();

        // `getJdbi` still returns a handle supplier, so the failure has to come from the pool underneath it.
        // Asserted as a thrown error of some kind rather than a message, because which layer reports it is
        // HikariCP's business, not the plugin's.
        assertThrows(RuntimeException.class, () -> adapter.getDatabaseManager().getJdbi()
                .withHandle(handle -> handle.createQuery("SELECT 1").mapTo(Integer.class).findOne()));
    }

    @Test
    @Timeout(120)
    @DisplayName("a closed adapter reports itself not connected")
    void aClosedAdapterReportsItselfNotConnected() throws IOException {
        final DatabaseAdapter adapter = open();
        assertTrue(adapter.isConnected());
        adapter.close();
        assertFalse(adapter.isConnected(), "a closed adapter is not connected");
    }
}
