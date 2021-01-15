package me.glaremasters.guilds.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

/**
 * Created by Glare
 * Date: 1/15/2021
 * Time: 1:26 PM
 */
public class WarUtils {

    /**
     * Special method designed to notify a player based on the specific way set in the config
     *
     * @param type     the type of way to notify the player
     * @param message  the message to send the player
     * @param audience the player to send the message to
     */
    public static void notify(final String type, final String message, final Audience audience) {
        switch (type.toLowerCase()) {
            case "title":
                audience.showTitle(Title.title(Component.text(message), Component.empty()));
                break;
            case "subtitle":
                audience.showTitle(Title.title(Component.empty(), Component.text(message)));
                break;
            case "none":
                break;
            default:
            case "actionbar":
                audience.sendActionBar(Component.text(message));
                break;
        }
    }
}
