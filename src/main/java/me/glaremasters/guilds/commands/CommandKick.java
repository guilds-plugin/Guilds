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

package me.glaremasters.guilds.commands;

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
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 11:59 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandKick extends BaseCommand {

    /**
     * Kick a player from the guild
     * @param player the player executing the command
     * @param guild the guild the targetPlayer is being kicked from
     * @param role the role of the player
     * @param name the name of the targetPlayer
     */
    @Subcommand("boot|kick")
    @Description("Kick someone from your Guild")
    @CommandPermission(Constants.BASE_PERM + "boot")
    @CommandCompletion("@members")
    @Syntax("<name>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@members") @Single String name) {
        if (!role.isKick())
            ACFUtil.sneaky(new InvalidPermissionException());

        OfflinePlayer boot = Bukkit.getOfflinePlayer(name);

        if (boot == null)
            return;

        GuildMember kick = guild.getMember(boot.getUniqueId());

        if (kick == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD,
                    "{player}", name));

        if (guild.isMaster(boot))
            ACFUtil.sneaky(new InvalidPermissionException());

        guild.removeMember(kick);

        getCurrentCommandIssuer().sendInfo(Messages.BOOT__SUCCESSFUL,
                "{player}", boot.getName());

        guild.sendMessage(getCurrentCommandManager(), Messages.BOOT__PLAYER_KICKED,
                "{player}", boot.getName(),
                "{kicker}", player.getName());

        if (boot.isOnline())
            getCurrentCommandManager().getCommandIssuer(boot).sendInfo(Messages.BOOT__KICKED,
                    "{kicker}", player.getName());
    }

}