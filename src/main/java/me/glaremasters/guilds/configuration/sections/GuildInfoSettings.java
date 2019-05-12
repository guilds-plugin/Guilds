package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:20 PM
 */
public class GuildInfoSettings implements SettingsHolder {

    private static final String INFO_PATH = "guis.guild-info.";

    @Comment("What would you like the name of the GUI to be?")
    public static final Property<String> GUI_NAME =
            newProperty(INFO_PATH + "name", "Guild Info");




    private GuildInfoSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Welcome to the Guild Info GUI section of the config.",
                "Here you can modify the configuration of what the Guild Info GUI looks like.",
                "This can be used by any member of a Guild and shows key information of the Guild.",
                "You can see things like the members, the balance, tier, etc."
        };
        conf.setComment("guis.guild-info", pluginHeader);
    }
}
