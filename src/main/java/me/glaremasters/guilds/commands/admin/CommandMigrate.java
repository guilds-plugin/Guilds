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
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
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
    public void execute(CommandIssuer issuer, String toBackend) throws IOException {
        if (issuer.isPlayer()) {
            return;
        }

        DatabaseBackend resolvedBackend = DatabaseBackend.getByBackendName(toBackend);
        if (resolvedBackend == null) {
            return; // TODO: send invalid backend message
        }


        try {
            DatabaseAdapter resolvedAdapter = guilds.getDatabase().cloneWith(resolvedBackend);
            if (!resolvedAdapter.isConnected()) {
                // TODO: send that backend failed to make connection (only occurs on SQL-based backends)
                return;
            }

            // Migrate whatever you need to migrate to new backend
            resolvedAdapter.getGuildAdapter().saveGuilds(handler.getGuilds());

            // TODO: send migration completed, now reboot with new backend settings
            //      or alternatively, could programmatically migrate the datasource.
            //      Like this:
            // DatabaseAdapter old = guilds.getDatabase();
            // old.close();
            // guilds.setDatabase(resolvedAdapter);
            // TODO: otherwise just close the resolvedAdapter (can use try-with-resources)
            // resolvedAdapter.close();
        } catch (IllegalArgumentException ex) {
            // TODO: send that requested backend matched current backend, request was aborted
        }
    }
}
