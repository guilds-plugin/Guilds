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
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by Glare
 * Date: 5/7/2019
 * Time: 4:39 PM
 */
public class GuildVaultSettings implements SettingsHolder {

    @Comment({"What do you want the name of the Vault to be?",
    "Note: This requires a restart to change the inventory names."})
    public static final Property<String> VAULT_NAME =
            newProperty("guis.vault.name", "&8» &rGuild Vault");

    @Comment("What materials would you like to blacklist from being put into the vaults?")
    public static final Property<List<String>> BLACKLIST_MATERIALS =
            newListProperty("guis.vault.blacklist.materials", "");

    @Comment("What custom names of items would you like to blacklist from being put into the vaults?")
    public static final Property<List<String>> BLACKLIST_NAMES =
            newListProperty("guis.vault.blacklist.names", "");

    @Comment({"What custom lore do you want to blacklist from being put into the vaults?",
    "Please keep in mind this can be prove to false-positives so please let me know if you have issues.",
    "This will currently loop through your lore to check for any strings you have in the list to check.",
    "Improvements will be made over time. Thanks for your patience and suppport in advanced."})
    public static final Property<List<String>> BLACKLIST_LORES =
            newListProperty("guis.vault.blacklist.lores", "");

    private GuildVaultSettings() {

    }

}
