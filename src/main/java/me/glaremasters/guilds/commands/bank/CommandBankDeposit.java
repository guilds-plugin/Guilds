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

package me.glaremasters.guilds.commands.bank;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:32 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandBankDeposit extends BaseCommand {

    private Economy economy;

    /**
     * Deposit money into the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being put into the bank
     */
    @Subcommand("bank deposit")
    @Description("{@@descriptions.bank-deposit}")
    @CommandPermission(Constants.BANK_PERM + "deposit")
    @Syntax("<amount>")
    public void onDeposit(Player player, Guild guild, GuildRole role, double amount) {

        double balance = guild.getBalance();
        double total = amount + balance;
        GuildTier tier = guild.getTier();

        if (!role.isDepositMoney())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (amount < 0)
            return;

        if (economy.getBalance(player) < amount) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
            return;
        }

        if (economy.getBalance(player) < amount)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY));

        if (total > tier.getMaxBankBalance())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.BANK__OVER_MAX));

        economy.withdrawPlayer(player, amount);
        guild.setBalance(total);
        getCurrentCommandIssuer().sendInfo(Messages.BANK__DEPOSIT_SUCCESS,
                "{amount}", String.valueOf(amount),
                "{total}", String.valueOf(guild.getBalance()));


    }

}