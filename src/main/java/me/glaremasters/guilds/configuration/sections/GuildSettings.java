/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
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
            "To turn off the ability to use colors, remove the & from the RegEx.",
            "Trying to use symbols such as Chinese ones? Try this Regex: [\\u4E00-\\u9FA5_a-zA-Z0-9&_\\一-龥]{1,6}"
    })
    public static final Property<String> NAME_REQUIREMENTS =
            newProperty("guild.requirements.name", "[a-zA-Z0-9&]{1,64}");

    @Comment("Similar to the name, just refer above.")
    public static final Property<String> PREFIX_REQUIREMENTS =
            newProperty("guild.requirements.prefix", "[a-zA-Z0-9&]{1,20}");

    @Comment("Would you like to include color codes signs (&b &l, etc) in the length check?")
    public static final Property<Boolean> INCLUDE_COLOR_CODES =
            newProperty("guild.requirements.include-color-codes", true);

    @Comment("Would you like to allow players to make a guild without a prefix?")
    public static final Property<Boolean> DISABLE_PREFIX =
            newProperty("guild.disable-prefix", false);

    @Comment("Would you like player to respawn at their guild home (if they have one) when they die?")
    public static final Property<Boolean> RESPAWN_AT_HOME =
            newProperty("guild.respawn-at-home", false);

    @Comment("Do we want to enable the blacklist?")
    public static final Property<Boolean> BLACKLIST_TOGGLE =
            newProperty("guild.blacklist.enabled", true);

    @Comment("Do we want the blacklist to be case sensitive?")
    public static final Property<Boolean> BLACKLIST_SENSITIVE =
            newProperty("guild.blacklist.case-sensitive", true);

    @Comment("What words would you like to blacklist from being used?")
    public static final Property<List<String>> BLACKLIST_WORDS =
            newListProperty("guild.blacklist.words", "crap", "ass", "stupid");

    @Comment({"This is the style used when a message sent in guild chat.", "As of 3.4.7, this now supports {display-name} to show the display name of a player."})
    public static final Property<String> GUILD_CHAT_FORMAT =
            newProperty("guild.format.chat", "&7&l[Guild Chat]&r &b[{role}&b]&r &b {player}: {message}");

    @Comment("This is the style used when a message is sent to ally chat")
    public static final Property<String> ALLY_CHAT_FORMAT =
            newProperty("guild.format.ally-chat", "&7&l[Ally Chat]&r &b[{guild}&b]&r &b {player}: {message}");

    @Comment("Similar to the one above, just for the admins spying.")
    public static final Property<String> SPY_CHAT_FORMAT =
            newProperty("guild.format.spy", "&7&l[Guild Spy]&r &b[{guild}&b]&r &b[{role}&b]&r &b {player}: {message}");

    @Comment("Would you like to log the guild chat to console?")
    public static final Property<Boolean> LOG_GUILD_CHAT =
            newProperty("guild.format.log-guild-chat", false);

    @Comment("Would you like to log the ally chat to console?")
    public static final Property<Boolean> LOG_ALLY_CHAT =
            newProperty("guild.format.log-ally-chat", false);

    @Comment("The left bracket in the placeholder")
    public static final Property<String> FORMAT_BRACKET_LEFT =
            newProperty("guild.format.placeholder-design.left-bracket", "[");

    @Comment("The content of the placeholder. Either will be {name} or {prefix}")
    public static final Property<String> FORMAT_CONTENT =
            newProperty("guild.format.placeholder-design.content", "{name}");

    @Comment("What to show instead of the placeholder if there's no guild")
    public static final Property<String> FORMAT_NO_GUILD =
            newProperty("guild.format.placeholder-design.no-guild", "");

    @Comment("The right bracket in the placeholder")
    public static final Property<String> FORMAT_BRACKET_RIGHT =
            newProperty("guild.format.placeholder-design.right-bracket", "]");

    @Comment("Do we want people in the same guild to be able to damage each other?")
    public static final Property<Boolean> GUILD_DAMAGE =
            newProperty("guild.damage.guild", false);

    @Comment("Do we want allies to be able to damage each other?")
    public static final Property<Boolean> ALLY_DAMAGE =
            newProperty("guild.damage.ally", false);

    @Comment("Would you like to send players their guild's motd on login?")
    public static final Property<Boolean> MOTD_ON_LOGIN =
            newProperty("guild.motd-on-login", true);


    private GuildSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Used for {GUILD_FORMATTED} and %guilds_formatted%"
        };
        conf.setComment("guild.format.placeholder-design", pluginHeader);
    }
}
