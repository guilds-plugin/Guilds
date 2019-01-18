package me.glaremasters.guilds.configuration;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 12:45 PM
 */
public class GuildsSettingsRetriever {

    private GuildsSettingsRetriever() {
    }

    public static ConfigurationData buildConfigurationData() {
        return ConfigurationDataBuilder.createConfiguration(
                PluginSettings.class, HooksSettings.class, GuiSettings.class,
                GuildSettings.class, TimerSettings.class, CostSettings.class,
                ClaimSettings.class, TablistSettings.class, TierSettings.class,
                RoleSettings.class
        );
    }

}
