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

package me.glaremasters.guilds.commands.admin.bank;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;

/**
 * Created by Glare
 * Date: 5/30/2019
 * Time: 3:36 PM
 */
@CommandAlias("%guilds")
public class CommandAdminBankWithdraw extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Admin command to remove money from a guild vault
     * @param issuer the person running the command
     * @param name the name of the guild
     * @param amount how much to remove
     */
    @Subcommand("admin bank withdraw")
    @Description("{@@descriptions.admin-bank-withdraw}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<amount>")
    @CommandCompletion("@guilds")
    public void execute(CommandIssuer issuer, @Single String name, double amount) {
        Guild guild = guildHandler.getGuild(name);

        if (guild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        if (amount < 0)
            return;

        double total = guild.getBalance() - amount;

        if (guild.getBalance() < amount)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK));

        guild.setBalance(total);

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__BANK_WITHDRAW, "{amount}", String.valueOf(amount),
                "{guild}", guild.getName(),
                "{total}", String.valueOf(total));
    }

}