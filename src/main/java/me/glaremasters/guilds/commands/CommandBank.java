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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:43 PM
 */
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandBank extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * Check the bank balance of a Guild
     * @param player the player to check
     * @param guild the guild they are in
     */
    @Subcommand("bank balance")
    @Description("{@@descriptions.bank-balance}")
    @CommandPermission("guilds.command.bank")
    public void onBalance(Player player, Guild guild) {
        getCurrentCommandIssuer().sendInfo(Messages.BANK__BALANCE, "{amount}", String.valueOf(guild.getBalance()));
    }

    /**
     * Deposit money into the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being put into the bank
     */
    @Subcommand("bank deposit")
    @Description("{@@descriptions.bank-deposit}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onDeposit(Player player, Guild guild, GuildRole role, Double amount) {
        if (!role.canDepositMoney()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (amount < 0) return;

        if (guilds.getEconomy().getBalance(player) < amount) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
            return;
        }
        guilds.getEconomy().withdrawPlayer(player, amount);
        Double balance = guild.getBalance();
        guild.setBalance(balance + amount);
        getCurrentCommandIssuer().sendInfo(Messages.BANK__DEPOSIT_SUCCESS, "{amount}", String.valueOf(amount), "{total}", String.valueOf(guild.getBalance()));
    }

    /**
     * Take money from the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being taken out
     */
    @Subcommand("bank withdraw")
    @Description("{@@descriptions.bank-withdraw}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onWithdraw(Player player, Guild guild, GuildRole role, Double amount) {
        if (!role.canWithdrawMoney()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (amount < 0) return;

        Double balance = guild.getBalance();
        if ((balance < amount)) {
            getCurrentCommandIssuer().sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
            return;
        }
        guild.setBalance(balance - amount);
        getCurrentCommandIssuer().sendInfo(Messages.BANK__WITHDRAWL_SUCCESS, "{amount}", String.valueOf(amount), "{total}", String.valueOf(guild.getBalance()));
    }

}
