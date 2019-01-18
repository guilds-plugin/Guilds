package me.glaremasters.guilds.configuration;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class TablistSettings implements SettingsHolder {

    @Comment("Should the plugin use the built-in tablist?")
    public static final Property<Boolean> ENABLED =
            newProperty("tablist.enabled", false);

    @Comment("Would you like to display a user's display name instead of their MC username?")
    public static final Property<Boolean> DISPLAY_NAME =
            newProperty("tablist.display-name", false);

    @Comment({"Change how the Prefixes in the TabList show!",
            "Note: DO NOT REMOVE THE {guild}",
            "You can use {prefix} to show the Guild Prefix instead if you would like."
    })
    public static final Property<String> FORMAT =
            newProperty("tablist.format", "&7[&b{guild}&7]&r");

    private TablistSettings() {
    }
}
