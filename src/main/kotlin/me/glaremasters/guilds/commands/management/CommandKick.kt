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
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import java.util.concurrent.TimeUnit
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildKickEvent
import me.glaremasters.guilds.configuration.sections.CooldownSettings
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.exceptions.InvalidPermissionException
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandKick : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var cooldownHandler: CooldownHandler
    @Dependency
    lateinit var permission: Permission

    @Subcommand("boot|kick")
    @Description("{@@descriptions.kick}")
    @CommandPermission(Constants.BASE_PERM + "boot")
    @CommandCompletion("@members")
    @Syntax("<name>")
    fun kick(player: Player, @Conditions("perm:perm=KICK") guild: Guild, @Values("@members") @Single name: String) {
        val user = Bukkit.getOfflinePlayer(name)
        val asMember = guild.getMember(user.uniqueId) ?: throw ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", name)

        if (guild.isMaster(user)) {
            throw InvalidPermissionException()
        }

        val event = GuildKickEvent(player, guild, user, GuildKickEvent.Cause.PLAYER_KICKED)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guildHandler.removePerms(permission, user, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC))
        cooldownHandler.addCooldown(user, Cooldown.Type.Join.name, settingsManager.getProperty(CooldownSettings.JOIN), TimeUnit.SECONDS)
        ClaimUtils.kickMember(user, player, guild, settingsManager)
        guild.removeMember(asMember)
        currentCommandIssuer.sendInfo(Messages.BOOT__SUCCESSFUL, "{player}", user.name)
        guild.sendMessage(currentCommandManager, Messages.BOOT__PLAYER_KICKED, "{player}", user.name, "{kicker}", player.name)

        if (!user.isOnline) {
            return
        }

        currentCommandManager.getCommandIssuer(user).sendInfo(Messages.BOOT__KICKED, "{kicker}", player.name)
    }
}
