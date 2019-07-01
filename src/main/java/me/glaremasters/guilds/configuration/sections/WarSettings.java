package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class WarSettings implements SettingsHolder {

    @Comment("How long does a defending guild have to accept a war challenge? (In seconds)")
    public static final Property<Integer> ACCEPT_TIME =
            newProperty("war.accept-time", 120);

}
