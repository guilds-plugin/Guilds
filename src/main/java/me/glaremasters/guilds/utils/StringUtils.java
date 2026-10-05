package me.glaremasters.guilds.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.stream.Collectors;


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
     * Get the announcements for the plugin.
     *
     * @param plugin the plugin for which the announcements are to be fetched
     * @return the announcements for the plugin
     * @throws IOException if an I/O error occurs while fetching the announcements
     */
    public static String getAnnouncements(JavaPlugin plugin) throws IOException {
        final String ver = plugin.getDescription().getVersion();
        String announcement;
        final String reg = String.format("https://glaremasters.me/api/guilds/?id=%s", ver);
        final String prem = String.format("https://glaremasters.me/api/guilds/?id=%s&u=%s&d=%s", ver, PremiumFun.getUserID(), PremiumFun.getDownloadID());
        URL url = new URL(PremiumFun.isPremium() ? prem : reg);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", Constants.USER_AGENT);
        try (InputStream in = con.getInputStream()) {
            String result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            announcement = StringUtils.convert_html(result);
            con.disconnect();
        } catch (Exception ex) {
            LoggingUtils.warn("Could not fetch Guilds announcements.", ex);
            announcement = "Could not fetch announcements!";
        }
        return announcement;
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

    /**
     * Converts HTML colour codes into the section sign codes Minecraft expects.
     *
     * @param html the html string
     * @return a new converted string
     */
    public static String convert_html(String html) {
        return html.replace('&', '§');
    }

}
