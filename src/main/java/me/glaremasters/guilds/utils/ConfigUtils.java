package me.glaremasters.guilds.utils;

import me.glaremasters.guilds.Guilds;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

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
     * Easily get a string from the config
     * @param path the path in the config to grab the string from
     * @return the value of the string in the config
     */
    public static String getString(String path) {
        return color(guilds.getConfig().getString(path));
    }

    /**
     * Easily get an int from the config
     * @param path the path in the config to grab the int from
     * @return the value of the int in the config
     */
    public static int getInt(String path) {
        return guilds.getConfig().getInt(path);
    }

    /**
     * Easily get a double from the config
     * @param path the path in the config to grab the double from
     * @return the value of the double in the config
     */
    public static Double getDouble(String path) {
        return guilds.getConfig().getDouble(path);
    }

    /**
     * Easily get a boolean from the config
     * @param path the path in the config to grab the boolean from
     * @return the value of the boolean in the config
     */
    public static boolean getBoolean(String path) {
        return guilds.getConfig().getBoolean(path);
    }

    /**
     * Easily get a string list from the config
     * @param path the path in the config to grab the string list from
     * @return the value of the string list in the config
     */
    public static List<String> getStringList(String path) {
        return guilds.getConfig().getStringList(path);
    }

    /**
     * Easily get a configuration section from the config
     * @param path the path in the config to grab the configuration section from
     * @return the value of the configuration section in the config
     */
    public static ConfigurationSection getSection(String path) {
        return guilds.getConfig().getConfigurationSection(path);
    }

    /**
     * Get the prefix of the plugin
     * @return prefix of plugin
     */
    public static String getPrefix() { return color(guilds.getConfig().getString("plugin-prefix"));
    }







}
