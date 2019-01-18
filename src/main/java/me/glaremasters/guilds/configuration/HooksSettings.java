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
public class HooksSettings implements SettingsHolder {

    @Comment("Do we want to hook into Essentials-Chat format to handle guild placeholders?")
    public static final Property<Boolean> ESSENTIALS =
            newProperty("hooks.essentials-chat", false);

    @Comment("Do we want to hook into WorldGuard to allow claiming land?")
    public static final Property<Boolean> WORLDGUARD =
            newProperty("hooks.worldguard-claims", false);

    private HooksSettings() {
    }
}

