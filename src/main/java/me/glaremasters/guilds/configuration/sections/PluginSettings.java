/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public final class PluginSettings implements SettingsHolder {

    @Comment({"This is used for the Guild's Announcement System, which allow me (The Author) to communicate to you guys without updating.",
            "The way this works is very simple. If you have \"console\" set to \"true\", you will see the announcement when the server starts.",
            "If you have \"in-game\" set to \"true\", your OPed players will see it the first time they login to the server."
    })
    public static final Property<Boolean> ANNOUNCEMENTS_CONSOLE =
            newProperty("settings.announcements.console", true);

    public static final Property<Boolean> ANNOUNCEMENTS_IN_GAME =
            newProperty("settings.announcements.in-game", true);

    @Comment({"Choosing your language for the plugin couldn't be easier! The default language is english.",
            "If you speak another language but don't see it here, feel free to submit it via one of the links above to have it added to the plugin.",
            "If you try and use a different language than any in the list above, the plugin will not function in a normal manner.",
            "As you can see this is currently en-US, and there is a en-US.yml file in the language folder.",
            "If I wanted to switch to french, I would use fr-FR as the language instead."
    })
    public static final Property<String> MESSAGES_LANGUAGE =
            newProperty("settings.messagesLanguage", "en-US");

    @Comment("Would you like to check for plugin updates on startup? It's highly suggested you keep this enabled!")
    public static final Property<Boolean> UPDATE_CHECK =
            newProperty("settings.update-check", true);

    @Comment({"What would you like the command aliases for the plugin to be?",
            "You can have as many as your want, just separate each with | and NO SPACES."})
    public static final Property<String> PLUGIN_ALIASES =
            newProperty("settings.plugin-aliases", "guild|guilds|g");

    @Comment({"Would you like to run vault permission changes async? (Will be less stress on the main thread and prevent lag)",
            "Async is used by LuckPerms.",
            "Set this to false if you are using PEx.",
            "I do suggest you switch to LuckPerms so that you can keep it async, but ultimately the choice is yours."})
    public static final Property<Boolean> RUN_VAULT_ASYNC =
            newProperty("settings.run-vault-async", true);

    private PluginSettings() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] pluginHeader = {
                "Guilds",
                "Creator: Glare",
                "Contributors: https://github.com/guilds-plugin/Guilds/graphs/contributors",
                "Issues: https://github.com/guilds-plugin/Guilds/issues",
                "Spigot: https://www.spigotmc.org/resources/66176/",
                "Wiki: https://wiki.glaremasters.me/",
                "Discord: https://glaremasters.me/discord"
        };
        conf.setComment("settings", pluginHeader);
    }
}
