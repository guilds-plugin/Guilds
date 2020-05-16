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
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import java.util.UUID
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.api.events.GuildCreateEvent
import me.glaremasters.guilds.configuration.sections.CostSettings
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildMember
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.StringUtils
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandCreate : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var actionHandler: ActionHandler
    @Dependency lateinit var economy: Economy
    @Dependency lateinit var permission: Permission
    @Dependency lateinit var cooldownHandler: CooldownHandler

    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission(Constants.BASE_PERM + "create")
    @Syntax("<name> (optional) <prefix>")
    @Conditions("NotMigrating")
    fun create(@Conditions("NoGuild") player: Player, name: String, @Optional prefix: String?) {
        val cooldown = Cooldown.Type.Join.name
        val id = player.uniqueId

        if (cooldownHandler.hasCooldown(cooldown, id)) {
            throw ExpectationNotMet(Messages.ACCEPT__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldown, id).toString())
        }

        val cost = settingsManager.getProperty(CostSettings.CREATION)

        if (guildHandler.checkGuildNames(name)) {
            throw ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        if (!guildHandler.nameCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__REQUIREMENTS)
        }

        if (!settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)) {
            if (prefix != null) {
                if (!guildHandler.prefixCheck(prefix, settingsManager)) {
                    throw ExpectationNotMet(Messages.CREATE__PREFIX_TOO_LONG)
                }
            } else {
                if (!guildHandler.prefixCheck(name, settingsManager)) {
                    throw ExpectationNotMet(Messages.CREATE__NAME_TOO_LONG)
                }
            }
        }

        if (!EconomyUtils.hasEnough(currentCommandManager, economy, player, cost)) {
            throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
        }

        currentCommandIssuer.sendInfo(Messages.CREATE__WARNING, "{amount}", EconomyUtils.format(cost))
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                if (!EconomyUtils.hasEnough(currentCommandManager, economy, player, cost)) {
                    throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
                }

                val gb = Guild.builder()
                gb.id(UUID.randomUUID())
                gb.name(StringUtils.color(name))

                if (!settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)) {
                    if (prefix == null) gb.prefix(StringUtils.color(name)) else gb.prefix(StringUtils.color(prefix))
                } else {
                    gb.prefix("")
                }

                gb.status(Guild.Status.Private)
                val master = GuildMember(id, guildHandler.getGuildRole(0))
                master.joinDate = System.currentTimeMillis()
                gb.guildMaster(master)

                val members = mutableListOf<GuildMember>()
                members.add(master)
                gb.members(members)
                gb.home(null)
                gb.balance(0.0)
                gb.tier(guildHandler.getGuildTier(1))

                gb.invitedMembers(arrayListOf())
                gb.allies(arrayListOf())
                gb.pendingAllies(arrayListOf())

                gb.vaults(arrayListOf())
                gb.codes(arrayListOf())

                val guild = gb.build()
                guild.creationDate = System.currentTimeMillis()

                val event = GuildCreateEvent(player, guild)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    return
                }

                guildHandler.addGuild(guild)
                economy.withdrawPlayer(player, cost)
                currentCommandIssuer.sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.name)
                guildHandler.addPerms(permission, player, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC))

                guild.updateGuildSkull(player, settingsManager)

                actionHandler.removeAction(player)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.CREATE__CANCELLED)
                actionHandler.removeAction(player)
            }
        })
    }
}
