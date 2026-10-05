package me.glaremasters.guilds.utils;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKeyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for message related operations.
 */
public class MessageUtils {

    /**
     * Returns a translated string for the given message key using the given command issuer.
     *
     * @param issuer the issuer that requested the message
     * @param key    the key of the message to be translated
     * @return the translated string
     */
    public static String asString(@NotNull final CommandIssuer issuer, @NotNull final MessageKeyProvider key) {
        return issuer.getManager().getLocales().getMessage(issuer, key);
    }

    /**
     * Returns a translated string for the given message key using the given player and plugin manager.
     *
     * @param player  the player for whom the message is translated
     * @param manager the manager of the plugin
     * @param key     the key of the message to be translated
     * @return the translated string
     */
    public static String asString(@NotNull final Player player, @NotNull final PaperCommandManager manager, @NotNull final MessageKeyProvider key) {
        return manager.getLocales().getMessage(manager.getCommandIssuer(player), key);
    }

    /**
     * Returns a translated string for the given message key using the given plugin manager.
     *
     * @param manager the manager of the plugin
     * @param key     the key of the message to be translated
     * @return the translated string
     */
    public static String asString(@NotNull final PaperCommandManager manager, @NotNull final MessageKeyProvider key) {
        return manager.getLocales().getMessage(manager.getCommandIssuer(Bukkit.getConsoleSender()), key);
    }
}
