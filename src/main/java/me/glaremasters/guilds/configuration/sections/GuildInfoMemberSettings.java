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
import me.glaremasters.guilds.conf.objects.MemberNav;
import me.glaremasters.guilds.conf.objects.MemberNavItem;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.*;

/**
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:20 PM
 */
public class GuildInfoMemberSettings implements SettingsHolder {

    private static final String INFO_PATH = "guis.guild-info-members.";

    @Comment({"How should the menu be sorted?",
            "ROLE: In order from highest role to lowest",
            "NAME: In order by their username",
            "AGE: In order of length in guild"})
    public static final Property<String> SORT_ORDER =
            newProperty(INFO_PATH + "sort", "ROLE");

    @Comment("What would you like the name of the GUI to be?")
    public static final Property<String> GUI_NAME =
            newProperty(INFO_PATH + "name", "&8» &rMembers of {name}");

    @Comment("What material do you want to use to represent members?")
    public static final Property<String> MEMBERS_MATERIAL =
            newProperty(INFO_PATH + "item.material", "EMPTY_MAP");

    @Comment("What do you want the name of the item to be?")
    public static final Property<String> MEMBERS_NAME =
            newProperty(INFO_PATH + "item.name", " ");

    @Comment("What do you want the lore of the item to be?")
    public static final Property<List<String>> MEMBERS_LORE =
            newListProperty(INFO_PATH + "item.lore", "&8• &7Name: &a{name}", "&8• &7Role: &a{role}", "&8• &7Status: {status}", "&8• &7Join Date: &a{join}", "&8• &7Last Login: &a{login}");

    @Comment("What do you want to be what shows when a member is online?")
    public static final Property<String> MEMBERS_ONLINE =
            newProperty(INFO_PATH + "item.online", "&aOnline");

    @Comment("What do you want to be what shows when a member is offline?")
    public static final Property<String> MEMBERS_OFFLINE =
            newProperty(INFO_PATH + "item.offline", "&cOffline");

    @Comment("Set the name and material for the navigation buttons")
    public static final Property<MemberNav> MEMBER_NAV =
            newBeanProperty(MemberNav.class, INFO_PATH + "nav", new MemberNav(new MemberNavItem("EMPTY_MAP", "Next"), new MemberNavItem("EMPTY_MAP", "Previous")));


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
