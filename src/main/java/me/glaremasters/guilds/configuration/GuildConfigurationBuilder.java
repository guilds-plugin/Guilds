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
