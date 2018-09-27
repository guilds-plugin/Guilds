package me.glaremasters.guilds.utils;

import org.bukkit.ChatColor;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class ConfigUtils {

    /**
     * Color a message
     * @param msg the message to color
     * @return a copy of the message with color
     */
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
