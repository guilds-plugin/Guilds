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

package me.glaremasters.guilds.commands.codes;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:22 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandCodeRedeem extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * Redeem an invite code to join a guild
     * @param player the player redeeming the code
     * @param code the code being redeemed
     */
    @Subcommand("code redeem")
    @Description("{@@descriptions.code-redeem}")
    @CommandPermission(Constants.CODE_PERM + "redeem")
    public void execute(Player player, String code) {

        if (guildHandler.getGuild(player) != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));

        Guild guild = guildHandler.getGuildByCode(code);

        if (guild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CODES__INVALID_CODE));

        if (guildHandler.checkIfFull(guild))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ACCEPT__GUILD_FULL));

        guildHandler.handleInvite(getCurrentCommandManager(), player, guild, guild.getCode(code));
    }

}