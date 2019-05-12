package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:20 PM
 */
public class GuildInfoMemberSettings implements SettingsHolder {

    private static final String INFO_PATH = "guis.guild-info-members.";

    @Comment("What would you like the name of the GUI to be?")
    public static final Property<String> GUI_NAME =
            newProperty(INFO_PATH + "name", "Members of {name}");

    @Comment("What material do you want to use to represent members?")
    public static final Property<String> MEMBERS_MATERIAL =
            newProperty(INFO_PATH + "item.material", "EMPTY_MAP");

    @Comment("What do you want the name of the item to be?")
    public static final Property<String> MEMBERS_NAME =
            newProperty(INFO_PATH + "item.name", " ");

    @Comment("What do you want the lore of the item to be?")
    public static final Property<List<String>> MEMBERS_LORE =
            newListProperty(INFO_PATH + "item.lore", "&f• Name: {name}", "&f• Role: {role}", "&f• Status: {status}", "");

    @Comment("What do you want to be what shows when a member is online?")
    public static final Property<String> MEMBERS_ONLINE =
            newProperty(INFO_PATH + "item.online", "&fONLINE");

    @Comment("What do you want to be what shows when a member is offline?")
    public static final Property<String> MEMBERS_OFFLINE =
            newProperty(INFO_PATH + "item.offline", "&fOFFLINE");




    private GuildInfoMemberSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "This part of the config controls what the members gui looks like.",
                "You can get to this in game by clicking on the members icon via the guild info gui."
        };
        conf.setComment("guis.guild-info-members", pluginHeader);
    }
}
