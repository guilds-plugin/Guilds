/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class GuiSettings implements SettingsHolder {

    private static final String LIST_PATH = "guis.guild-list.";
    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_LIST_NAME =
            newProperty(LIST_PATH + "gui-name", "Guild List");
    @Comment("What should the name of the all the items be in the inventory?")
    public static final Property<String> GUILD_LIST_ITEM_NAME =
            newProperty(LIST_PATH + "item-name", "&f{player}'s Guild");
    @Comment("What item should players click to go to the next page?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM =
            newProperty(LIST_PATH + "next-page-item", "EMPTY_MAP");
    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM_NAME =
            newProperty(LIST_PATH + "next-page-item-name", "&fNext Page");
    @Comment("What item should players click to go to the previous page?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM =
            newProperty(LIST_PATH + "previous-page-item", "EMPTY_MAP");
    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM_NAME =
            newProperty(LIST_PATH + "previous-page-item-name", "&fPrevious Page");
    @Comment("What item should players be able to hover over to see what page they are currently on?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM =
            newProperty(LIST_PATH + "page-number-item", "DIAMOND_BLOCK");
    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM_NAME =
            newProperty(LIST_PATH + "page-number-item-name", "&fPage: {page}");
    @Comment("What should be the default texture url for textures that fail to load in?")
    public static final Property<String> GUILD_LIST_HEAD_DEFAULT_URL =
            newProperty(LIST_PATH + "head-default-url", "http://textures.minecraft.net/texture/1a1654ce1fd5deea16c151586f21c63d130f1a5a122eef098133b4f92f6f55");
    @Comment({"You are free to design this to your liking", "This is just an example of all the available placeholders that you can use for the lore!"})
    public static final Property<List<String>> GUILD_LIST_HEAD_LORE =
            newListProperty(LIST_PATH + "head-lore", "&cName&8: &a{guild-name}", "&cPrefix&8: &a{guild-prefix}", "&cMaster&8: &a{guild-master}", "&cStatus&8: &a{guild-status}", "&cTier&8: &a{guild-tier}", "&cBalance&8: &a{guild-balance}", "&cMember Count&8: &a{guild-member-count}");

    private GuiSettings() {
    }

}
