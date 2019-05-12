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
public class GuildInfoSettings implements SettingsHolder {

    private static final String INFO_PATH = "guis.guild-info.";

    @Comment({"What would you like the name of the GUI to be?",
    "Currently supports {name} for the name of the guild and {prefix} for the prefix of the guild"})
    public static final Property<String> GUI_NAME =
            newProperty(INFO_PATH + "name", "{name}");

    @Comment("What material do you want the tier button to be?")
    public static final Property<String> TIER_MATERIAL =
            newProperty(INFO_PATH + "tier-material", "DIAMOND");

    @Comment("What do you want the name of the tier button to be?")
    public static final Property<String> TIER_NAME =
            newProperty(INFO_PATH + "tier-name", "&fGuild Tier");

    @Comment("What do you want the lore of the tier button to be?")
    public static final Property<List<String>> TIER_LORE =
            newListProperty(INFO_PATH + "tier-lore", "&f• Level: {tier}");

    @Comment("What material do you want the bank button to be?")
    public static final Property<String> BANK_MATERIAL =
            newProperty(INFO_PATH + "bank-material", "GOLD_INGOT");

    @Comment("What do you want the name of the bank button to be?")
    public static final Property<String> BANK_NAME =
            newProperty(INFO_PATH + "bank-name", "&fGuild Bank");

    @Comment("What do you want the lore of the bank button to be?")
    public static final Property<List<String >> BANK_LORE =
            newListProperty(INFO_PATH + "bank-lore", "&f• Balance: {current} / {max}");

    @Comment("What material do you want the members button to be?")
    public static final Property<String> MEMBERS_MATERIAL =
            newProperty(INFO_PATH + "members-material", "IRON_HELMET");

    @Comment("What do you want the name of the members button to be?")
    public static final Property<String> MEMBERS_NAME =
            newProperty(INFO_PATH + "members-name", "&fGuild Members");

    @Comment("What do you want the lore of the members button to be?")
    public static final Property<List<String >> MEMBERS_LORE =
            newListProperty(INFO_PATH + "members-lore", "&f• Members: {current} / {max}", "&f• Online: {online} / {total}", "", "&fClick to view members!");

    @Comment("What material do you want the status button to be?")
    public static final Property<String> STATUS_MATERIAL =
            newProperty(INFO_PATH + "status-material", "LEVER");

    @Comment("What do you want the name of the status button to be?")
    public static final Property<String> STATUS_NAME =
            newProperty(INFO_PATH + "status-name", "&fGuild Status");

    @Comment("What do you want the lore of the status button to be?")
    public static final Property<List<String >> STATUS_LORE =
            newListProperty(INFO_PATH + "status-lore", "&f• Status: {status}");

    @Comment("What material do you want the home button to be?")
    public static final Property<String> HOME_MATERIAL =
            newProperty(INFO_PATH + "home-material", "BED");

    @Comment("What do you want the name of the home button to be?")
    public static final Property<String> HOME_NAME =
            newProperty(INFO_PATH + "home-name", "&fGuild Home");

    @Comment("What do you want the lore of the home button to be?")
    public static final Property<List<String >> HOME_LORE =
            newListProperty(INFO_PATH + "home-lore", "&f• Home: {coords}");




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
