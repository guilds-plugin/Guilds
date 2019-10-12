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
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
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
 * Time: 8:59 PM
 */
@CommandAlias("%guilds")
public class CommandAdminAddPlayer extends BaseCommand {

   @Dependency private GuildHandler guildHandler;

    /**
     * Admin command to add a player to a guild
     * @param player the admin running the command
     * @param target the player being added to the guild
     * @param name the guild the player is being added to
     */
    @Subcommand("admin addplayer")
    @Description("{@@descriptions.admin-addplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@online @guilds")
    @Syntax("<player> <guild>")
    public void execute(Player player, @Values("@online") @Single String target, @Values("@guilds") @Single String name) {
        OfflinePlayer adding = Bukkit.getOfflinePlayer(target);

        if (adding == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND));
        }

        Guild possibleGuild = guildHandler.getGuild(adding);

        if (possibleGuild != null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));
        }

        Guild targetGuild = guildHandler.getGuild(name);

        if (targetGuild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }


        targetGuild.addMember(adding, guildHandler);

        if (adding.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(adding).sendInfo(Messages.ADMIN__PLAYER_ADDED,
                    "{guild}", targetGuild.getName());
        }

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED,
                "{player}", adding.getName(),
                "{guild}", targetGuild.getName());

        targetGuild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_ADD,
                "{player}", adding.getName());
    }

}