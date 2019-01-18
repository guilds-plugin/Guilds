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

package me.glaremasters.guilds.configuration;

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
public class TierSettings implements SettingsHolder {

    private static String listPath = "tiers.list.";

    @Comment("Should permissions carry over between tiers?")
    public static final Property<Boolean> CARRY_OVER =
            newProperty("tiers.carry-over", true);

    @Comment("Which level tier is this? 1 is the default.")
    public static final Property<Integer> ONE_LEVEL =
            newProperty(listPath + "1.level", 1);

    @Comment("What is the name of this tier?")
    public static final Property<String> ONE_NAME =
            newProperty(listPath + "1.name", "Bronze");

    @Comment("How much is this tier? (If first tier, keep as same price as cost creation)")
    public static final Property<Double> ONE_COST =
            newProperty(listPath + "1.cost", 0.0);

    @Comment("How many members can be in a guild of this tier?")
    public static final Property<Integer> ONE_MAX_MEMBERS =
            newProperty(listPath + "1.max-members", 15);

    @Comment("How much extra XP should drop from mobs?")
    public static final Property<Double> ONE_MOB =
            newProperty(listPath + "1.mob-xp-multiplier", 1.0);

    @Comment("How much extra damage should be done?")
    public static final Property<Double> ONE_DAMAGE =
            newProperty(listPath + "1.damage-multiplier", 1.0);

    @Comment("How much can this tier hold in the bank?")
    public static final Property<Double> ONE_MAX_BANK =
            newProperty(listPath + "1.max-bank-balance", 10000.0);

    @Comment("How many members should be in a guild for it to be able to rankup?")
    public static final Property<Integer> ONE_MEMBERS_REQUIRE =
            newProperty(listPath + "1.members-to-rankup", 0);

    @Comment("If you wish to give this tier extra permissions, put them here.")
    public static final Property<List<String>> ONE_PERMS =
            newListProperty(listPath + "1.permissions", "");

    @Comment("Which level tier is this? 1 is the default.")
    public static final Property<Integer> TWO_LEVEL =
            newProperty(listPath + "2.level", 2);

    @Comment("What is the name of this tier?")
    public static final Property<String> TWO_NAME =
            newProperty(listPath + "2.name", "Silver");

    @Comment("How much is this tier? (If first tier, keep as same price as cost creation)")
    public static final Property<Double> TWO_COST =
            newProperty(listPath + "2.cost", 200.0);

    @Comment("How many members can be in a guild of this tier?")
    public static final Property<Integer> TWO_MAX_MEMBERS =
            newProperty(listPath + "2.max-members", 30);

    @Comment("How much extra XP should drop from mobs?")
    public static final Property<Double> TWO_MOB =
            newProperty(listPath + "2.mob-xp-multiplier", 2.0);

    @Comment("How much extra damage should be done?")
    public static final Property<Double> TWO_DAMAGE =
            newProperty(listPath + "2.damage-multiplier", 1.0);

    @Comment("How much can this tier hold in the bank?")
    public static final Property<Double> TWO_MAX_BANK =
            newProperty(listPath + "2.max-bank-balance", 20000.0);

    @Comment("How many members should be in a guild for it to be able to rankup?")
    public static final Property<Integer> TWO_MEMBERS_REQUIRE =
            newProperty(listPath + "2.members-to-rankup", 0);

    @Comment("If you wish to give this tier extra permissions, put them here.")
    public static final Property<List<String>> TWO_PERMS =
            newListProperty(listPath + "2.permissions", "");

    @Comment("Which level tier is this? 1 is the default.")
    public static final Property<Integer> THREE_LEVEL =
            newProperty(listPath + "3.level", 3);

    @Comment("What is the name of this tier?")
    public static final Property<String> THREE_NAME =
            newProperty(listPath + "3.name", "Gold");

    @Comment("How much is this tier? (If first tier, keep as same price as cost creation)")
    public static final Property<Double> THREE_COST =
            newProperty(listPath + "3.cost", 300.0);

    @Comment("How many members can be in a guild of this tier?")
    public static final Property<Integer> THREE_MAX_MEMBERS =
            newProperty(listPath + "3.max-members", 50);

    @Comment("How much extra XP should drop from mobs?")
    public static final Property<Double> THREE_MOB =
            newProperty(listPath + "3.mob-xp-multiplier", 3.0);

    @Comment("How much extra damage should be done?")
    public static final Property<Double> THREE_DAMAGE =
            newProperty(listPath + "3.damage-multiplier", 1.0);

    @Comment("How much can this tier hold in the bank?")
    public static final Property<Double> THREE_MAX_BANK =
            newProperty(listPath + "3.max-bank-balance", 30000.0);

    @Comment("How many members should be in a guild for it to be able to rankup?")
    public static final Property<Integer> THREE_MEMBERS_REQUIRE =
            newProperty(listPath + "3.members-to-rankup", 0);

    @Comment("If you wish to give this tier extra permissions, put them here.")
    public static final Property<List<String>> THREE_PERMS =
            newListProperty(listPath + "3.permissions", "");

    private TierSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "This section of the config will talk about various parts of upgrading a guild and allow you to choose how it works.",
                "For \"mob-xp-multiplier\" the default is 1, meaning that it will drop the normal amount of XP for non-upgraded guilds.",
                "DO NOT set it to 0, that will either throw errors or cause mobs to not drop XP.",
                "Keep in mind for the damage-multiplier, it applies to players also, so by default it's set to normal for every tier."
        };
        conf.setComment("tiers", pluginHeader);
    }
}
