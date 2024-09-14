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
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.api.events.GuildDepositMoneyEvent
import me.glaremasters.guilds.api.events.GuildWithdrawMoneyEvent
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.configuration.sections.TierSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildRolePerm
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit

@CommandAlias("%guilds")
internal class CommandAdminSimulate : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var actionHandler: ActionHandler
    @Dependency lateinit var permission: Permission
    @Dependency lateinit var economy: Economy

    @Subcommand("admin simulate bank deposit")
    @Description("{@@descriptions.admin-simulate-bank-deposit}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<player> <amount>")
    @CommandCompletion("@online")
    fun deposit(issuer: CommandIssuer, @Values("@online") onlinePlayer: OnlinePlayer, amount: Double) {

        val player = onlinePlayer.player
        val guild = guildHandler.getGuild(player)

        if (guild == null) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD)
            return
        }

        val balance = guild.balance
        val total = amount + balance
        val tier = guild.tier

        if (!guild.getMember(player.uniqueId).role.hasPerm(GuildRolePerm.DEPOSIT_MONEY)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__ROLE_NO_PERMISSION)
            return
        }

        if (amount < 0) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY)
            return
        }

        if (!EconomyUtils.hasEnough(currentCommandManager, economy, player, amount)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY)
            return
        }

        if (total > tier.maxBankBalance) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.BANK__OVER_MAX)
            return
        }

        val event = GuildDepositMoneyEvent(player, guild, amount)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        economy.withdrawPlayer(player.player, amount)
        guild.balance = total
        guild.sendMessage(currentCommandManager, Messages.BANK__DEPOSIT_SUCCESS, "{player}", player.name,
            "{amount}", EconomyUtils.format(amount), "{total}", EconomyUtils.format(guild.balance))
    }

    @Subcommand("admin simulate bank withdraw")
    @Description("{@@descriptions.admin-simulate-bank-withdraw}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("<player> <amount>")
    @CommandCompletion("@online")
    fun withdraw(issuer: CommandIssuer, @Values("@online") onlinePlayer: OnlinePlayer, amount: Double) {

        val player = onlinePlayer.player
        val guild = guildHandler.getGuild(player)

        if (guild == null) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD)
            return
        }

        if (!guild.getMember(player.uniqueId).role.hasPerm(GuildRolePerm.WITHDRAW_MONEY)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__ROLE_NO_PERMISSION)
            return
        }

        val bal = guild.balance

        if (amount < 0) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY)
            return
        }

        if (bal < amount) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK)
            return
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

    @Subcommand("admin simulate vault")
    @Description("{@@descriptions.admin-simulate-vault}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@online")
    @Syntax("<player>")
    fun vault(issuer: CommandIssuer, @Values("@online") onlinePlayer: OnlinePlayer) {

        val player = onlinePlayer.player
        val guild = guildHandler.getGuild(player)

        if (guild == null) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD)
            return
        }
        guilds.guiHandler.vaults.get(guild, player).open(player)
    }

    @Subcommand("admin simulate upgrade")
    @Description("{@@descriptions.admin-simulate-upgrade}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@online")
    @Syntax("<player>")
    fun upgrade(issuer: CommandIssuer, @Values("@online") onlinePlayer: OnlinePlayer) {

        val player = onlinePlayer.player
        val guild = guildHandler.getGuild(player)

        if (guildHandler.isMaxTier(guild)) {
            throw ExpectationNotMet(Messages.UPGRADE__TIER_MAX)
        }

        val tier = guildHandler.getGuildTier(guild.tier.level + 1)
        val bal = guild.balance
        val cost = tier.cost
        val async = settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC)

        if (guildHandler.memberCheck(guild)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{amount}", guild.tier.membersToRankup.toString())
            return
        }

        if (!EconomyUtils.hasEnough(bal, cost)) {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", EconomyUtils.format(cost - bal))
            return
        }

        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__MONEY_WARNING,
            "{amount}", EconomyUtils.format(cost))
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                if (!EconomyUtils.hasEnough(bal, cost)) {
                    guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", EconomyUtils.format(cost - bal))
                    return
                }

                guild.balance = bal - cost

                if (!guilds.settingsHandler.tierConf.getProperty(TierSettings.CARRY_OVER)) {
                    guildHandler.removePermsFromAll(permission, guild, async)
                }

                guildHandler.upgradeTier(guild)
                guildHandler.addPermsToAll(permission, guild, async)
                guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__SUCCESS)
                actionHandler.removeAction(player)
            }

            override fun decline() {
                guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.UPGRADE__CANCEL)
                actionHandler.removeAction(player)
            }
        })
    }
}