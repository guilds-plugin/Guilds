package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 3/22/2019
 * Time: 12:49 AM
 */
public class CodeSettings  implements SettingsHolder {

    @Comment("How long do you want the default length of guild codes to be?")
    public static final Property<Integer> CODE_LENGTH =
            newProperty("codes.length", 7);

    @Comment("Do you want inactive codes (no uses left) to display on the /guild code list?")
    public static final Property<Boolean> LIST_INACTIVE_CODES =
            newProperty("codes.list-inactive-codes", true);

    @Comment("What is the max amount of active codes you would like to allow per guild?")
    public static final Property<Integer> ACTIVE_CODE_AMOUNT =
            newProperty("codes.amount", 10);

    private CodeSettings () {

    }

}
