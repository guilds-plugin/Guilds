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
import me.glaremasters.guilds.api.events.GuildWithdrawMoneyEvent;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:42 PM
 */
@CommandAlias("%guilds")
public class CommandBankWithdraw extends BaseCommand {

    @Dependency private Economy economy;

    /**
     * Take money from the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being taken out
     */
    @Subcommand("bank withdraw")
    @Description("{@@descriptions.bank-withdraw}")
    @CommandPermission(Constants.BANK_PERM + "withdraw")
    @Syntax("<amount>")
    public void execute(Player player, Guild guild, GuildRole role, double amount) {

        if (!role.isWithdrawMoney()) {
            ACFUtil.sneaky(new InvalidPermissionException());
        }

        double balance = guild.getBalance();

        if (amount < 0) {
            return;
        }

        if (balance < amount) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK));
        }


        GuildWithdrawMoneyEvent event = new GuildWithdrawMoneyEvent(player, guild, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        guild.setBalance(balance - amount);
        economy.depositPlayer(player, amount);
        guild.sendMessage(getCurrentCommandManager(), Messages.BANK__WITHDRAWAL_SUCCESS, "{player}", player.getName(),
                "{amount}", String.valueOf(NumberFormat.getInstance().format(amount)),
                "{total}", String.valueOf(NumberFormat.getInstance().format(guild.getBalance())));
    }
}