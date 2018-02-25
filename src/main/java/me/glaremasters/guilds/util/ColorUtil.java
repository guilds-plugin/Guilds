package me.glaremasters.guilds.util;

import org.bukkit.ChatColor;

/**
 * Created by GlareMasters on 2/24/2018.
 */
public class ColorUtil {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
