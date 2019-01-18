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
public class CostSettings implements SettingsHolder {

    @Comment("How much should it cost to create a guild?")
    public static final Property<Double> CREATION =
            newProperty("cost.creation", 0.0);

    @Comment("How much should it cost to set the cost of the guild home?")
    public static final Property<Double> SETHOME =
            newProperty("cost.sethome", 0.0);



    private CostSettings() {
    }
}