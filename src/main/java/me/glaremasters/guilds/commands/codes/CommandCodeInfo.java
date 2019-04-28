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
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:23 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandCodeInfo extends BaseCommand {

    /**
     * THis command will display info about a guild's codes
     * @param player the player running the command
     * @param guild the guild they are in
     * @param role the role of the player
     * @param code the code they are requesting information about
     */
    @Subcommand("code info")
    @Description("{@@descriptions.code-info}")
    @CommandPermission(Constants.CODE_PERM + "info")
    @Syntax("<code>")
    @CommandCompletion("@activeCodes")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@activeCodes") @Single String code) {

        if (!role.isSeeCodeRedeemers())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (code == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CODES__INVALID_CODE));

        GuildCode gc = guild.getCode(code);

        getCurrentCommandIssuer().sendInfo(Messages.CODES__INFO, "{code}", gc.getId(), "{amount}", String.valueOf(gc.getUses()), "{creator}", Bukkit.getOfflinePlayer(gc.getCreator()).getName(), "{redeemers}", guild.getRedeemers(code));

    }

}