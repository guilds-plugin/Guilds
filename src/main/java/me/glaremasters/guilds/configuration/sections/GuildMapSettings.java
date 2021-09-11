/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
 * Created by Glare
 * Date: 4/13/2019
 * Time: 8:24 PM
 */
public class GuildMapSettings implements SettingsHolder {

    private static final String LIST_PATH = "guis.guild-map.";

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_MAP_NAME =
            newProperty(LIST_PATH + "gui-name", "Area Map");

    @Comment({"What should the name of the all the items be in the inventory?",
            "Currently supports {player} and {guild}."})
    public static final Property<String> GUILD_MAP_ITEM_NAME =
            newProperty(LIST_PATH + "guilds-on-map-name", "&f{player}'s Guild");

    @Comment({"What should the name of the all the items be in the inventory?",
            "Currently supports {player} and {guild}."})
    public static final Property<String> GUILD_MAP_CENTER_NAME =
            newProperty(LIST_PATH + "center-item-name", "You");

    @Comment("What should be the default texture url for textures that fail to load in? Refer to the Guild Manage settings to see how to change the texture!")
    public static final Property<String> GUILD_MAP_HEAD_DEFAULT_URL =
            newProperty(LIST_PATH + "head-default-url", "7a2df315b43583b1896231b77bae1a507dbd7e43ad86c1cfbe3b2b8ef3430e9e");

    @Comment("What should be the texture url for the point representing the person upon the map?")
    public static final Property<String> GUILD_MAP_CENTER_HEAD_DEFAULT_URL =
            newProperty(LIST_PATH + "head-center-url", "7a2df315b43583b1896231b77bae1a507dbd7e43ad86c1cfbe3b2b8ef3430e9e");

    @Comment("Do we want to try to use skull textures or just ignore them and use the one provided?")
    public static final Property<Boolean> USE_DEFAULT_TEXTURE =
            newProperty(LIST_PATH + "use-default-texture", false);

    @Comment({"You are free to design this to your liking", "This is just an example of all the available placeholders that you can use for the lore!",
            "Note: With v3.6.7 and on, you can now use {guild-tier-name} for the name of the tier.",
            "Also, from v3.6.7 and on, {guild-status} will now apply from what you set for the guild-info GUI for the status being public or private.",
            "In version 3.5.2.2, {guild-challenge-wins} and {guild-challenge-loses} have been added.",
            "In version 3.5.3.3, {creation} was added to display the creation date of the guild"})
    public static final Property<List<String>> GUILD_MAP_ITEM_LORE =
            newListProperty(LIST_PATH + "head-lore", "&cName&8: &a{guild-name}", "&cPrefix&8: &a{guild-prefix}", "&cMaster&8: &a{guild-master}", "&cStatus&8: &a{guild-status}", "&cTier&8: &a{guild-tier}", "&cBalance&8: &a{guild-balance}", "&cMember Count&8: &a{guild-member-count}", "&cCreation Date&8: &a{creation}");


    private GuildMapSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Use the following website to get available materials: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html",
                "This can work across all MC versions and will attempt to use the proper material based on what version of MC you are using."
        };
        conf.setComment("guis", pluginHeader);
    }
}
