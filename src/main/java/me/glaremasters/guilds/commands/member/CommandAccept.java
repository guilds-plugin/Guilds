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
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 10:29 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandAccept extends BaseCommand {

    private GuildHandler guildHandler;
    private Permission permission;

    /**
     * Accept a guild invite
     * @param player the player accepting the invite
     * @param name the name of the guild being accepted
     */
    @Subcommand("accept|join")
    @Description("{@@descriptions.accept}")
    @CommandPermission(Constants.BASE_PERM + "accept")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void execute(Player player, @Values("@invitedTo") @Single String name) {
        if (guildHandler.getGuild(player) != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));

        Guild guild = guildHandler.getGuild(name);

        if (guild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        if (!guild.checkIfInvited(player) && guild.isPrivate())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ACCEPT__NOT_INVITED));

        if (guildHandler.checkIfFull(guild))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ACCEPT__GUILD_FULL));

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        guild.addMember(player, guildHandler);

        guildHandler.addPerms(permission, player);

        guild.sendMessage(getCurrentCommandManager(), Messages.ACCEPT__PLAYER_JOINED,
                "{player}", player.getName());

        getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__SUCCESSFUL,
                "{guild}", guild.getName());
    }

}