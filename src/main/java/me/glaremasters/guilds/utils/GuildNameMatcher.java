package me.glaremasters.guilds.utils;

import co.aikar.commands.ACFBukkitUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Decides when two guild names name the same guild.
 *
 * <p>One rule, used everywhere a guild is named: reduce the name to the text a player sees, then
 * compare it without regard to case. The visible text is what is left after the alternate colour
 * codes are translated and every colour code is removed, so {@code "&cRaiders"},
 * {@code "§cRaiders"} and {@code "Raiders"} are one name.
 *
 * <p>Duplicate-name detection and guild lookup have to use the same rule. When they did not, a
 * server refused to create a name it also refused to resolve: {@code /guild create} compares names
 * with the colour codes stripped and the case ignored, while the lookup compared the stripped name
 * with the typed name exactly. A guild called "Raiders" could not be found as "raiders" and could
 * not be found under its own colour codes either.
 */
public final class GuildNameMatcher {

    private GuildNameMatcher() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Reduces a guild name to the text a player sees.
     *
     * <p>The alternate colour codes are translated first, so a name written with {@code &} codes
     * and a name written with the section sign codes reduce to the same text. The result keeps the
     * case of the input; comparing two reduced names is a separate decision.
     *
     * @param name the name as it was typed or as it is stored
     * @return the visible text of the name
     */
    public static String normalize(@NotNull final String name) {
        return ACFBukkitUtil.removeColors(StringUtils.color(name));
    }

    /**
     * Whether two names name the same guild.
     *
     * @param first the first name
     * @param second the second name
     * @return true when both names reduce to the same visible text, ignoring case
     */
    public static boolean isSameName(@NotNull final String first, @NotNull final String second) {
        return normalize(first).equalsIgnoreCase(normalize(second));
    }
}
