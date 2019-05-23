/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
 * Date: 5/7/2019
 * Time: 4:39 PM
 */
public class VaultPickerSettings implements SettingsHolder {

    @Comment({"What do you want the name of the gui to be?",
    "Currently supports {name} for the name of the guild."})
    public static final Property<String> GUI_NAME =
            newProperty("guis.vault-picker.name", "&8» &r{name}'s Vaults");

    @Comment({"How many rows would you like to display?"})
    public static final Property<Integer> GUI_SIZE =
            newProperty("guis.vault-picker.rows",1);

    @Comment("What do you want the material of the vaults to be?")
    public static final Property<String> PICKER_MATERIAL =
            newProperty("guis.vault-picker.item-material", "CHEST");

    @Comment({"WHat do you want the name of the vault to be?",
    "I recommend keeping this blank so that we can put the vault number in the lore."})
    public static final Property<String> PICKER_NAME =
            newProperty("guis.vault-picker.item-name", " ");

    public static final Property<List<String>> PICKER_LORE =
            newListProperty("guis.vault-picker.item-lore", "&8• &7Vault &9#{number}", "&8• &7Status: {status}", "");

    @Comment("What do you want to show when a vault is unlocked?")
    public static final Property<String> PICKER_UNLOCKED =
            newProperty("guis.vault-picker.unlocked", "&9Unlocked");

    @Comment("What do you want to show when a vault is locked?")
    public static final Property<String> PICKER_LOCKED =
            newProperty("guis.vault-picker.locked", "&c&mLocked&r");



    private VaultPickerSettings() {

    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Here you can control what the GUI looks like that allows players to choose which vault to open",
                "You can do things like set the name of the gui, the material to use, material name, and lore!"
        };
        conf.setComment("guis.vault-picker", pluginHeader);
    }

}
