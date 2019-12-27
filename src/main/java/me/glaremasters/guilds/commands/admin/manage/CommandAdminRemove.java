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

package me.glaremasters.guilds.commands.admin.manage;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 8:53 PM
 */
@CommandAlias("%guilds")
public class CommandAdminRemove extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ActionHandler actionHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Admin command to remove a guild from the server
     * @param player the admin running the command
     * @param guild the name of the guild being removed
     */
    @Subcommand("admin remove")
    @Description("{@@descriptions.admin-remove}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void execute(Player player, @Flags("admin") @Values("@guilds") Guild guild) {

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        if (guildHandler.isMigrating()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__MIGRATING));
        }

        getCurrentCommandIssuer().sendInfo(Messages.DELETE__WARNING,
                "{guild}", guild.getName());

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.ADMIN_DELETED);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }

                ClaimUtils.deleteWithGuild(guild, settingsManager);

                guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__GUILDMASTER_LEFT,
                        "{player}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName());

                guildHandler.removeGuild(guild);
                getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_SUCCESS,
                        "{guild}", guild.getName());
                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                actionHandler.removeAction(player);
            }
        });

    }

}