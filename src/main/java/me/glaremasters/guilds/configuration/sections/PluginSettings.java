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
