package me.glaremasters.guilds.commands.console;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.BackupUtils;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;

import java.io.IOException;

@CommandAlias("%guilds")
public class CommandBackup extends BaseCommand {

    @Dependency private ActionHandler actionHandler;
    @Dependency private Guilds guilds;

    @Subcommand("console backup")
    @Description("{@@descriptions.console-backup}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.BACKUP__WARNING);

        actionHandler.addAction(issuer.getIssuer(), new ConfirmAction() {
            @Override
            public void accept() {
                guilds.getCommandManager().getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.BACKUP__STARTED);
                Guilds.newChain().async(() -> {
                    try {
                        BackupUtils.zipDir("guilds-backup-" + System.currentTimeMillis() + ".zip", guilds.getDataFolder().getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).sync(() -> guilds.getCommandManager().getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.BACKUP__FINISHED)).execute();
                actionHandler.removeAction(issuer.getIssuer());
            }

            @Override
            public void decline() {
                actionHandler.removeAction(issuer.getIssuer());
                getCurrentCommandIssuer().sendInfo(Messages.BACKUP__CANCELLED);
            }
        });
    }

}
