package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
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
        return isNameTaken(input, guilds, null);
    }

    /**
     * Check whether a guild name is already used by a guild other than the one being renamed.
     *
     * <p>A rename has to compare against every guild <em>except</em> its own, otherwise a guild can
     * never keep or return to a name it already has.
     *
     * @param input the raw proposed guild name
     * @param guilds the existing guilds
     * @param ignoredGuildId the guild to leave out of the comparison, or null to compare against all
     * @return true when a different guild already has the same visible name
     */
    public static boolean isNameTaken(String input, Collection<Guild> guilds, @Nullable UUID ignoredGuildId) {
        final String normalizedInput = normalizeName(input);
        return guilds.stream()
                .filter(guild -> ignoredGuildId == null || !ignoredGuildId.equals(guild.getId()))
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
