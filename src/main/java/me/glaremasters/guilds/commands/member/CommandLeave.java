/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.commands.member;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 11:25 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandLeave extends BaseCommand {

    private GuildHandler guildHandler;
    private ActionHandler actionHandler;

    /**
     * Leave a guild
     * @param player the player leaving the guild
     * @param guild the guild being left
     */
    @Subcommand("leave|exit")
    @Description("{@@descriptions.leave}")
    @CommandPermission(Constants.BASE_PERM + "leave")
    public void execute(Player player, Guild guild) {
        if (guild.isMaster(player))
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING_GUILDMASTER);
        else
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING);

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildLeaveEvent event = new GuildLeaveEvent(player, guild);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return;

                if (guild.isMaster(player)) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.MASTER_LEFT);
                    Bukkit.getPluginManager().callEvent(removeEvent);

                    if (removeEvent.isCancelled())
                        return;

                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__GUILDMASTER_LEFT,
                            "{player}", player.getName());

                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                    guildHandler.removeGuild(guild);

                } else {
                    guild.removeMember(player);

                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__PLAYER_LEFT,
                            "{player}", player.getName());
                }

                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });
    }

}