package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class ExperimentalSettings implements SettingsHolder {

    @Comment({"Enable this to use member skulls instead of player heads for the guild member list.", "This is experimental because large amounts of members may cause lag."})
    public static final Property<Boolean> MEMBER_HEAD_SKILLS =
            newProperty("experimental.member-skulls", false);

    private ExperimentalSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] experimentalHeader = {
                "The following are experimental features that are not fully tested. Use at your own risk. Feedback is appreciated."
        };
        conf.setComment("experimental", experimentalHeader);
    }
}
