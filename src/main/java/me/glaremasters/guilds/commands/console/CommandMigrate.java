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
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
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
    @Dependency private ArenaHandler arenaHandler;
    @Dependency private CooldownHandler cooldownHandler;
    @Dependency private ChallengeHandler challengeHandler;

    @Subcommand("console migrate")
    @Description("{@@descriptions.console-migrate}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@sources")
    public void execute(CommandIssuer issuer, @Values("@sources") String toBackend) throws IOException {
        if (issuer.isPlayer()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__CONSOLE_COMMAND);
        }

        getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__WARNING);
        actionHandler.addAction(issuer.getIssuer(), new ConfirmAction() {
            @Override
            public void accept() {

                DatabaseBackend resolvedBacked = DatabaseBackend.getByBackendName(toBackend);
                if (resolvedBacked == null) {
                    ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__INVALID_BACKEND));
                }

                Guilds.newChain().async(() -> {
                    try {
                        guildHandler.setMigrating(true);
                        DatabaseAdapter resolvedAdapter = guilds.getDatabase().cloneWith(resolvedBacked);
                        if (!resolvedAdapter.isConnected()) {
                            guildHandler.setMigrating(false);
                            ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__CONNECTION_FAILED));
                        }

                        resolvedAdapter.getGuildAdapter().saveGuilds(guildHandler.getGuilds());
                        resolvedAdapter.getArenaAdapter().saveArenas(arenaHandler.getArenas());
                        resolvedAdapter.getCooldownAdapter().saveCooldowns(cooldownHandler.getCooldowns().values());
                        resolvedAdapter.getChallengeAdapter().saveChallenges(challengeHandler.getChallenges());

                        DatabaseAdapter old = guilds.getDatabase();
                        guilds.setDatabase(resolvedAdapter);
                        old.close();

                        guildHandler.setMigrating(false);

                    } catch (IllegalArgumentException ex) {
                        guildHandler.setMigrating(false);
                        ACFUtil.sneaky(new ExpectationNotMet(Messages.MIGRATE__SAME_BACKEND));
                    } catch (IOException e) {
                        guildHandler.setMigrating(false);
                        e.printStackTrace();
                    }
                }).sync(() -> {
                    guilds.getCommandManager().getCommandIssuer(issuer.getIssuer()).sendInfo(Messages.MIGRATE__COMPLETE, "{amount}", String.valueOf(guildHandler.getGuildsSize()));
                    actionHandler.removeAction(issuer.getIssuer());
                }).execute();
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__CANCELLED);
                actionHandler.removeAction(issuer.getIssuer());
            }
        });
    }
}
