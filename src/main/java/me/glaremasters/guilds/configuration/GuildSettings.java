package me.glaremasters.guilds.configuration;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class GuildSettings implements SettingsHolder {

    @Comment({"With the default RegEx currently set, the minimum length of the prefix is 1 and the maximum is 64.",
            "To change this, adjust the number and you can refer to the link below on how to modify RegEx.",
            "RegEx (https://en.wikipedia.org/wiki/Regular_expression) used to only allow certain characters (default only allows alphanumeric characters).",
            "To turn off the ability to use colors, remove the & from the RegEx."
    })
    public static final Property<String> NAME_REQUIREMENTS =
            newProperty("guild.requirements.name", "[a-zA-Z0-9&]{1,64}");

    @Comment("Similar to the name, just refer above.")
    public static final Property<String> PREFIX_REQUIREMENTS =
            newProperty("guild.requirements.prefix", "[a-zA-Z0-9&]{1,20}");

    @Comment("Do we want to enable the blacklist?")
    public static final Property<Boolean> BLACKLIST_TOGGLE =
            newProperty("guild.blacklist.enabled", true);

    @Comment("What words would you like to blacklist from being used?")
    public static final Property<List<String>> BLACKLIST_WORDS =
            newListProperty("guild.blacklist.words", "crap", "ass", "stupid");

    @Comment("This is the style used when a message sent in guild chat.")
    public static final Property<String> GUILD_CHAT_FORMAT =
            newProperty("guild.format.chat", "&7&l[Guild Chat]&r &b[{role}&b]&r &b {player}: {message}");

    @Comment("Similar to the one above, just for the admins spying.")
    public static final Property<String> SPY_CHAT_FORMAT =
            newProperty("guild.format.spy", "&7&l[Guild Spy]&r &b[{guild}&b]&r &b[{role}&b]&r &b {player}: {message}");

    @Comment("Do we want people in the same guild to be able to damage each other?")
    public static final Property<Boolean> GUILD_DAMAGE =
            newProperty("guild.damage.guild", false);

    @Comment("Do we want allies to be able to damage each other?")
    public static final Property<Boolean> ALLY_DAMAGE =
            newProperty("guild.damage.ally", false);



    private GuildSettings() {
    }
}
