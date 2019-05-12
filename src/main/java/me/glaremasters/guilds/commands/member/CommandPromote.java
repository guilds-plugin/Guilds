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

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
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
 * Time: 11:30 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandPromote extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * Promote a player in the guild
     * @param player the player executing the command
     * @param target the player being promoted yay
     * @param guild the guild which's member is being promoted
     * @param role the role of the player promoting
     */
    @Subcommand("promote")
    @Description("{@@descriptions.promote}")
    @CommandPermission(Constants.BASE_PERM + "promote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@members") @Single String target) {
        if (!role.isPromote())
            ACFUtil.sneaky(new InvalidPermissionException());

        OfflinePlayer user = PlayerUtils.getPlayer(target);

        if (user == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND,
                    "{player}", target));

        if (user.getName().equals(player.getName()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.PROMOTE__CANT_PROMOTE));

        if ((!RoleUtils.inGuild(guild, user)) && !RoleUtils.checkPromote(guild, user, player))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD,
                    "{player}", target));

        if (RoleUtils.isOfficer(guild, user))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.PROMOTE__CANT_PROMOTE));

        RoleUtils.promote(guildHandler, guild, user);

        String oldRole = RoleUtils.getPrePromotedRoleName(guildHandler, guild.getMember(user.getUniqueId()));
        String newRole = RoleUtils.getCurrentRoleName(guild.getMember(user.getUniqueId()));

        getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__PROMOTE_SUCCESSFUL,
                "{player}", target,
                "{old}", oldRole,
                "{new}", newRole);

        if (user.isOnline())
            getCurrentCommandManager().getCommandIssuer(user).sendInfo(Messages.PROMOTE__YOU_WERE_PROMOTED,
                    "{old}", oldRole,
                    "{new}", newRole);


    }

}