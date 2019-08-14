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
    public void execute(CommandIssuer issuer) throws IOException {
        if (issuer.isPlayer()) {
            return;
        }

        guilds.getDatabase().getGuildAdapter().saveGuilds(handler.getGuilds());
    }

}
