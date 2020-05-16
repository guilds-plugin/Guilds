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

package me.glaremasters.guilds.commands.management

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.configuration.sections.TierSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import net.milkbowl.vault.permission.Permission
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandUpgrade : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var actionHandler: ActionHandler
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var permission: Permission

    @Subcommand("upgrade")
    @Description("{@@descriptions.upgrade}")
    @CommandPermission(Constants.BASE_PERM + "upgrade")
    @Syntax("")
    fun upgrade(player: Player, @Conditions("perm:perm=UPGRADE_GUILD") guild: Guild) {
        if (guildHandler.isMaxTier(guild)) {
            throw ExpectationNotMet(Messages.UPGRADE__TIER_MAX)
        }

        val tier = guildHandler.getGuildTier(guild.tier.level + 1)
        val bal = guild.balance
        val cost = tier.cost
        val async = settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC)

        if (guildHandler.memberCheck(guild)) {
            throw ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MEMBERS, "{amount}", guild.tier.membersToRankup.toString())
        }

        if (!EconomyUtils.hasEnough(bal, cost)) {
            throw ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", EconomyUtils.format(cost - bal))
        }

        currentCommandIssuer.sendInfo(Messages.UPGRADE__MONEY_WARNING, "{amount}", EconomyUtils.format(cost))
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                if (!EconomyUtils.hasEnough(bal, cost)) {
                    throw ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", EconomyUtils.format(cost - bal))
                }

                guild.balance = bal - cost

                if (!guilds.settingsHandler.tierConf.getProperty(TierSettings.CARRY_OVER)) {
                    guildHandler.removePermsFromAll(permission, guild, async)
                }

                guildHandler.upgradeTier(guild)
                guildHandler.addPermsToAll(permission, guild, async)
                currentCommandIssuer.sendInfo(Messages.UPGRADE__SUCCESS)
                actionHandler.removeAction(player)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.UPGRADE__CANCEL)
                actionHandler.removeAction(player)
            }
        })
    }
}
