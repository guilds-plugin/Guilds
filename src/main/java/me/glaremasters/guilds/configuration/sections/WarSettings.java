/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    @Comment("Would you like to have the players keep their inventory when they die in the arena?")
    public static final Property<Boolean> KEEP_INVENTORY =
            newProperty("war.keep-inventory", true);

    @Comment("Would you like to have the players keep their experience when they die in the arena?")
    public static final Property<Boolean> KEEP_EXP =
            newProperty("war.keep-exp", true);

    @Comment({"Would you like to clear the drops when a player dies in the arena?",
            "This is typically paired with keeping inventory true so that you can prevent duplication."})
    public static final Property<Boolean> CLEAR_DROPS =
            newProperty("war.clear-drops", true);

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

    @Comment({"How would you like to notify the player about joining the war and the countdowns?", "Options: [actionbar, title, subtitle, none]"})
    public static final Property<String> NOTIFY_TYPE = newProperty("war.notify-type", "actionbar");

    @Comment("Would you like to enable running commands after a challenge ends? (such as broadcasting)")
    public static final Property<Boolean> ENABLE_POST_CHALLENGE_COMMANDS =
            newProperty("war.post-challenge-commands.enabled", false);

    @Comment({"What commands would you like to run after a challenge ends??",
            "Supports the following placeholder:",
            "{challenger} - The name of the challenging Guild",
            "{defender} - The name of the defending Guild",
            "{winner} - The winner of the challenge",
            "{loser} - The loser of the challenge"})
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
