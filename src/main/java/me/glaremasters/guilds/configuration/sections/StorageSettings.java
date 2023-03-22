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
package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class StorageSettings implements SettingsHolder {

    @Comment("What storage method should be used? (MySQL, MariaDB, JSON, SQLite)")
    public static final Property<String> STORAGE_TYPE =
            newProperty("storage.storage-type", "json");

    @Comment({"How often (in minutes) do you want all Guild Data to save?"})
    public static final Property<Integer> SAVE_INTERVAL =
            newProperty("storage.save-interval", 1);

    @Comment("Define the address for the database. (Doesn't apply to SQLite)")
    public static final Property<String> SQL_HOST =
            newProperty("storage.sql.host", "localhost");

    @Comment("Define the port for the database. (Doesn't apply to SQLite)")
    public static final Property<String> SQL_PORT =
            newProperty("storage.sql.port", "3306");

    @Comment({"The name of the database to store data in.", "This must be already created! (Doesn't apply to SQLite)"})
    public static final Property<String> SQL_DATABASE =
            newProperty("storage.sql.database", "guilds");

    @Comment("The prefix for all Guilds tables.")
    public static final Property<String> SQL_TABLE_PREFIX =
            newProperty("storage.sql.table-prefix", "guilds_");

    @Comment("Define the credentials for the database. (Doesn't apply to SQLite)")
    public static final Property<String> SQL_USERNAME =
            newProperty("storage.sql.username", "root");
    public static final Property<String> SQL_PASSWORD =
            newProperty("storage.sql.password", "");

    public static final Property<Boolean> UTF8 =
            newProperty("storage.sql.utf8", true);

    @Comment("Sets whether or not to use SSL for the remote SQL database connection (Doesn't apply to SQLite)")
    public static final Property<Boolean> SQL_ENABLE_SSL =
            newProperty("storage.sql.enable-ssl", false);

    @Comment({"Sets the maximum size of the SQL connection pool.", "This value will determine the maximum number of connections maintained. (Doesn't apply to SQLite)"})
    public static final Property<Integer> SQL_POOL_SIZE =
            newProperty("storage.sql.pool.maximum-pool-size", 10);

    @Comment({"Sets the minimum number of idle connections that the pool will maintain.", "For maximum performance keep this value the same as 'maximum-pool-size' (Doesn't apply to SQLite)"})
    public static final Property<Integer> SQL_POOL_IDLE =
            newProperty("storage.sql.pool.minimum-idle", 10);

    @Comment({"Sets the maximum lifetime of a connection in the pool in milliseconds. (Doesn't apply to SQLite)"})
    public static final Property<Integer> SQL_POOL_LIFETIME =
            newProperty("storage.sql.pool.maximum-lifetime", 1800000);

    @Comment("Sets the maximum number of milliseconds for a connection in the pool before timing out. (Doesn't apply to SQLite)")
    public static final Property<Integer> SQL_POOL_TIMEOUT =
            newProperty("storage.sql.pool.connection-timeout", 5000);

    private StorageSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] poolHeader = {
                "These settings change the SQL connection pool.",
                "The default settings are optimized for the majority of users.",
                "Do NOT change these settings unless you know what you are doing!",
                "For those looking to migrate data:",
                "IF YOU ARE MIGRATING FROM JSON OR SQLITE TO MYSQL THE SETTINGS MUST BE CONFIGURED EVEN IF THE DATA TYPE IS SET TO JSON OR MYSQL.",
                "When migrating to MySQL, it will attempt to use the MySQL backend (grab the login information here) in order to connect to the database.",
                "We are not responsible for data loss if you are too lazy to read the warnings.",
                "ALWAYS REMEMBER TO BACKUP DATA BEFORE MIGRATING.",
                "Lastly, remember to change your storage-type to the type you're migrating to before you reboot."
        };
        conf.setComment("storage.sql.pool", poolHeader);
    }

}