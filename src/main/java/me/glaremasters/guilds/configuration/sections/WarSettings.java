package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
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

    @Comment("How long should we wait to teleport the players and start the war after everyone joined?")
    public static final Property<Integer> READY_TIME =
            newProperty("war.ready-time", 60);

    @Comment("Would you like to give rewards to the winning guild?")
    public static final Property<Boolean> WAR_REWARDS_ENABLED =
            newProperty("war.rewards.enabled", false);

    @Comment({"What rewards (commands) would you like to run for the winning Guild?",
    "Current supports {player}."})
    public static final Property<List<String>> WAR_REWARDS =
            newListProperty("war.rewards.rewards", "");

}
