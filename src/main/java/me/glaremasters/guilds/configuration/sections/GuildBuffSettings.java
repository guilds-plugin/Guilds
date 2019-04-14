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
public class GuildBuffSettings implements SettingsHolder {

    private static final String BUFF_PATH = "guis.guild-buffs.buffs.";

    @Comment("What should the name of the inventory be?")
    public static final Property<String> GUILD_BUFF_NAME =
            newProperty("guis.guild-buffs.gui-name", "Guild Buffs");

    @Comment("Do we want to allow users to have more than one buff at a time?")
    public static final Property<Boolean> BUFF_STACKING =
            newProperty("guis.guild-buffs.buff-stacking", false);

    @Comment("What do you want to name the buff?")
    public static final Property<String> HASTE_NAME =
            newProperty(BUFF_PATH + "haste.name", "&a&lSubstance of the Redmod Graff");

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
            newProperty(BUFF_PATH + "haste.type", "FAST_DIGGING");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> HASTE_LORE =
            newListProperty(BUFF_PATH + "haste.description", "&aType » &7Haste", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> HASTE_DISPLAY =
            newProperty(BUFF_PATH + "haste.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> SPEED_NAME =
            newProperty(BUFF_PATH + "speed.name", "&a&lBlessing of the Cheetah");

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
            newListProperty(BUFF_PATH + "speed.description", "&aType » &7Speed", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> SPEED_DISPLAY =
            newProperty(BUFF_PATH + "speed.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> FR_NAME =
            newProperty(BUFF_PATH + "fire-resistance.name", "&a&lScales of the Dragon");

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
            newListProperty(BUFF_PATH + "fire-resistance.description", "&aType » &7Fire-Resistance", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> FR_DISPLAY =
            newProperty(BUFF_PATH + "fire-resistance.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> NV_NAME =
            newProperty(BUFF_PATH + "night-vision.name", "&a&lEyes of the Lurking Demon");

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
            newListProperty(BUFF_PATH + "night-vision.description", "&aType » &7Night-Vision", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> NV_DISPLAY =
            newProperty(BUFF_PATH + "night-vision.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> INVISIBILITY_NAME =
            newProperty(BUFF_PATH + "invisibility.name", "&a&lFeet of the Ghostly Walker");

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
            newListProperty(BUFF_PATH + "invisibility.description", "&aType » &7Invisibility", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> INVISIBILITY_DISPLAY =
            newProperty(BUFF_PATH + "invisibility.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> STRENGTH_NAME =
            newProperty(BUFF_PATH + "strength.name", "&a&lMighty Strength of the Pouncing Lion");

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
            newProperty(BUFF_PATH + "strength.type", "INCREASE_DAMAGE");

    @Comment("You can put as much as you want here")
    public static final Property<List<String>> STRENGTH_LORE =
            newListProperty(BUFF_PATH + "strength.description", "&aType » &7Strength", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> STRENGTH_DISPLAY =
            newProperty(BUFF_PATH + "strength.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> JUMP_NAME =
            newProperty(BUFF_PATH + "jump.name", "&a&lBounce of the Quick Witted Rabbit");

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
            newListProperty(BUFF_PATH + "jump.description",  "&aType » &7Jump", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> JUMP_DISPLAY =
            newProperty(BUFF_PATH + "jump.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> WB_NAME =
            newProperty(BUFF_PATH + "water-breathing.name", "&a&lLungs of the Albino Shark");

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
            newListProperty(BUFF_PATH + "water-breathing.description", "&aType » &7Water-Breathing", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> WB_DISPLAY =
            newProperty(BUFF_PATH + "water-breathing.display", true);

    @Comment("What do you want to name the buff?")
    public static final Property<String> REGENERATION_NAME =
            newProperty(BUFF_PATH + "regeneration.name", "&a&lIntegrity of the Mystic Witch");

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
            newListProperty(BUFF_PATH + "regeneration.description", "&aType » &7Regeneration", "&aLength » &760 Seconds", "&aCost » &7$60");

    @Comment("Do you want this buff to show in-game?")
    public static final Property<Boolean> REGENERATION_DISPLAY =
            newProperty(BUFF_PATH + "regeneration.display", true);

    @Comment("What do you want the name of the Vault to be?")
    public static final Property<String> VAULT_NAME =
            newProperty("guis.vault.name", "Guild Vault");

    private GuildBuffSettings() {
    }

}
