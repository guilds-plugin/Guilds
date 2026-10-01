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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the two things an adapter has to get right before a migration is allowed to touch it: whether the
 * destination is really somewhere else, and whether a failed setup gives its connection pool back.
 */
class DatabaseAdapterSetupTest {

    private Guilds plugin;
    private SettingsManager settings;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Guilds.class);
        settings = Mockito.mock(SettingsManager.class);
        Mockito.when(settings.getProperty(StorageSettings.SQL_TABLE_PREFIX)).thenReturn("guilds");
        Mockito.when(settings.getProperty(StorageSettings.SQL_HOST)).thenReturn("db.internal");
        Mockito.when(settings.getProperty(StorageSettings.SQL_PORT)).thenReturn("3306");
        Mockito.when(settings.getProperty(StorageSettings.SQL_DATABASE)).thenReturn("guilds");
    }

    /**
     * An adapter whose pool was opened against the settings as they currently stand.
     *
     * <p>Set by field rather than by connecting, so these tests need no database. {@link SqliteSmokeTest}
     * covers what a real one does; these cover the identity comparison, which needs none.
     */
    private DatabaseAdapter adapterOn(DatabaseBackend backend) throws Exception {
        final DatabaseAdapter adapter = new DatabaseAdapter(plugin, settings, false);
        set(adapter, "backend", backend);
        set(adapter, "sqlTablePrefix", "guilds");
        set(adapter, "sqlHost", "db.internal");
        set(adapter, "sqlPort", "3306");
        set(adapter, "sqlDatabase", "guilds");
        return adapter;
    }

    /** The same, holding a stand-in pool. */
    private DatabaseAdapter adapterWithPool(DatabaseBackend backend) throws Exception {
        final DatabaseAdapter adapter = adapterOn(backend);
        set(adapter, "databaseManager", Mockito.mock(DatabaseManager.class));
        return adapter;
    }

    private static DatabaseManager managerOf(DatabaseAdapter adapter) throws Exception {
        final Field field = DatabaseAdapter.class.getDeclaredField("databaseManager");
        field.setAccessible(true);
        return (DatabaseManager) field.get(adapter);
    }

    private static void set(DatabaseAdapter adapter, String field, Object value) throws Exception {
        final Field declared = DatabaseAdapter.class.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(adapter, value);
    }

    // ---------------------------------------------------------------------------------------
    // Is the destination somewhere else?
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("migrating between the two server SQL backends is recognised as the same storage")
    void migratingBetweenTheTwoServerSqlBackendsIsRecognisedAsTheSameStorage() throws Exception {
        // Both read one set of properties, so this is the same host, database and tables seen twice. The
        // migration would then be writing over the rows it is reading, and a failure partway through would
        // have deleted cooldowns out of the live tables with no second copy to restore from.
        assertTrue(adapterOn(DatabaseBackend.MYSQL).sharesStorageWith(DatabaseBackend.MARIADB));
        assertTrue(adapterOn(DatabaseBackend.MARIADB).sharesStorageWith(DatabaseBackend.MYSQL));
        assertTrue(adapterOn(DatabaseBackend.MARIADB).sharesStorageWith(DatabaseBackend.MARIADB));
        assertTrue(adapterOn(DatabaseBackend.MYSQL).sharesStorageWith(DatabaseBackend.MYSQL));
    }

    @Test
    @DisplayName("repointing the storage settings and reloading makes a MySQL to MariaDB move a real move")
    void repointingTheStorageSettingsAndReloadingMakesAMySqlToMariaDbMoveARealMove() throws Exception {
        // The pool was opened against `db.internal`. The operator edits the config to a new server and
        // reloads, which refreshes the settings object this adapter is still holding. Refusing here would
        // leave a server with no way to move between the two SQL backends at all.
        final DatabaseAdapter adapter = adapterOn(DatabaseBackend.MYSQL);
        Mockito.when(settings.getProperty(StorageSettings.SQL_HOST)).thenReturn("db.new");

        assertFalse(adapter.sharesStorageWith(DatabaseBackend.MARIADB));
    }

    @Test
    @DisplayName("repointing only the table prefix is enough to make it a different set of tables")
    void repointingOnlyTheTablePrefixIsEnoughToMakeItADifferentSetOfTables() throws Exception {
        final DatabaseAdapter adapter = adapterOn(DatabaseBackend.MYSQL);
        Mockito.when(settings.getProperty(StorageSettings.SQL_TABLE_PREFIX)).thenReturn("other");

        assertFalse(adapter.sharesStorageWith(DatabaseBackend.MARIADB));
    }

    @Test
    @DisplayName("migrating to a different kind of storage is not blocked")
    void migratingToADifferentKindOfStorageIsNotBlocked() throws Exception {
        // The false-positive direction matters as much: refusing these would leave a server with no way to
        // leave SQL or JSON at all.
        assertFalse(adapterOn(DatabaseBackend.MYSQL).sharesStorageWith(DatabaseBackend.SQLITE));
        assertFalse(adapterOn(DatabaseBackend.SQLITE).sharesStorageWith(DatabaseBackend.MARIADB));
        assertFalse(adapterOn(DatabaseBackend.MYSQL).sharesStorageWith(DatabaseBackend.JSON));
        assertFalse(adapterOn(DatabaseBackend.JSON).sharesStorageWith(DatabaseBackend.MYSQL));
        assertFalse(adapterOn(DatabaseBackend.JSON).sharesStorageWith(DatabaseBackend.JSON));
    }

    // ---------------------------------------------------------------------------------------
    // Does a failed setup give the pool back?
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a setup that fails partway closes the pool it had already opened")
    void aSetupThatFailsPartwayClosesThePoolItHadAlreadyOpened() throws Exception {
        // The pool is open before the first `createContainer`, and a failure from there on — a table the
        // database user cannot create, a missing driver — used to abandon it. The only caller is
        // `cloneWith`, which discards the adapter, so nothing else could ever close it.
        try (MockedConstruction<DatabaseManager> construction = Mockito.mockConstruction(DatabaseManager.class)) {
            final DatabaseAdapter source = adapterOn(DatabaseBackend.MYSQL);

            // Every provider call is unstubbed, so the first `createContainer` throws the way a real
            // permission or driver failure would.
            assertThrows(IOException.class, () -> source.cloneWith(DatabaseBackend.MARIADB));

            assertFalse(construction.constructed().isEmpty(), "the destination should have opened a pool");
            // Twice, in fact: `setUpBackend` closes on the way out and `cloneWith` closes the clone it is
            // discarding. Both reach the manager, and its own close is idempotent.
            Mockito.verify(construction.constructed().get(0), Mockito.atLeastOnce()).close();
        }
    }

    @Test
    @DisplayName("closing an adapter twice is safe")
    void closingAnAdapterTwiceIsSafe() throws Exception {
        // An adapter that really holds a manager: closing one that never had a pool would pass against a
        // close() that did nothing at all.
        // The failure path above closes in `setUpBackend` and then again in `cloneWith`, so the second close
        // has to be a no-op rather than an operation on a pool that is already down.
        final DatabaseAdapter adapter = adapterWithPool(DatabaseBackend.MYSQL);
        final DatabaseManager manager = managerOf(adapter);

        assertDoesNotThrow(adapter::close);
        assertDoesNotThrow(adapter::close);

        // The setUpBackend failure path and the cloneWith catch both close, so the adapter has to tolerate
        // being closed again by whoever owns it.
        Mockito.verify(manager, Mockito.atLeastOnce()).close();
    }
}
