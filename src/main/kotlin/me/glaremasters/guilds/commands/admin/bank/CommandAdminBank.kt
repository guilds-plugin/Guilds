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

package me.glaremasters.guilds.commands.admin.bank

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils

@CommandAlias("%guilds")
internal class CommandAdminBank : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin bank balance")
    @Description("{@@descriptions.admin-bank-balance}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<%syntax>")
    @CommandCompletion("@guilds")
    fun balance(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_BALANCE, "{guild}", guild.name, "{balance}", EconomyUtils.format(guild.balance))
    }

    @Subcommand("admin bank deposit")
    @Description("{@@descriptions.admin-bank-deposit}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<%syntax> <amount>")
    @CommandCompletion("@guilds")
    fun deposit(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Double) {
        if (amount < 0) {
            return
        }

        val total = guild.balance + amount

        guild.balance = total
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_DEPOSIT, "{amount}", amount.toString(), "{guild}", guild.name, "{total}", total.toString())
    }

    @Subcommand("admin bank withdraw")
    @Description("{@@descriptions.admin-bank-withdraw}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<%syntax> <amount>")
    @CommandCompletion("@guilds")
    fun withdraw(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Double) {
        if (amount < 0) {
            return
        }

        val total = guild.balance - amount

        if (guild.balance < total) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        guild.balance = total
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_WITHDRAW, "{amount}", amount.toString(), "{guild}", guild.name, "{total}", total.toString())
    }
}
