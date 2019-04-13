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

    private static final String BUFF_PATH = "guis.guild-buffs.buffs.";

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_BUFF_NAME =
            newProperty("guis.guild-buffs.gui-name", "Guild Buffs");

    @Comment("Do we want to allow users to have more than one buff at a time?")
    public static final Property<Boolean> BUFF_STACKING =
            newProperty("guis.guild-buffs.buff-stacking", false);

    @Comment("What do you want to name the buff?")
    public static final Property<String> HASTE_NAME =
            newProperty(BUFF_PATH + "haste.name", "Substance of the Redmod Graff");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> HASTE_PRICE =
            newProperty(BUFF_PATH + "haste.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> HASTE_TIME =
            newProperty(BUFF_PATH + "haste.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> HASTE_AMPLIFIER =
            newProperty(BUFF_PATH + "haste.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> HASTE_ICON =
            newProperty(BUFF_PATH + "haste.icon", "FEATHER");

    @Comment("What type of potion is this?")
    public static final Property<String> HASTE_TYPE =
            newProperty(BUFF_PATH + "haste.type", "HASTE");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> HASTE_LORE =
            newListProperty(BUFF_PATH + "haste.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bmining speed for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> HASTE_DISPLAY =
            newProperty(BUFF_PATH + "haste.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> SPEED_NAME =
            newProperty(BUFF_PATH + "speed.name", "Blessing of the Cheetah");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> SPEED_PRICE =
            newProperty(BUFF_PATH + "speed.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> SPEED_TIME =
            newProperty(BUFF_PATH + "speed.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> SPEED_AMPLIFIER =
            newProperty(BUFF_PATH + "speed.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> SPEED_ICON =
            newProperty(BUFF_PATH + "speed.icon", "SUGAR");

    @Comment("What type of potion is this?")
    public static final Property<String> SPEED_TYPE =
            newProperty(BUFF_PATH + "speed.type", "SPEED");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> SPEED_LORE =
            newListProperty(BUFF_PATH + "speed.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bmovement speed for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> SPEED_DISPLAY =
            newProperty(BUFF_PATH + "speed.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> FR_NAME =
            newProperty(BUFF_PATH + "fire-resistance.name", "Scales of the Dragon");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> FR_PRICE =
            newProperty(BUFF_PATH + "fire-resistance.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> FR_TIME =
            newProperty(BUFF_PATH + "fire-resistance.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> FR_AMPLIFIER =
            newProperty(BUFF_PATH + "fire-resistance.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> FR_ICON =
            newProperty(BUFF_PATH + "fire-resistance.icon", "BLAZE_POWDER");

    @Comment("What type of potion is this?")
    public static final Property<String> FR_TYPE =
            newProperty(BUFF_PATH + "fire-resistance.type", "FIRE_RESISTANCE");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> FR_LORE =
            newListProperty(BUFF_PATH + "fire-resistance.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bfire resistance for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> FR_DISPLAY =
            newProperty(BUFF_PATH + "fire-resistance.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> NV_NAME =
            newProperty(BUFF_PATH + "night-vision.name", "Eyes of the Lurking Demon");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> NV_PRICE =
            newProperty(BUFF_PATH + "night-vision.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> NV_TIME =
            newProperty(BUFF_PATH + "night-vision.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> NV_AMPLIFIER =
            newProperty(BUFF_PATH + "night-vision.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> NV_ICON =
            newProperty(BUFF_PATH + "night-vision.icon", "REDSTONE_TORCH_ON");

    @Comment("What type of potion is this?")
    public static final Property<String> NV_TYPE =
            newProperty(BUFF_PATH + "night-vision.type", "NIGHT_VISION");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> NV_LORE =
            newListProperty(BUFF_PATH + "night-vision.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bnight vision for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> NV_DISPLAY =
            newProperty(BUFF_PATH + "night-vision.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> INVISIBILITY_NAME =
            newProperty(BUFF_PATH + "invisibility.name", "Feet of the Ghostly Walker");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> INVISIBILITY_PRICE =
            newProperty(BUFF_PATH + "invisibility.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> INVISIBILITY_TIME =
            newProperty(BUFF_PATH + "invisibility.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> INVISIBILITY_AMPLIFIER =
            newProperty(BUFF_PATH + "invisibility.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> INVISIBILITY_ICON =
            newProperty(BUFF_PATH + "invisibility.icon", "EYE_OF_ENDER");

    @Comment("What type of potion is this?")
    public static final Property<String> INVISIBILITY_TYPE =
            newProperty(BUFF_PATH + "invisibility.type", "INVISIBILITY");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> INVISIBILITY_LORE =
            newListProperty(BUFF_PATH + "invisibility.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&binvisibility for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> INVISIBILITY_DISPLAY =
            newProperty(BUFF_PATH + "invisibility.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> STRENGTH_NAME =
            newProperty(BUFF_PATH + "strength.name", "Mighty Strength of the Pouncing Lion");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> STRENGTH_PRICE =
            newProperty(BUFF_PATH + "strength.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> STRENGTH_TIME =
            newProperty(BUFF_PATH + "strength.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> STRENGTH_AMPLIFIER =
            newProperty(BUFF_PATH + "strength.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> STRENGTH_ICON =
            newProperty(BUFF_PATH + "strength.icon", "DIAMOND_SWORD");

    @Comment("What type of potion is this?")
    public static final Property<String> STRENGTH_TYPE =
            newProperty(BUFF_PATH + "strength.type", "STRENGTH");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> STRENGTH_LORE =
            newListProperty(BUFF_PATH + "strength.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bstrength for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> STRENGTH_DISPLAY =
            newProperty(BUFF_PATH + "strength.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> JUMP_NAME =
            newProperty(BUFF_PATH + "jump.name", "Bounce of the Quick Witted Rabbit");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> JUMP_PRICE =
            newProperty(BUFF_PATH + "jump.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> JUMP_TIME =
            newProperty(BUFF_PATH + "jump.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> JUMP_AMPLIFIER =
            newProperty(BUFF_PATH + "jump.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> JUMP_ICON =
            newProperty(BUFF_PATH + "jump.icon", "DIAMOND_BOOTS");

    @Comment("What type of potion is this?")
    public static final Property<String> JUMP_TYPE =
            newProperty(BUFF_PATH + "jump.type", "JUMP_BOOST");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> JUMP_LORE =
            newListProperty(BUFF_PATH + "jump.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bjump for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> JUMP_DISPLAY =
            newProperty(BUFF_PATH + "jump.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> WB_NAME =
            newProperty(BUFF_PATH + "water-breathing.name", "Lungs of the Albino Shark");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> WB_PRICE =
            newProperty(BUFF_PATH + "water-breathing.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> WB_TIME =
            newProperty(BUFF_PATH + "water-breathing.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> WB_AMPLIFIER =
            newProperty(BUFF_PATH + "water-breathing.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> WB_ICON =
            newProperty(BUFF_PATH + "water-breathing.icon", "BUCKET");

    @Comment("What type of potion is this?")
    public static final Property<String> WB_TYPE =
            newProperty(BUFF_PATH + "water-breathing.type", "WATER_BREATHING");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> WB_LORE =
            newListProperty(BUFF_PATH + "water-breathing.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bwater-breathing for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> WB_DISPLAY =
            newProperty(BUFF_PATH + "water-breathing.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> REGENERATION_NAME =
            newProperty(BUFF_PATH + "regeneration.name", "Integrity of the Mystic Witch");

    @Comment("How much do you want the buff to cost?")
    public static final Property<Double> REGENERATION_PRICE =
            newProperty(BUFF_PATH + "regeneration.price", 60.0);

    @Comment("How long (in second) should the buff last?")
    public static final Property<Integer> REGENERATION_TIME =
            newProperty(BUFF_PATH + "regeneration.time", 60);

    @Comment("How strong do you want the buff to be? 0 = Potion Level 1, 1 = Potion Level 2, etc...")
    public static final Property<Integer> REGENERATION_AMPLIFIER =
            newProperty(BUFF_PATH + "regeneration.amplifier", 0);

    @Comment("What item do you want to represent the buff?")
    public static final Property<String> REGENERATION_ICON =
            newProperty(BUFF_PATH + "regeneration.icon", "EMERALD");

    @Comment("What type of potion is this?")
    public static final Property<String> REGENERATION_TYPE =
            newProperty(BUFF_PATH + "regeneration.type", "REGENERATION");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> REGENERATION_LORE =
            newListProperty(BUFF_PATH + "regeneration.description", "&bThis buff will allow you and your", "&bGuild Members to obtain increased", "&bregeneration for a certain amount of time.");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> REGENERATION_DISPLAY =
            newProperty(BUFF_PATH + "regeneration.display", true);

    @Comment("What do you want the name of the Vault to be?")
    public static final Property<String> VAULT_NAME =
            newProperty("guis.vault.name", "Guild Vault");

    private GuiSettings() {
    }

}
