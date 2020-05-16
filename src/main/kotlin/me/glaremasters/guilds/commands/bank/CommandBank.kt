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

package me.glaremasters.guilds.commands.bank

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildDepositMoneyEvent
import me.glaremasters.guilds.api.events.GuildWithdrawMoneyEvent
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandBank : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var economy: Economy

    @Subcommand("bank balance")
    @Description("{@@descriptions.bank-balance}")
    @Syntax("")
    @CommandPermission(Constants.BANK_PERM + "balance")
    fun balance(player: Player, guild: Guild) {
        currentCommandIssuer.sendInfo(Messages.BANK__BALANCE, "{amount}", EconomyUtils.format(guild.balance))
    }

    @Subcommand("bank deposit")
    @Description("{@@descriptions.bank-deposit}")
    @CommandPermission(Constants.BANK_PERM + "deposit")
    @Syntax("<amount>")
    fun deposit(player: Player, @Conditions("perm:perm=DEPOSIT_MONEY") guild: Guild, amount: Double) {
        val balance = guild.balance
        val total = amount + balance
        val tier = guild.tier

        if (amount < 0) {
            throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
        }

        if (!EconomyUtils.hasEnough(currentCommandManager, economy, player, amount)) {
            throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
        }

        if (total > tier.maxBankBalance) {
            throw ExpectationNotMet(Messages.BANK__OVER_MAX)
        }

        val event = GuildDepositMoneyEvent(player, guild, amount)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        economy.withdrawPlayer(player, amount)
        guild.balance = total
        guild.sendMessage(currentCommandManager, Messages.BANK__DEPOSIT_SUCCESS, "{player}", player.name,
                "{amount}", EconomyUtils.format(amount), "{total}", EconomyUtils.format(guild.balance))
    }

    @Subcommand("bank withdraw")
    @Description("{@@descriptions.bank-withdraw}")
    @CommandPermission(Constants.BANK_PERM + "withdraw")
    @Syntax("<amount>")
    fun withdraw(player: Player, @Conditions("perm:perm=WITHDRAW_MONEY") guild: Guild, amount: Double) {
        val bal = guild.balance

        if (amount < 0) {
            throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
        }

        if (bal < amount) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        val event = GuildWithdrawMoneyEvent(player, guild, amount)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guild.balance = bal - amount
        economy.depositPlayer(player, amount)
        guild.sendMessage(currentCommandManager, Messages.BANK__WITHDRAWAL_SUCCESS, "{player}", player.name,
                "{amount}", EconomyUtils.format(amount), "{total}", EconomyUtils.format(guild.balance))
    }
}
