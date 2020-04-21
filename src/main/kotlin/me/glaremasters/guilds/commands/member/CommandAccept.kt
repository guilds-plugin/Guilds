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

package me.glaremasters.guilds.commands.member

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildJoinEvent
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandAccept : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var permission: Permission
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var cooldownHandler: CooldownHandler

    @Subcommand("accept|join")
    @Description("{@@descriptions.accept}")
    @CommandPermission(Constants.BASE_PERM + "accept")
    @CommandCompletion("@joinableGuilds")
    @Syntax("<%syntax>")
    fun accept(@Conditions("NoGuild") player: Player, @Flags("other") guild: Guild) {
        val cooldown = Cooldown.Type.Join.name

        if (cooldownHandler.hasCooldown(cooldown, player.uniqueId)) {
            throw ExpectationNotMet(Messages.ACCEPT__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldown, player.uniqueId).toString())
        }

        if (!guild.checkIfInvited(player) && guild.isPrivate) {
            throw ExpectationNotMet(Messages.ACCEPT__NOT_INVITED)
        }

        if (guildHandler.checkIfFull(guild)) {
            throw ExpectationNotMet(Messages.ACCEPT__GUILD_FULL)
        }

        val event = GuildJoinEvent(player, guild)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guild.sendMessage(currentCommandManager, Messages.ACCEPT__PLAYER_JOINED, "{player}", player.name)
        guild.addMember(player, guildHandler)
        guildHandler.addPerms(permission, player, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC))

        if (ClaimUtils.isEnable(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()
            ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent { region -> ClaimUtils.addMember(region, player) }
        }

        currentCommandIssuer.sendInfo(Messages.ACCEPT__SUCCESSFUL, "{guild}", guild.name)
    }
}