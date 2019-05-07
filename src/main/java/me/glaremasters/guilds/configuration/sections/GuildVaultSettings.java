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

    @Comment("What do you want the name of the Vault to be?")
    public static final Property<String> VAULT_NAME =
            newProperty("guis.vault.name", "Guild Vault");

    @Comment("What materials would you like to blacklist from being put into the vaults?")
    public static final Property<List<String>> BLACKLIST_MATERIALS =
            newListProperty("guis.vault.blacklist.materials", "");

    @Comment("What custom names of items would you like to blacklist from being put into the vauts?")
    public static final Property<List<String>> BLACKLIST_NAMES =
            newListProperty("guis.vault.blacklist.names", "");

    private GuildVaultSettings() {

    }

}
