package me.glaremasters.guilds.configuration;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/18/2019
 * Time: 2:26 PM
 */
public class ClaimSettings implements SettingsHolder {

    @Comment({"This is the number of blocks around the player it will try to create the region.",
            "Keep in mind this is the RADIUS, it will go this many blocks in both directions.",
            "For example, if you take the default 15, it'll do 30 total as it will go 15 blocks in both directions.",
            "This is a CUBOID region, not SPHERE."
    })
    public static final Property<Integer> RADIUS =
            newProperty("claims.radius", 15);

    @Comment({"Customize the entrance and exit message of joining claims.",
            "Supports {prefix} for guild prefix and {guild} for guild name.",
            "Also supports color codes!"
    })
    public static final Property<String> ENTER_MESSAGE =
            newProperty("claims.enter-message", "&aNow entering &d{guild}'s &aclaim!");

    public static final Property<String> EXIT_MESSAGE =
            newProperty("claims.exit-message", "&aNow leaving &d{guild}'s &aclaim!");

    @Comment("Would you like to disable guild claiming in specific worlds?")
    public static final Property<List<String>> DISABLED_WORLDS =
            newListProperty("claims.disabled-worlds", "");

    private ClaimSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "This section of the config will allow you to handle guild land claiming.",
                "Remember that the enable / disable for this is the WorldGuard Hook at the TOP of the config.",
                "There are multiple options when it comes to guild claims. For the time being, all guilds will only get one claim."
        };
        conf.setComment("claims", pluginHeader);
    }
}
