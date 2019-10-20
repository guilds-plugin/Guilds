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

@CommandAlias("%guilds")
public class CommandBackup extends BaseCommand {

    @Dependency private ActionHandler actionHandler;
    @Dependency private Guilds guilds;

    @Subcommand("console backup")
    @Description("{@@descriptions.console-backup}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__CONSOLE_COMMAND);
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
