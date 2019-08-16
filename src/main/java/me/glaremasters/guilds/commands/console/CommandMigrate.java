package me.glaremasters.guilds.commands.console;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;

import java.io.IOException;

@CommandAlias("%guilds")
public class CommandMigrate extends BaseCommand {

    @Dependency private Guilds guilds;
    @Dependency private GuildHandler guildHandler;
    @Dependency private ActionHandler actionHandler;

    @Subcommand("console migrate")
    @Description("{@@descriptions.console-migrate}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@sources")
    public void execute(CommandIssuer issuer, @Values("@sources") String toBackend) throws IOException {
        if (issuer.isPlayer()) {
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__WARNING);
        actionHandler.addAction(issuer.getIssuer(), new ConfirmAction() {
            @Override
            public void accept() {

                DatabaseBackend resolvedBacked = DatabaseBackend.getByBackendName(toBackend);
                if (resolvedBacked == null) {
                    ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__INVALID_BACKEND));
                }

                try {
                    DatabaseAdapter resolvedAdapter = guilds.getDatabase().cloneWith(resolvedBacked);
                    if (!resolvedAdapter.isConnected()) {
                        ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__CONNECTION_FAILED));
                    }

                    resolvedAdapter.getGuildAdapter().saveGuilds(guildHandler.getGuilds());

                    DatabaseAdapter old = guilds.getDatabase();
                    guilds.setDatabase(resolvedAdapter);
                    old.close();

                    getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__COMPLETE);
                } catch (IllegalArgumentException ex) {
                    ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__SAME_BACKEND));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                actionHandler.removeAction(issuer.getIssuer());
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__CANCELLED);
                actionHandler.removeAction(issuer.getIssuer());
            }
        });
    }
}
