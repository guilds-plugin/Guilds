package me.glaremasters.guilds.configuration;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import me.glaremasters.guilds.conf.GuildBuffSettings;
import me.glaremasters.guilds.configuration.sections.*;

/**
 * A builder class that provides static methods for building different types of guild configuration data.
 */
public class GuildConfigurationBuilder {

    /**
     * Private constructor to prevent instantiation.
     */
    private GuildConfigurationBuilder() {
    }

    /**
     * Builds the main guild configuration data, including settings for various features.
     *
     * @return A ConfigurationData object containing the main guild configuration settings.
     */
    public static ConfigurationData buildConfigurationData() {
        return ConfigurationDataBuilder.createConfiguration(
                PluginSettings.class, ExperimentalSettings.class, StorageSettings.class, HooksSettings.class, GuildListSettings.class,
                VaultPickerSettings.class, GuildVaultSettings.class, GuildInfoSettings.class,
                GuildInfoMemberSettings.class, GuildSettings.class,
                WarSettings.class, CooldownSettings.class, CostSettings.class,
                ClaimSettings.class, TicketSettings.class, CodeSettings.class
        );
    }

    /**
     * Builds the guild configuration data for tiers.
     *
     * @return A ConfigurationData object containing the guild tier configuration settings.
     */
    public static ConfigurationData buildTierData() {
        return ConfigurationDataBuilder.createConfiguration(TierSettings.class);
    }

    /**
     * Builds the guild configuration data for roles.
     *
     * @return A ConfigurationData object containing the guild role configuration settings.
     */
    public static ConfigurationData buildRoleData() {
        return ConfigurationDataBuilder.createConfiguration(RoleSettings.class);
    }

    /**
     * Builds the guild configuration data for buffs.
     *
     * @return A ConfigurationData object containing the guild buff configuration settings.
     */
    public static ConfigurationData buildBuffData() {
        return ConfigurationDataBuilder.createConfiguration(GuildBuffSettings.class);
    }
}
