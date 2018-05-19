package me.glaremasters.guilds.util;

import static me.glaremasters.guilds.util.ColorUtil.color;
import me.glaremasters.guilds.Guilds;

/**
 * Created by GlareMasters on 5/18/2018.
 */
public final class ConfigUtil {

    private static final Guilds guilds = Guilds.getInstance();

    /**
     * Get a string from the config
     * @param path from config
     * @return string
     */
    public static String getString(String path) {
        return color(guilds.getConfig().getString((path)));
    }

    /**
     * Get an int from the config
     * @param path from config
     * @return int
     */
    public static int getInt(String path) {
        return guilds.getConfig().getInt(path);
    }

    /**
     * Get a boolean from the config
     * @param path from config
     * @return true / false
     */
    public static boolean getBoolean(String path) {
        return guilds.getConfig().getBoolean(path);
    }

}
