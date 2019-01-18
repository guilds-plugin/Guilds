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
public class TimerSettings implements SettingsHolder {

    @Comment("How often (in seconds) can a player set their guild home?")
    public static final Property<Integer> SETHOME =
            newProperty("timers.cooldowns.sethome", 60);

    @Comment("How often (in seconds) can a player go to their guild home?")
    public static final Property<Integer> HOME =
            newProperty("timers.cooldowns.home", 60);

    @Comment("How long should a user have to stand still before teleporting?")
    public static final Property<Integer> WU_HOME =
            newProperty("timers.warpups.home", 3);

    private TimerSettings() {
    }
}
