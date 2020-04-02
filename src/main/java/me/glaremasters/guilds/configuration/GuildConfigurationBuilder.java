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

package me.glaremasters.guilds.configuration;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import me.glaremasters.guilds.conf.GuildBuffSettings;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.configuration.sections.CodeSettings;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.configuration.sections.GuildInfoMemberSettings;
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings;
import me.glaremasters.guilds.configuration.sections.GuildListSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.configuration.sections.RoleSettings;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.configuration.sections.TicketSettings;
import me.glaremasters.guilds.configuration.sections.TierSettings;
import me.glaremasters.guilds.configuration.sections.VaultPickerSettings;
import me.glaremasters.guilds.configuration.sections.WarSettings;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 12:45 PM
 */
public class GuildConfigurationBuilder {

    private GuildConfigurationBuilder() {
    }

    public static ConfigurationData buildConfigurationData() {
        return ConfigurationDataBuilder.createConfiguration(
                PluginSettings.class, StorageSettings.class, HooksSettings.class, GuildListSettings.class,
                VaultPickerSettings.class, GuildVaultSettings.class, GuildInfoSettings.class,
                GuildInfoMemberSettings.class, GuildSettings.class,
                WarSettings.class, CooldownSettings.class, CostSettings.class,
                ClaimSettings.class, TicketSettings.class, CodeSettings.class
        );
    }

    public static ConfigurationData buildTierData() {
        return ConfigurationDataBuilder.createConfiguration(TierSettings.class);
    }

    public static ConfigurationData buildRoleData() {
        return ConfigurationDataBuilder.createConfiguration(RoleSettings.class);
    }

    public static ConfigurationData buildBuffData() {
        return ConfigurationDataBuilder.createConfiguration(GuildBuffSettings.class);
    }


}
