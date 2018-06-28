package me.glaremasters.guilds.utils;

import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class LoggerUtils {

    private static String prefix = "[Guilds]";

    /**
     * Log a message to the console with just some general information
     * @param msg the message you would like to send to the console
     * @param args this is just the args of the entire method
     */
    public static void info(String msg, String... args) {
        Bukkit.getLogger().log(Level.INFO, prefix + " " + String.format(msg, (Object) args));
    }

    /**
     * Log a message to the console with some information that may be an issue
     * @param msg the message you would like to send to the console
     * @param args this is just the args of the entire method
     */
    public static void warning(String msg, String... args) {
        Bukkit.getLogger().log(Level.WARNING, prefix + " " + String.format(msg, (Object) args));
    }

    /**
     * Log a message to the console with an error that will be an issue
     * @param msg the message you would like to send to the console
     * @param args this is just the args of the entire method
     */
    public static void severe(String msg, String... args) {
        Bukkit.getLogger().log(Level.SEVERE, prefix + " " + String.format(msg, (Object) args));
    }

}
