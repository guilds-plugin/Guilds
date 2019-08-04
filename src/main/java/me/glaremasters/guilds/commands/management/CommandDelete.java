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

package me.glaremasters.guilds.commands.management;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 11:12 PM
 */
@CommandAlias("%guilds")
public class CommandDelete extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ActionHandler actionHandler;
    @Dependency private Permission permission;
    @Dependency private SettingsManager settingsManager;

    /**
     * Delete your guild
     * @param player the player deleting the guild
     * @param guild the guild being deleted
     * @param role the role of the player
     */
    @Subcommand("delete")
    @Description("{@@descriptions.delete}")
    @CommandPermission(Constants.BASE_PERM + "delete")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isRemoveGuild())
            ACFUtil.sneaky(new InvalidPermissionException());

        getCurrentCommandIssuer().sendInfo(Messages.DELETE__WARNING);

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.PLAYER_DELETED);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return;

                guildHandler.removePermsFromAll(permission, guild, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC));

                guildHandler.removeAlliesOnDelete(guild);

                guildHandler.notifyAllies(guild, getCurrentCommandManager());

                guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__GUILDMASTER_LEFT,
                        "{player}", player.getName());

                ClaimUtils.deleteWithGuild(player, guild, settingsManager);

                guildHandler.removeGuild(guild);

                getCurrentCommandIssuer().sendInfo(Messages.DELETE__SUCCESSFUL,
                        "{guild}", guild.getName());

                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.DELETE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });


    }

}