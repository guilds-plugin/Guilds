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
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Created by Glare
 * Date: 4/8/2019
 * Time: 10:50 AM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandRequest extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private CooldownHandler cooldownHandler;

    /**
     * Request an invite
     *
     * @param player the player requesting
     * @param name   the name of the guild
     */
    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission(Constants.BASE_PERM + "request")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void execute(Player player, @Values("@guilds") @Single String name) {
        Guild guild = guildHandler.getGuild(player);

        if (guild != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));

        Guild target = guildHandler.getGuild(name);

        if (target == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        if (cooldownHandler.hasCooldown(Cooldown.TYPES.Request.name(), player.getUniqueId()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.REQUEST__COOLDOWN, "{time}",
                    String.valueOf(cooldownHandler.getRemaining(Cooldown.TYPES.Request.name(), player.getUniqueId()))));

        cooldownHandler.addCooldown(player, Cooldown.TYPES.Request.name(), 45, TimeUnit.SECONDS);

        guildHandler.pingOnlineInviters(target, getCurrentCommandManager(), player);


        getCurrentCommandIssuer().sendInfo(Messages.REQUEST__SUCCESS,
                "{guild}", target.getName());
    }

}
