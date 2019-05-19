package me.glaremasters.guilds.configuration;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.migration.PlainMigrationService;
import lombok.Getter;
import me.glaremasters.guilds.Guilds;

import java.io.File;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 4:47 PM
 */
public class SettingsHandler {

    private Guilds guilds;
    @Getter private SettingsManager settingsManager;

    public SettingsHandler(Guilds guilds) {

        this.guilds = guilds;

        settingsManager = SettingsManagerBuilder
                .withYamlFile(new File(guilds.getDataFolder(), "config.yml"))
                .migrationService(new PlainMigrationService())
                .configurationData(GuildConfigurationBuilder.buildConfigurationData())
                .create();
    }

}
