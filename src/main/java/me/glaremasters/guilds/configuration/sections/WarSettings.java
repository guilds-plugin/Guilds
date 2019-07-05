package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class WarSettings implements SettingsHolder {

    @Comment("How long does a defending guild have to accept a war challenge? (In seconds)")
    public static final Property<Integer> ACCEPT_TIME =
            newProperty("war.accept-time", 120);

    @Comment("What is the min number of players needed on each side for a war to start?")
    public static final Property<Integer> MIN_PLAYERS =
            newProperty("war.min-players", 3);

    @Comment("What is the max number of players allowed on each side for a war?")
    public static final Property<Integer> MAX_PLAYERS =
            newProperty("war.max-players", 8);

    @Comment("How long do players of both sides have to join the war? (In seconds)")
    public static final Property<Integer> JOIN_TIME =
            newProperty("war.join-time", 60);

}
