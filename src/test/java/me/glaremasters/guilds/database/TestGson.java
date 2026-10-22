package me.glaremasters.guilds.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;

import java.lang.reflect.Field;

/**
 * Puts a Gson where the JSON providers expect to find one.
 *
 * <p>The providers read their Gson from {@link Guilds#getGson()}, which is a static field filled in
 * during plugin startup. A unit test has no plugin, so the field is set directly. The Gson built
 * here is the same one the plugin builds, so the files the tests write are the files the plugin
 * would write.
 */
public final class TestGson {

    private TestGson() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Installs the plugin's Gson into the plugin's static field.
     *
     * @throws ReflectiveOperationException if the field cannot be reached
     */
    public static void install() throws ReflectiveOperationException {
        final Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        field.set(null, new GsonBuilder().setPrettyPrinting().create());
    }

    /**
     * @return a Gson identical to the one the plugin uses
     */
    public static Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }
}
