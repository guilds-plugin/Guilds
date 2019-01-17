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
 * Time: 1:11 PM
 */
public class GuiSettings implements SettingsHolder {

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_LIST_NAME =
            newProperty("guis.guild-list.gui-name", "Guild List");

    @Comment("What should the name of the all the items be in the inventory?")
    public static final Property<String> GUILD_LIST_ITEM_NAME =
            newProperty("guis.guild-list.item-name", "&f{player}'s Guild");

    @Comment("What item should players click to go to the next page?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM =
            newProperty("guis.guild-list.next-page-item", "EMPTY_MAP");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM_NAME =
            newProperty("guis.guild-list.next-page-item-name", "&fNext Page");

    @Comment("What item should players click to go to the previous page?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM =
            newProperty("guis.guild-list.previous-page-item", "EMPTY_MAP");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM_NAME =
            newProperty("guis.guild-list.previous-page-item-name", "&fPrevious Page");

    @Comment("What item should players be able to hover over to see what page they are currently on?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM =
            newProperty("guis.guild-list.page-number-item", "DIAMOND_BLOCK");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM_NAME =
            newProperty("guis.guild-list.page-number-item-name", "&fPage: {page}");

    @Comment("What should be the default texture url for textures that fail to load in?")
    public static final Property<String> GUILD_LIST_HEAD_DEFAULT_URL =
            newProperty("guis.guild-list.head-default-url", "http://textures.minecraft.net/texture/1a1654ce1fd5deea16c151586f21c63d130f1a5a122eef098133b4f92f6f55");

    @Comment({"You are free to design this to your liking", "This is just an example of all the available placeholders that you can use for the lore!"})
    public static final Property<List<String>> GUILD_LIST_HEAD_LORE =
            newListProperty("guis.guild-list.head-lore", "&cName&8: &a{guild-name}", "&cPrefix&8: &a{guild-prefix}", "&cMaster&8: &a{guild-master}", "&cStatus&8: &a{guild-status}", "&cTier&8: &a{guild-tier}", "&cBalance&8: &a{guild-balance}", "&cMember Count&8: &a{guild-member-count}");

    public static final Property<String> GUILD_BUFF_NAME =
            newProperty("guis.guild-buffs.gui-name", "Guild Buffs");

    public static final Property<Boolean> BUFF_STACKING =
            newProperty("guis.guild-buffs.buff-stacking", false);

    public static final Property<String> HASTE_NAME =
            newProperty("guis.guild-buffs.haste.name", "Substance of the Redmod Graff");

    public static final Property<Double> HASTE_PRICE =
            newProperty("guis.guild-buffs.haste.price", 60.0);

    public static final Property<Integer> HASTE_TIME =
            newProperty("guis.guild-buffs.haste.time", 60);

    public static final Property<Integer> HASTE_AMPLIFIER =
            newProperty("guis.guild-buffs.haste.amplifier", 0);

    public static final Property<String> HASTE_ICON =
            newProperty("guis.guild-buffs.haste.icon", "FEATHER");

    public static final Property<List<String>> HASTE_LORE =
            newListProperty("guis.guild-buffs.haste.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bmining speed for a certain amount of time.");

    public static final Property<Boolean> HASTE_DISPLAY =
            newProperty("guis.guild-buffs.haste.display", true);


    private GuiSettings() {
    }

}
