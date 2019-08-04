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

package me.glaremasters.guilds.commands.bank;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.api.events.GuildDepositMoneyEvent;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.EconomyUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:32 PM
 */
@CommandAlias("%guilds")
public class CommandBankDeposit extends BaseCommand {

    @Dependency private Economy economy;

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
    public void execute(Player player, Guild guild, GuildRole role, double amount) {

        double balance = guild.getBalance();
        double total = amount + balance;
        GuildTier tier = guild.getTier();

        if (!role.isDepositMoney())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (amount < 0)
            return;

        if (!EconomyUtils.hasEnough(getCurrentCommandManager(), economy, player, amount))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY));

        if (total > tier.getMaxBankBalance())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.BANK__OVER_MAX));

        GuildDepositMoneyEvent event = new GuildDepositMoneyEvent(player, guild, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        economy.withdrawPlayer(player, amount);
        guild.setBalance(total);
        guild.sendMessage(getCurrentCommandManager(), Messages.BANK__DEPOSIT_SUCCESS, "{player}", player.getName(),
                "{amount}", String.valueOf(NumberFormat.getInstance().format(amount)),
                        "{total}", String.valueOf(NumberFormat.getInstance().format(guild.getBalance())));
    }

}