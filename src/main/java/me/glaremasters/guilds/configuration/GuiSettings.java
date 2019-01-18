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
public class GuiSettings implements SettingsHolder {

    private static String listPath = "guis.guild-list.";
    private static String buffPath = "guis.guild-buffs.";

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_LIST_NAME =
            newProperty(listPath + "gui-name", "Guild List");

    @Comment("What should the name of the all the items be in the inventory?")
    public static final Property<String> GUILD_LIST_ITEM_NAME =
            newProperty(listPath + "item-name", "&f{player}'s Guild");

    @Comment("What item should players click to go to the next page?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM =
            newProperty(listPath + "next-page-item", "EMPTY_MAP");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_NEXT_PAGE_ITEM_NAME =
            newProperty(listPath + "next-page-item-name", "&fNext Page");

    @Comment("What item should players click to go to the previous page?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM =
            newProperty(listPath + "previous-page-item", "EMPTY_MAP");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PREVIOUS_PAGE_ITEM_NAME =
            newProperty(listPath + "previous-page-item-name", "&fPrevious Page");

    @Comment("What item should players be able to hover over to see what page they are currently on?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM =
            newProperty(listPath + "page-number-item", "DIAMOND_BLOCK");

    @Comment("What should the name of this item be?")
    public static final Property<String> GUILD_LIST_PAGE_NUMBER_ITEM_NAME =
            newProperty(listPath + "page-number-item-name", "&fPage: {page}");

    @Comment("What should be the default texture url for textures that fail to load in?")
    public static final Property<String> GUILD_LIST_HEAD_DEFAULT_URL =
            newProperty(listPath + "head-default-url", "http://textures.minecraft.net/texture/1a1654ce1fd5deea16c151586f21c63d130f1a5a122eef098133b4f92f6f55");

    @Comment({"You are free to design this to your liking", "This is just an example of all the available placeholders that you can use for the lore!"})
    public static final Property<List<String>> GUILD_LIST_HEAD_LORE =
            newListProperty(listPath + "head-lore", "&cName&8: &a{guild-name}", "&cPrefix&8: &a{guild-prefix}", "&cMaster&8: &a{guild-master}", "&cStatus&8: &a{guild-status}", "&cTier&8: &a{guild-tier}", "&cBalance&8: &a{guild-balance}", "&cMember Count&8: &a{guild-member-count}");

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_BUFF_NAME =
            newProperty(buffPath + "gui-name", "Guild Buffs");

    @Comment("Do we want to allow users to have more than one buff at a time?")
    public static final Property<Boolean> BUFF_STACKING =
            newProperty(buffPath + "buff-stacking", false);

    @Comment("What do you want to name the buff?")
    public static final Property<String> HASTE_NAME =
            newProperty(buffPath + "haste.name", "Substance of the Redmod Graff");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> HASTE_PRICE =
            newProperty(buffPath + "haste.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> HASTE_TIME =
            newProperty(buffPath + "haste.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> HASTE_AMPLIFIER =
            newProperty(buffPath + "haste.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> HASTE_ICON =
            newProperty(buffPath + "haste.icon", "FEATHER");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> HASTE_LORE =
            newListProperty(buffPath + "haste.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bmining speed for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> HASTE_DISPLAY =
            newProperty(buffPath + "haste.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> SPEED_NAME =
            newProperty(buffPath + "speed.name", "Blessing of the Cheetah");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> SPEED_PRICE =
            newProperty(buffPath + "speed.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> SPEED_TIME =
            newProperty(buffPath + "speed.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> SPEED_AMPLIFIER =
            newProperty(buffPath + "speed.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> SPEED_ICON =
            newProperty(buffPath + "speed.icon", "SUGAR");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> SPEED_LORE =
            newListProperty(buffPath + "speed.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bmovement speed for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> SPEED_DISPLAY =
            newProperty(buffPath + "speed.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> FR_NAME =
            newProperty(buffPath + "fire-resistance.name", "Scales of the Dragon");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> FR_PRICE =
            newProperty(buffPath + "fire-resistance.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> FR_TIME =
            newProperty(buffPath + "fire-resistance.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> FR_AMPLIFIER =
            newProperty(buffPath + "fire-resistance.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> FR_ICON =
            newProperty(buffPath + "fire-resistance.icon", "BLAZE_POWDER");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> FR_LORE =
            newListProperty(buffPath + "fire-resistance.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bfire resistance for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> FR_DISPLAY =
            newProperty(buffPath + "fire-resistance.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> NV_NAME =
            newProperty(buffPath + "night-vision.name", "Eyes of the Lurking Demon");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> NV_PRICE =
            newProperty(buffPath + "night-vision.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> NV_TIME =
            newProperty(buffPath + "night-vision.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> NV_AMPLIFIER =
            newProperty(buffPath + "night-vision.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> NV_ICON =
            newProperty(buffPath + "night-vision.icon", "REDSTONE_TORCH_ON");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> NV_LORE =
            newListProperty(buffPath + "night-vision.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bnight vision for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> NV_DISPLAY =
            newProperty(buffPath + "night-vision.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> INVISIBILITY_NAME =
            newProperty(buffPath + "invisibility.name", "Feet of the Ghostly Walker");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> INVISIBILITY_PRICE =
            newProperty(buffPath + "invisibility.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> INVISIBILITY_TIME =
            newProperty(buffPath + "invisibility.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> INVISIBILITY_AMPLIFIER =
            newProperty(buffPath + "invisibility.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> INVISIBILITY_ICON =
            newProperty(buffPath + "invisibility.icon", "EYE_OF_ENDER");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> INVISIBILITY_LORE =
            newListProperty(buffPath + "invisibility.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&binvisibility for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> INVISIBILITY_DISPLAY =
            newProperty(buffPath + "invisibility.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> STRENGTH_NAME =
            newProperty(buffPath + "strength.name", "Mighty Strength of the Pouncing Lion");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> STRENGTH_PRICE =
            newProperty(buffPath + "strength.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> STRENGTH_TIME =
            newProperty(buffPath + "strength.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> STRENGTH_AMPLIFIER =
            newProperty(buffPath + "strength.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> STRENGTH_ICON =
            newProperty(buffPath + "strength.icon", "DIAMOND_SWORD");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> STRENGTH_LORE =
            newListProperty(buffPath + "strength.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bstrength for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> STRENGTH_DISPLAY =
            newProperty(buffPath + "strength.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> JUMP_NAME =
            newProperty(buffPath + "jump.name", "Bounce of the Quick Witted Rabbit");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> JUMP_PRICE =
            newProperty(buffPath + "jump.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> JUMP_TIME =
            newProperty(buffPath + "jump.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> JUMP_AMPLIFIER =
            newProperty(buffPath + "jump.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> JUMP_ICON =
            newProperty(buffPath + "jump.icon", "DIAMOND_BOOTS");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> JUMP_LORE =
            newListProperty(buffPath + "jump.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bjump for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> JUMP_DISPLAY =
            newProperty(buffPath + "jump.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> WB_NAME =
            newProperty(buffPath + "water-breathing.name", "Bounce of the Quick Witted Rabbit");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> WB_PRICE =
            newProperty(buffPath + "water-breathing.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> WB_TIME =
            newProperty(buffPath + "water-breathing.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> WB_AMPLIFIER =
            newProperty(buffPath + "water-breathing.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> WB_ICON =
            newProperty(buffPath + "water-breathing.icon", "DIAMOND_BOOTS");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> WB_LORE =
            newListProperty(buffPath + "water-breathing.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bwater-breathing for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> WB_DISPLAY =
            newProperty(buffPath + "water-breathing.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> REGENERATION_NAME =
            newProperty(buffPath + "regeneration.name", "Bounce of the Quick Witted Rabbit");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> REGENERATION_PRICE =
            newProperty(buffPath + "regeneration.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> REGENERATION_TIME =
            newProperty(buffPath + "regeneration.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> REGENERATION_AMPLIFIER =
            newProperty(buffPath + "regeneration.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> REGENERATION_ICON =
            newProperty(buffPath + "regeneration.icon", "DIAMOND_BOOTS");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> REGENERATION_LORE =
            newListProperty(buffPath + "regeneration.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bregeneration for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> REGENERATION_DISPLAY =
            newProperty(buffPath + "regeneration.display", true);


    private GuiSettings() {
    }

}
