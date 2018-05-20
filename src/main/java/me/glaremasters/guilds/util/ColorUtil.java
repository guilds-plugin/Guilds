package me.glaremasters.guilds.util;

import org.bukkit.ChatColor;

/**
 * Created by GlareMasters on 2/24/2018.
 */
public class ColorUtil {

    /**
     * Transform a regular string and support color
     * @param string message to translate
     * @return colorfied string
     */
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
