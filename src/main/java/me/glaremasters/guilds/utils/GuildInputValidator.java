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

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Validates guild names and prefixes against their configured regular expressions.
 */
public final class GuildInputValidator {

    private static final char SECTION_SIGN = '\u00A7';
    private static final Pattern ALTERNATE_COLOR_CODE = Pattern.compile("(?i)&[0-9A-FK-ORX]");
    private static final Pattern SECTION_COLOR_CODE = Pattern.compile("(?i)" + SECTION_SIGN + "[0-9A-FK-ORX]");
    private static final Pattern ANY_COLOR_CODE = Pattern.compile("(?i)(?:&|" + SECTION_SIGN + ")[0-9A-FK-ORX]");

    private GuildInputValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validate a guild name using the configured name requirements.
     *
     * @param input the raw guild name
     * @param settingsManager the settings manager
     * @return true when the input satisfies the configured requirements
     */
    public static boolean isValidName(String input, SettingsManager settingsManager) {
        return matchesRequirements(
                input,
                settingsManager.getProperty(GuildSettings.NAME_REQUIREMENTS),
                settingsManager.getProperty(GuildSettings.INCLUDE_COLOR_CODES)
        );
    }

    /**
     * Validate a guild prefix using the configured prefix requirements.
     *
     * @param input the raw guild prefix
     * @param settingsManager the settings manager
     * @return true when the input satisfies the configured requirements
     */
    public static boolean isValidPrefix(String input, SettingsManager settingsManager) {
        return matchesRequirements(
                input,
                settingsManager.getProperty(GuildSettings.PREFIX_REQUIREMENTS),
                settingsManager.getProperty(GuildSettings.INCLUDE_COLOR_CODES)
        );
    }

    /**
     * Check whether a guild name is already used, ignoring color formatting and case.
     *
     * @param input the raw proposed guild name
     * @param guilds the existing guilds
     * @return true when an existing guild has the same visible name
     */
    public static boolean isNameTaken(String input, Collection<Guild> guilds) {
        final String normalizedInput = normalizeName(input);
        return guilds.stream()
                .map(Guild::getName)
                .map(GuildInputValidator::normalizeName)
                .anyMatch(normalizedInput::equalsIgnoreCase);
    }

    private static boolean matchesRequirements(String input, String regex, boolean includeColorCodes) {
        if (includeColorCodes) {
            return input.matches(regex);
        }

        final String visibleInput = ANY_COLOR_CODE.matcher(input).replaceAll("");
        if (!visibleInput.matches(regex)) {
            return false;
        }

        if (ALTERNATE_COLOR_CODE.matcher(input).find()
                && !regexAcceptsCharacter(visibleInput, regex, '&')) {
            return false;
        }

        return !SECTION_COLOR_CODE.matcher(input).find()
                || regexAcceptsCharacter(visibleInput, regex, SECTION_SIGN);
    }

    private static String normalizeName(String input) {
        return ACFBukkitUtil.removeColors(StringUtils.color(input));
    }

    private static boolean regexAcceptsCharacter(String validInput, String regex, char character) {
        if (validInput.isEmpty()) {
            return String.valueOf(character).matches(regex);
        }

        for (int i = 0; i < validInput.length(); i++) {
            final String candidate = validInput.substring(0, i) + character + validInput.substring(i + 1);
            if (candidate.matches(regex)) {
                return true;
            }
        }

        return false;
    }
}
