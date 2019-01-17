package me.glaremasters.guilds.configuration;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 12:51 PM
 */
public final class PluginSettings implements SettingsHolder {

    @Comment({"This is used for the Guild's Announcement System, which allow me (The Author) to communicate to you guys without updating.",
            "The way this works is very simple. If you have \"console\" set to \"true\", you will see the announcement when the server starts.",
            "If you have \"in-game\" set to \"true\", your OPed players will see it the first time they login to the server."
    })
    public static final Property<Boolean> ANNOUNCEMENTS_CONSOLE =
            newProperty("settings.announcements.console", true);

    public static final Property<Boolean> ANNOUNCEMENTS_IN_GAME =
            newProperty("settings.announcements.in-game", true);

    @Comment({"Choosing your language for the plugin couldn't be easier! The default language is english.",
            "If you speak another language but don't see it here, feel free to submit it via one of the links above to have it added to the plugin.",
            "If you try and use a different language than any in the list above, the plugin will not function in a normal manner.",
            "As you can see this is currently en-US, and there is a en-US.yml file in the language folder.",
            "If I wanted to switch to french, I would use fr-FR as the language instead."
    })
    public static final Property<String> MESSAGES_LANGUAGE =
            newProperty("settings.messagesLanguage", "en-US");

    private PluginSettings() {
    }
}
