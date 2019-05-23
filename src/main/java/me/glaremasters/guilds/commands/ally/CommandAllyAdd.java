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

package me.glaremasters.guilds.commands.ally;

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
import me.glaremasters.guilds.api.events.GuildAddAllyEvent;
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
 * Date: 4/4/2019
 * Time: 7:13 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandAllyAdd extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Send a guild ally request
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild the request is being sent to
     */
    @Subcommand("ally add")
    @Description("{@@descriptions.ally-add}")
    @CommandPermission(Constants.ALLY_PERM + "add")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@guilds") @Single String name) {
        if (!role.isAddAlly())
            ACFUtil.sneaky(new InvalidPermissionException());

        Guild target = guildHandler.getGuild(name);

        if (target == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        if (guild.isAllyPending(target))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ALLY__ALREADY_REQUESTED));

        if (guildHandler.isAlly(guild, target))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ALLY__ALREADY_ALLY));

        if (guild.getId().equals(target.getId()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ALLY__SAME_GUILD));

        GuildAddAllyEvent event = new GuildAddAllyEvent(player, guild, target);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        getCurrentCommandIssuer().sendInfo(Messages.ALLY__INVITE_SENT,
                "{guild}", target.getName());

        target.sendMessage(getCurrentCommandManager(), Messages.ALLY__INCOMING_INVITE,
                "{guild}", guild.getName());

        target.addPendingAlly(guild);
    }


}