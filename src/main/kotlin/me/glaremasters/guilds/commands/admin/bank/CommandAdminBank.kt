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
import me.glaremasters.guilds.exte.rounded
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
    @Syntax("%guild")
    @CommandCompletion("@guilds")
    fun balance(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_BALANCE, "{guild}", guild.name, "{balance}", EconomyUtils.format(guild.balance))
    }

    @Subcommand("admin bank deposit")
    @Description("{@@descriptions.admin-bank-deposit}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("%guild %amount")
    @CommandCompletion("@guilds")
    fun deposit(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Double) {
        if (!amount.isFinite() || amount <= 0) {
            throw ExpectationNotMet(Messages.SYNTAX__AMOUNT)
        }

        val rounded = amount.rounded()

        val total = guild.balance + rounded

        guild.balance = total
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_DEPOSIT, "{amount}", rounded.toString(), "{guild}", guild.name, "{total}", total.toString())
    }

    @Subcommand("admin bank withdraw")
    @Description("{@@descriptions.admin-bank-withdraw}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("%guild %amount")
    @CommandCompletion("@guilds")
    fun withdraw(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Double) {
        if (!amount.isFinite() || amount <= 0) {
            throw ExpectationNotMet(Messages.SYNTAX__AMOUNT)
        }

        val rounded = amount.rounded()

        val total = guild.balance - rounded

        if (total < 0) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        guild.balance = total
        currentCommandIssuer.sendInfo(Messages.ADMIN__BANK_WITHDRAW, "{amount}", rounded.toString(), "{guild}", guild.name, "{total}", total.toString())
    }
}
