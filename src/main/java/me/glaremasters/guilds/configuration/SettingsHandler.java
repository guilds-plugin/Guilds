package me.glaremasters.guilds.configuration;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.migration.PlainMigrationService;
import me.glaremasters.guilds.Guilds;

import java.io.File;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 4:47 PM
 */
public class SettingsHandler {

    private final SettingsManager mainConf;
    private final SettingsManager tierConf;
    private final SettingsManager roleConf;
    private final SettingsManager buffConf;

    public SettingsHandler(Guilds guilds) {

        mainConf = SettingsManagerBuilder
                .withYamlFile(new File(guilds.getDataFolder(), "config.yml"))
                .migrationService(new GuildsMigrationService(guilds.getDataFolder()))
                .configurationData(GuildConfigurationBuilder.buildConfigurationData())
                .create();

        tierConf = SettingsManagerBuilder.withYamlFile(new File(guilds.getDataFolder(), "tiers.yml"))
                .migrationService(new PlainMigrationService())
                .configurationData(GuildConfigurationBuilder.buildTierData())
                .create();

        roleConf = SettingsManagerBuilder.withYamlFile(new File(guilds.getDataFolder(), "roles.yml"))
                .migrationService(new PlainMigrationService())
                .configurationData(GuildConfigurationBuilder.buildRoleData())
                .create();

        buffConf = SettingsManagerBuilder.withYamlFile(new File(guilds.getDataFolder(), "buffs.yml"))
                .migrationService(new BuffsMigrationService())
                .configurationData(GuildConfigurationBuilder.buildBuffData())
                .create();
    }

    public SettingsManager getMainConf() {
        return this.mainConf;
    }

    public SettingsManager getTierConf() {
        return tierConf;
    }

    public SettingsManager getRoleConf() {
        return roleConf;
    }

    public SettingsManager getBuffConf() {
        return buffConf;
    }
}
