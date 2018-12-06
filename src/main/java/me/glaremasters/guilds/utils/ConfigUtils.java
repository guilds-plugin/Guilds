package me.glaremasters.guilds.utils;

import me.glaremasters.guilds.Guilds;
import org.bukkit.ChatColor;

import java.util.List;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class ConfigUtils {

    private static final Guilds guilds = Guilds.getGuilds();

    /**
     * Color a message
     * @param msg the message to color
     * @return a copy of the message with color
     */
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

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
     * Get a double from the config
     * @param path from config
     * @return double
     */
    public static Double getDouble(String path) {
        return guilds.getConfig().getDouble(path);
    }

    /**
     * Get a boolean from the config
     * @param path from config
     * @return true / false
     */
    public static boolean getBoolean(String path) {
        return guilds.getConfig().getBoolean(path);
    }

    /**
     * Get a stringlist from the config
     * @param path from config
     * @return stringlist
     */
    public static List<String> getStringList(String path) {
        return guilds.getConfig().getStringList(path);
    }

}
