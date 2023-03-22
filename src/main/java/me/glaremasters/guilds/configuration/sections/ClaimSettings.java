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
 * Date: 1/18/2019
 * Time: 2:26 PM
 */
public class ClaimSettings implements SettingsHolder {

    @Comment({"This is the number of blocks around the player it will try to create the region.",
            "Keep in mind this is the RADIUS, it will go this many blocks in both directions.",
            "For example, if you take the default 15, it'll do 30 total as it will go 15 blocks in both directions.",
            "This is a CUBOID region, not SPHERE."
    })
    public static final Property<Integer> RADIUS =
            newProperty("claims.radius", 15);

    @Comment({"Customize the entrance and exit message of joining claims.",
            "Supports {prefix} for guild prefix and {guild} for guild name.",
            "Also supports color codes!"
    })
    public static final Property<String> ENTER_MESSAGE =
            newProperty("claims.enter-message", "&aNow entering &d{guild}'s &aclaim!");

    public static final Property<String> EXIT_MESSAGE =
            newProperty("claims.exit-message", "&aNow leaving &d{guild}'s &aclaim!");

    @Comment("Would you like to disable guild claiming in specific worlds?")
    public static final Property<List<String>> DISABLED_WORLDS =
            newListProperty("claims.disabled-worlds", "");

    @Comment({"Would you like to enable claim signs?", "Format - ",
    "First Line: [Guild Claim]", "Second Line: WorldGuard Region Name", "Third Line: Price"})
    public static final Property<Boolean> CLAIM_SIGNS =
            newProperty("claims.claim-signs", false);

    @Comment("The text to look for on a sign for the guild claims")
    public static final Property<String> CLAIM_SIGN_TEXT =
            newProperty("claims.claim-sign-text", "[Guild Claim]");

    @Comment({"Would you like to make it so that claims can only be aquired through the purchasing with signs?",
            "This will disable the regular claim commands."})
    public static final Property<Boolean> FORCE_CLAIM_SIGNS =
            newProperty("claims.force-claim-signs", false);

    private ClaimSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "This section of the config will allow you to handle guild land claiming.",
                "Remember that the enable / disable for this is the WorldGuard Hook at the TOP of the config.",
                "There are multiple options when it comes to guild claims. For the time being, all guilds will only get one claim."
        };
        conf.setComment("claims", pluginHeader);
    }
}
