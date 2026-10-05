package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class CooldownSettings implements SettingsHolder {

    @Comment("How often (in seconds) can a player set their guild home?")
    public static final Property<Integer> SETHOME =
            newProperty("timers.cooldowns.sethome", 60);

    @Comment("How often (in seconds) can a player go to their guild home?")
    public static final Property<Integer> HOME =
            newProperty("timers.cooldowns.home", 60);

    @Comment("How often (in seconds) can a player request to join a guild?")
    public static final Property<Integer> REQUEST =
            newProperty("timers.cooldowns.request", 60);

    @Comment("How long should a user have to wait before joining a new guild after leaving one?")
    public static final Property<Integer> JOIN =
            newProperty("timers.cooldowns.join", 120);

    @Comment("Do you want to enable making players stand still before teleporting?")
    public static final Property<Boolean> WU_HOME_ENABLED =
            newProperty("timers.warmups.home.enabled", false);

    @Comment("How long should a user have to stand still before teleporting?")
    public static final Property<Integer> WU_HOME =
            newProperty("timers.warmups.home.time", 3);

    private CooldownSettings() {
    }
}
