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
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;

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
     * @param issuer the admin running the command
     * @param guild  the guild the player is being added to
     * @param targetPlayer the player being added to a guild
     */
    @Subcommand("admin addplayer")
    @Description("{@@descriptions.admin-addplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds @online")
    @Syntax("<%syntax> <player>")
    public void execute(CommandIssuer issuer, @Flags("admin") @Values("@guilds") Guild guild, String targetPlayer) {
        OfflinePlayer adding = PlayerUtils.getPlayer(targetPlayer);

        if (adding == null || !adding.hasPlayedBefore()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NO_EXIST));
        }

        if (guildHandler.getGuild(adding) != null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));
        }

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        guild.addMember(adding, guildHandler);

        if (adding.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(adding).sendInfo(Messages.ADMIN__PLAYER_ADDED,
                    "{guild}", guild.getName());
        }

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED,
                "{player}", adding.getName(),
                "{guild}", guild.getName());

        guild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_ADD,
                "{player}", adding.getName());
    }

}