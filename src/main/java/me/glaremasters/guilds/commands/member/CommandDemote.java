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
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.PlayerUtils;
import me.glaremasters.guilds.utils.RoleUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 11:31 PM
 */
@CommandAlias("%guilds")
public class CommandDemote extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Demote a player in a guild
     * @param player the person running the command
     * @param target the player you want to demote
     * @param guild check player is in a guild
     * @param role check player can demote another player
     */
    @Subcommand("demote")
    @Description("{@@descriptions.demote}")
    @CommandPermission(Constants.BASE_PERM + "demote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@members") @Single String target) {
        if (!role.isDemote())
            ACFUtil.sneaky(new InvalidPermissionException());

        OfflinePlayer user = PlayerUtils.getPlayer(target);

        if (user == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND,
                    "{player}", target));

        if (user.getName().equals(player.getName()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE));

        if (!RoleUtils.inGuild(guild, user))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD,
                    "{player}", target));

        if (RoleUtils.sameRole(guild, player, user))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE));

        if (RoleUtils.isLowest(guildHandler, guild.getMember(user.getUniqueId())) ||
                (RoleUtils.isLower(guild.getMember(user.getUniqueId()), guild.getMember(player.getUniqueId()))))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.DEMOTE__CANT_DEMOTE));

        RoleUtils.demote(guildHandler, guild, user);

        String oldRole = RoleUtils.getPreDemotedRoleName(guildHandler, guild.getMember(user.getUniqueId()));
        String newRole = RoleUtils.getCurrentRoleName(guild.getMember(user.getUniqueId()));

        getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__DEMOTE_SUCCESSFUL,
                "{player}", target,
                "{old}", oldRole,
                "{new}", newRole);

        if (user.isOnline())
            getCurrentCommandManager().getCommandIssuer(user).sendInfo(Messages.DEMOTE__YOU_WERE_DEMOTED,
                    "{old}", oldRole,
                    "{new}", newRole);


    }

}