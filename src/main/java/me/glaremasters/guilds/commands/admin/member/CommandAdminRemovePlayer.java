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

package me.glaremasters.guilds.commands.admin.member;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 9:10 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandAdminRemovePlayer extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Admin command to remove a player from a guild
     * @param player the admin running the command
     * @param target the guild the player is being removed from
     */
    @Subcommand("admin removeplayer")
    @Description("{@@descriptions.admin-removeplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<name>")
    public void execute(Player player, String target) {
        OfflinePlayer removing = Bukkit.getOfflinePlayer(target);

        if (removing == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND));

        Guild guild = guildHandler.getGuild(removing);

        if (guild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        guild.removeMember(removing);

        if (removing.isOnline())
            getCurrentCommandManager().getCommandIssuer(removing).sendInfo(Messages.ADMIN__PLAYER_REMOVED,
                    "{guild}", guild.getName());

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED,
                "{player}", removing.getName(),
                "{guild}", guild.getName());

        guild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_REMOVE,
                "{player}", removing.getName());
    }

}