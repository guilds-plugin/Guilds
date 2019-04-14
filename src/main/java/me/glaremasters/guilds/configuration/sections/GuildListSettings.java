package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by Glare
 * Date: 4/13/2019
 * Time: 8:24 PM
 */
public class GuildListSettings implements SettingsHolder {

    private static final String LIST_PATH = "guis.guild-list.";

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_LIST_NAME =
            newProperty(LIST_PATH + "gui-name", "Guild List");

    @Comment("What should the name of the all the items be in the inventory?")
    public static final Property<String> GUILD_LIST_ITEM_NAME =
            newProperty(LIST_PATH + "item-name", "&f{player}'s Guild");

/*    @Comment("What item should players click to go to the next page?")
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
            newProperty(LIST_PATH + "head-default-url", "http://textures.minecraft.net/texture/1a1654ce1fd5deea16c151586f21c63d130f1a5a122eef098133b4f92f6f55");*/

    @Comment({"You are free to design this to your liking", "This is just an example of all the available placeholders that you can use for the lore!"})
    public static final Property<List<String>> GUILD_LIST_HEAD_LORE =
            newListProperty(LIST_PATH + "head-lore", "&cName&8: &a{guild-name}", "&cPrefix&8: &a{guild-prefix}", "&cMaster&8: &a{guild-master}", "&cStatus&8: &a{guild-status}", "&cTier&8: &a{guild-tier}", "&cBalance&8: &a{guild-balance}", "&cMember Count&8: &a{guild-member-count}");


    private GuildListSettings() {}
}
