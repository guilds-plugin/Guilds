package me.glaremasters.guilds.messages;

import co.aikar.commands.CommandIssuer;
import me.glaremasters.guilds.Guilds;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

import static me.glaremasters.guilds.utils.ConfigUtils.color;
import static me.glaremasters.guilds.utils.ConfigUtils.getPrefix;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 2:50 PM
 */
public enum Message {

    COMMAND_ERROR_ALREADY_IN_GUILD, COMMAND_ERROR_NO_GUILD, COMMAND_ERROR_ROLE_NO_PERMISSION,

    COMMAND_CREATE_GUILD_NAME_TAKEN, COMMAND_CREATE_SUCCESSFUL, COMMAND_CREATE_WARNING, COMMAND_CREATE_CANCELLED
    ;


    /**
     * Send a message to a player
     * @param issuer the player being sent the message
     * @param message the message being sent
     */
    public static void sendMessage(CommandIssuer issuer, Message message) {
        issuer.sendMessage(color(getPrefix() + Guilds.getGuilds().languageConfig.getString(getPath(message))));
    }

    public static void sendMessage(CommandSender sender, Message message) {
        sender.sendMessage(color(getPrefix() + Guilds.getGuilds().languageConfig.getString(getPath(message))));
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }
    /**
     * Get the path of a message
     * @param message the message you're getting the path of
     * @return the path translated to yaml format
     */
    private static String getPath(Message message) {
        StringBuilder pathBuilder = new StringBuilder();
        String[] parts = message.name().toLowerCase().split("_");

        pathBuilder.append(parts[0]).append(".").append(parts[1]).append(".")
                .append(String.join("-", Arrays.copyOfRange(parts, 2, parts.length)));

        return "messages." + pathBuilder.toString();
    }

    /**
     * Replace strings in a message
     * @param strings the strings being replaced
     * @return the replaced content for the strings
     */
    public String replace(String... strings) {
        if (strings.length % 2 != 0) {
            throw new IllegalArgumentException("Amount of keys and values do not match!");
        }

        String message = Guilds.getGuilds().languageConfig.getString(getPath(this));

        if (message == null) {
            return null;
        }

        for (int i = 0; i < strings.length / 2; i++) {
            message = message.replace(strings[i * 2], strings[(i * 2) + 1]);
        }

        return message;
    }
}
