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

package me.glaremasters.guilds.commands.member;

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
import me.glaremasters.guilds.api.events.GuildInviteEvent;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 10:15 PM
 */
@CommandAlias("%guilds")
public class CommandInvite extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Invite player to guild
     * @param player current player
     * @param target player being invited
     * @param guild the guild that the targetPlayer is being invited to
     * @param role the role of the player
     */
    @Subcommand("invite")
    @Description("{@@descriptions.invite}")
    @CommandPermission(Constants.BASE_PERM + "invite")
    @CommandCompletion("@online")
    @Syntax("<name>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@online") @Single String target) {
        if (!role.isInvite()) {
            ACFUtil.sneaky(new InvalidPermissionException());
        }

        Player pl = Bukkit.getPlayer(target);

        if (pl == null || !pl.isOnline()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", target));
        }

        if (guildHandler.getGuild(pl) != null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.INVITE__ALREADY_IN_GUILD, "{player}", target));
        }

        if (guild.checkIfInvited(pl)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.INVITE__ALREADY_INVITED));
        }

        GuildInviteEvent event =  new GuildInviteEvent(player, guild, pl);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        guild.inviteMember(pl.getUniqueId());

        getCurrentCommandManager().getCommandIssuer(pl).sendInfo(Messages.INVITE__MESSAGE,
                        "{player}", player.getName(),
                "{guild}", guild.getName());

        getCurrentCommandIssuer().sendInfo(Messages.INVITE__SUCCESSFUL,
                "{player}", pl.getName());



    }

}