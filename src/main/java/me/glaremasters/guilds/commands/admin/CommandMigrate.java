package me.glaremasters.guilds.commands.admin;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.database.providers.MySQLProvider;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.Constants;

import java.io.IOException;

@CommandAlias("%guilds")
public class CommandMigrate extends BaseCommand {

    @Dependency private Guilds guilds;
    @Dependency private SettingsManager settingsManager;
    @Dependency private GuildHandler handler;

    @Subcommand("admin migrate")
    @Description("{@@descriptions.admin-migrate}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            return;
        }

        String type = settingsManager.getProperty(StorageSettings.STORAGE_TYPE).toLowerCase();

        switch (type) {
            case "mysql":
                try {
                    new JsonProvider(guilds.getDataFolder()).saveGuilds(handler.getGuilds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "json":
                try {
                    new MySQLProvider(settingsManager).saveGuilds(handler.getGuilds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
