package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class WarSettings implements SettingsHolder {

    @Comment({"How often (in minutes) can a guild be the defender in a war?",
    "This is to help prevent abuse from guilds fighting each other to farm rewards.",
    "This is defaulted to 1 day."})
    public static final Property<Integer> DEFEND_COOLDOWN =
            newProperty("war.defend-cooldown", 1440);

    @Comment("Would you like to block commands while a player is in the war?")
    public static final Property<Boolean> DISABLE_COMMANDS =
            newProperty("war.disable-commands", false);

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

    @Comment("Would you like to enable running commands before a challenge starts? (such as broadcasting)")
    public static final Property<Boolean> ENABLE_PRE_CHALLENGE_COMMANDS =
            newProperty("war.pre-challenge-commands.enabled", false);

    @Comment({"What commands would you like to run before a challenge starts?",
    "Supports the following placeholder:",
    "{challenger} - The name of the challenging Guild",
    "{defender} - The name of the defending Guild"})
    public static final Property<List<String>> PRE_CHALLENGE_COMMANDS =
            newListProperty("war.pre-challenge-commands.commands", "");

    @Comment("Would you like to enable running commands after a challenge ends? (such as broadcasting)")
    public static final Property<Boolean> ENABLE_POST_CHALLENGE_COMMANDS =
            newProperty("war.post-challenge-commands.enabled", false);

    @Comment({"What commands would you like to run after a challenge ends??",
            "Supports the following placeholder:",
            "{challenger} - The name of the challenging Guild",
            "{defender} - The name of the defending Guild",
            "{winner} - The winner of the challenge"})
    public static final Property<List<String>> POST_CHALLENGE_COMMANDS =
            newListProperty("war.post-challenge-commands.commands", "");

    @Comment("Would you like to give rewards to the winning guild?")
    public static final Property<Boolean> WAR_REWARDS_ENABLED =
            newProperty("war.rewards.enabled", false);

    @Comment({"What rewards (commands) would you like to run for the winning Guild?",
    "Current supports {player}."})
    public static final Property<List<String>> WAR_REWARDS =
            newListProperty("war.rewards.rewards", "");

}
