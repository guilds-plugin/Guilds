package me.glaremasters.guilds.utils;

import org.bukkit.ChatColor;

import java.util.Random;


/**
 * A collection of string utility methods.
 */
public final class StringUtils {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Translates alternate color codes in a string.
     *
     * @param input the string to be translated
     * @return the translated string
     */
    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Generates a random string with the specified length.
     *
     * @param length the length of the generated string
     * @return the generated string
     */
    public static String generateString(int length) {
        final Random random = new Random();
        final StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

}
