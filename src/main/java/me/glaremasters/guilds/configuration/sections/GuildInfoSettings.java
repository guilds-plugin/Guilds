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
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:20 PM
 */
public class GuildInfoSettings implements SettingsHolder {

    private static final String INFO_PATH = "guis.guild-info.";

    @Comment({"What would you like the name of the GUI to be?",
    "Currently supports {name} for the name of the guild and {prefix} for the prefix of the guild"})
    public static final Property<String> GUI_NAME =
            newProperty(INFO_PATH + "name", "&8» &r{name}'s Info");

    @Comment("What material do you want the tier button to be?")
    public static final Property<String> TIER_MATERIAL =
            newProperty(INFO_PATH + "tier-material", "DIAMOND");

    @Comment("What do you want the name of the tier button to be?")
    public static final Property<String> TIER_NAME =
            newProperty(INFO_PATH + "tier-name", "&3Guild Tier");

    @Comment("What do you want the lore of the tier button to be?")
    public static final Property<List<String>> TIER_LORE =
            newListProperty(INFO_PATH + "tier-lore", "&8• &7Level: &b{tier}");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> TIER_DISPLAY =
            newProperty(INFO_PATH + "tier-display", true);

    @Comment("What material do you want the bank button to be?")
    public static final Property<String> BANK_MATERIAL =
            newProperty(INFO_PATH + "bank-material", "GOLD_INGOT");

    @Comment("What do you want the name of the bank button to be?")
    public static final Property<String> BANK_NAME =
            newProperty(INFO_PATH + "bank-name", "&6Guild Bank");

    @Comment("What do you want the lore of the bank button to be?")
    public static final Property<List<String >> BANK_LORE =
            newListProperty(INFO_PATH + "bank-lore", "&8• &7Balance: &e{current} &7/ &e{max}");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> BANK_DISPLAY =
            newProperty(INFO_PATH + "bank-display", true);

    @Comment("What material do you want the members button to be?")
    public static final Property<String> MEMBERS_MATERIAL =
            newProperty(INFO_PATH + "members-material", "IRON_HELMET");

    @Comment("What do you want the name of the members button to be?")
    public static final Property<String> MEMBERS_NAME =
            newProperty(INFO_PATH + "members-name", "&5Guild Members");

    @Comment("What do you want the lore of the members button to be?")
    public static final Property<List<String >> MEMBERS_LORE =
            newListProperty(INFO_PATH + "members-lore", "&8• &7Members: &d{current} &7/ &d{max}", "&8• &7Online: &d{online} &7/ &d{current}", "", "&7Click to view members!");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> MEMBERS_DISPLAY =
            newProperty(INFO_PATH + "members-display", true);

    @Comment("What material do you want the status button to be when a guild is public?")
    public static final Property<String> STATUS_MATERIAL_PUBLIC =
            newProperty(INFO_PATH + "status-material.public", "EMERALD");

    @Comment("What material do you want the status button to be when a guild is private?")
    public static final Property<String> STATUS_MATERIAL_PRIVATE =
            newProperty(INFO_PATH + "status-material.private", "REDSTONE");

    @Comment("What do you want the name of the status button to be?")
    public static final Property<String> STATUS_NAME =
            newProperty(INFO_PATH + "status-name-item", "&2Guild Status");

    @Comment("What do you want the status to say if it's public?")
    public static final Property<String> STATUS_PUBLIC =
            newProperty(INFO_PATH + "status-name.public", "&aPublic");

    @Comment("What do you want the status to say if it's private?")
    public static final Property<String> STATUS_PRIVATE =
            newProperty(INFO_PATH + "status-name.private", "&cPrivate");

    @Comment("What do you want the lore of the status button to be?")
    public static final Property<List<String >> STATUS_LORE =
            newListProperty(INFO_PATH + "status-lore", "&8• &7Status: &r{status}");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> STATUS_DISPLAY =
            newProperty(INFO_PATH + "status-display", true);

    @Comment("What material do you want the home button to be?")
    public static final Property<String> HOME_MATERIAL =
            newProperty(INFO_PATH + "home-material", "BED");

    @Comment("What do you want the name of the home button to be?")
    public static final Property<String> HOME_NAME =
            newProperty(INFO_PATH + "home-name", "&cGuild Home");

    @Comment("What do you want the lore of the home button to be?")
    public static final Property<List<String >> HOME_LORE =
            newListProperty(INFO_PATH + "home-lore", "&8• &7Home: &f{coords}");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> HOME_DISPLAY =
            newProperty(INFO_PATH + "home-display", true);

    @Comment("What do you want it to say when a guild doesn't have a home set?")
    public static final Property<String> HOME_EMPTY =
            newProperty(INFO_PATH + "home-empty", "&fNot Set");

    @Comment("Do you want players to be teleported to their guild home when they click this?")
    public static final Property<Boolean> HOME_TELEPORT =
            newProperty(INFO_PATH + "home-teleport", false);

    @Comment("What material do you want the vault button to be?")
    public static final Property<String> VAULT_MATERIAL =
            newProperty(INFO_PATH + "vault-material", "CHEST");

    @Comment("What do you want the name of the home button to be?")
    public static final Property<String> VAULT_NAME =
            newProperty(INFO_PATH + "vault-name", "&9Guild Vaults");

    @Comment("What do you want the lore of the vault button to be?")
    public static final Property<List<String >> VAULT_LORE =
            newListProperty(INFO_PATH + "vault-lore", "&7Click here to open your guild vaults!");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> VAULT_DISPLAY =
            newProperty(INFO_PATH + "vault-display", true);

    @Comment("What material do you want the motd button to be?")
    public static final Property<String> MOTD_MATERIAL =
            newProperty(INFO_PATH + "motd-material", "SIGN");

    @Comment("What do you want the name of the motd button to be?")
    public static final Property<String> MOTD_NAME =
            newProperty(INFO_PATH + "motd-name", "&6Guild MOTD");

    @Comment("What do you want the lore of the motd button to be?")
    public static final Property<List<String >> MOTD_LORE =
            newListProperty(INFO_PATH + "motd-lore", "{motd}");

    @Comment("Would you like to display this button?")
    public static final Property<Boolean> MOTD_DISPLAY =
            newProperty(INFO_PATH + "motd-display", true);




    private GuildInfoSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Welcome to the Guild Info GUI section of the config.",
                "Here you can modify the configuration of what the Guild Info GUI looks like.",
                "This can be used by any member of a Guild and shows key information of the Guild.",
                "You can see things like the members, the balance, tier, etc."
        };
        conf.setComment("guis.guild-info", pluginHeader);
    }
}
