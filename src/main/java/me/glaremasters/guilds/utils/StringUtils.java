/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
            ex.printStackTrace();
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
     * Converts HTML
     *
     * @param html the html string
     * @return a new converted string
     * Side note: No clue who made this
     * This might be prone for removal or be changed to a library / different function.
     */
    public static String convert_html(String html) {


        html = html.replaceAll("&", "§");

        /*
         * In contrast to fixing Java's broken regex charclasses,
         * this one need be no bigger, as unescaping shrinks the string
         * here, where in the other one, it grows it.
         */

        StringBuffer newstr = new StringBuffer(html.length());

        boolean saw_backslash = false;

        for (int i = 0; i < html.length(); i++) {
            int cp = html.codePointAt(i);
            if (html.codePointAt(i) > Character.MAX_VALUE) {
                i++; /****WE HATES UTF-16! WE HATES IT FOREVERSES!!!****/
            }

            if (!saw_backslash) {
                if (cp == '\\') {
                    saw_backslash = true;
                } else {
                    newstr.append(Character.toChars(cp));
                }
                continue; /* switch */
            }

            if (cp == '\\') {
                saw_backslash = false;
                newstr.append('\\');
                newstr.append('\\');
                continue; /* switch */
            }

            switch (cp) {

                case 'r':
                    newstr.append('\r');
                    break; /* switch */

                case 'n':
                    newstr.append('\n');
                    break; /* switch */

                case 'f':
                    newstr.append('\f');
                    break; /* switch */

                /* PASS a \b THROUGH!! */
                case 'b':
                    newstr.append("\\b");
                    break; /* switch */

                case 't':
                    newstr.append('\t');
                    break; /* switch */

                case 'a':
                    newstr.append('\007');
                    break; /* switch */

                case 'e':
                    newstr.append('\033');
                    break; /* switch */

                /*
                 * A "control" character is what you get when you xor its
                 * codepoint with '@'==64.  This only makes sense for ASCII,
                 * and may not yield a "control" character after all.
                 *
                 * Strange but true: "\c{" is ";", "\c}" is "=", etc.
                 */
                case 'c': {
                    if (++i == html.length()) {
                        return "trailing \\c";
                    }
                    cp = html.codePointAt(i);
                    /*
                     * don't need to grok surrogates, as next line blows them up
                     */
                    if (cp > 0x7f) {
                        return "expected ASCII after \\c";
                    }
                    newstr.append(Character.toChars(cp ^ 64));
                    break; /* switch */
                }

                case '8':
                case '9':
                    return "illegal octal digit";
                /* NOTREACHED */

                /*
                 * may be 0 to 2 octal digits following this one
                 * so back up one for fallthrough to next case;
                 * unread this digit and fall through to next case.
                 */
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    --i;
                    /* FALLTHROUGH */

                    /*
                     * Can have 0, 1, or 2 octal digits following a 0
                     * this permits larger values than octal 377, up to
                     * octal 777.
                     */
                case '0': {
                    if (i + 1 == html.length()) {
                        /* found \0 at end of string */
                        newstr.append(Character.toChars(0));
                        break; /* switch */
                    }
                    i++;
                    int digits = 0;
                    int j;
                    for (j = 0; j <= 2; j++) {
                        if (i + j == html.length()) {
                            break; /* for */
                        }
                        /* safe because will unread surrogate */
                        int ch = html.charAt(i + j);
                        if (ch < '0' || ch > '7') {
                            break; /* for */
                        }
                        digits++;
                    }
                    if (digits == 0) {
                        --i;
                        newstr.append('\0');
                        break; /* switch */
                    }
                    int value;
                    try {
                        value = Integer.parseInt(
                                html.substring(i, i + digits), 8);
                    } catch (NumberFormatException nfe) {
                        return "invalid octal value for \\0 escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += digits - 1;
                    break; /* switch */
                } /* end case '0' */

                case 'x': {
                    if (i + 2 > html.length()) {
                        return "string too short for \\x escape";
                    }
                    i++;
                    boolean saw_brace = false;
                    if (html.charAt(i) == '{') {
                        /* ^^^^^^ ok to ignore surrogates here */
                        i++;
                        saw_brace = true;
                    }
                    int j;
                    for (j = 0; j < 8; j++) {

                        if (!saw_brace && j == 2) {
                            break;  /* for */
                        }

                        /*
                         * ASCII test also catches surrogates
                         */
                        int ch = html.charAt(i + j);
                        if (ch > 127) {
                            return "illegal non-ASCII hex digit in \\x escape";
                        }

                        if (saw_brace && ch == '}') {
                            break; /* for */
                        }

                        if (!((ch >= '0' && ch <= '9')
                                ||
                                (ch >= 'a' && ch <= 'f')
                                ||
                                (ch >= 'A' && ch <= 'F')
                        )
                        ) {
                            return (String.format(
                                    "illegal hex digit #%d '%c' in \\x", ch, ch));
                        }

                    }
                    if (j == 0) {
                        return "empty braces in \\x{} escape";
                    }
                    int value;
                    try {
                        value = Integer.parseInt(html.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\x escape";
                    }
                    newstr.append(Character.toChars(value));
                    if (saw_brace) {
                        j++;
                    }
                    i += j - 1;
                    break; /* switch */
                }

                case 'u': {
                    if (i + 4 > html.length()) {
                        return "string too short for \\u escape";
                    }
                    i++;
                    int j;
                    for (j = 0; j < 4; j++) {
                        /* this also handles the surrogate issue */
                        if (html.charAt(i + j) > 127) {
                            return "illegal non-ASCII hex digit in \\u escape";
                        }
                    }
                    int value;
                    try {
                        value = Integer.parseInt(html.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\u escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                case 'U': {
                    if (i + 8 > html.length()) {
                        return "string too short for \\U escape";
                    }
                    i++;
                    int j;
                    for (j = 0; j < 8; j++) {
                        /* this also handles the surrogate issue */
                        if (html.charAt(i + j) > 127) {
                            return "illegal non-ASCII hex digit in \\U escape";
                        }
                    }
                    int value;
                    try {
                        value = Integer.parseInt(html.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        return "invalid hex value for \\U escape";
                    }
                    newstr.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                default:
                    newstr.append('\\');
                    newstr.append(Character.toChars(cp));
                    /*
                     * say(String.format(
                     *       "DEFAULT unrecognized escape %c passed through",
                     *       cp));
                     */
                    break; /* switch */

            }
            saw_backslash = false;
        }

        /* weird to leave one at the end */
        if (saw_backslash) {
            newstr.append('\\');
        }

        return newstr.toString();
    }

}
