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

package me.glaremasters.guilds.commands.ally

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
import me.glaremasters.guilds.api.events.GuildAddAllyEvent
import me.glaremasters.guilds.api.events.GuildRemoveAllyEvent
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAlly : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds

    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("ally accept")
    @Description("{@@descriptions.ally-accept}")
    @CommandPermission(Constants.ALLY_PERM + "accept")
    @CommandCompletion("@allyInvites")
    @Syntax("<%syntax>")
    fun accept(player: Player, @Conditions("perm:perm=ADD_ALLY|NotMaxedAllies") guild: Guild, @Flags("other") target: Guild) {
        if (!guild.isAllyPending(target)) {
            return
        }

        guild.removePendingAlly(target)
        guildHandler.addAlly(guild, target)

        guild.sendMessage(currentCommandManager, Messages.ALLY__CURRENT_ACCEPTED, "{guild}", target.name)
        target.sendMessage(currentCommandManager, Messages.ALLY__TARGET_ACCEPTED, "{guild}", guild.name)
    }

    @Subcommand("ally add")
    @Description("{@@descriptions.ally-add}")
    @CommandPermission(Constants.ALLY_PERM + "add")
    @CommandCompletion("@guilds")
    @Syntax("<%syntax>")
    fun add(player: Player, @Conditions("perm:perm=ADD_ALLY|NotMaxedAllies") guild: Guild, @Flags("other") target: Guild) {
        if (target.isAllyPending(guild)) {
            throw ExpectationNotMet(Messages.ALLY__ALREADY_REQUESTED)
        }

        if (guildHandler.isAlly(guild, target)) {
            throw ExpectationNotMet(Messages.ALLY__ALREADY_ALLY)
        }

        if (guild == target) {
            throw ExpectationNotMet(Messages.ALLY__SAME_GUILD)
        }

        val event = GuildAddAllyEvent(player, guild, target)
        guilds.server.pluginManager.callEvent(event)

        if (event.isCancelled) {
            return
        }

        currentCommandIssuer.sendInfo(Messages.ALLY__INVITE_SENT, "{guild}", target.name)
        target.sendMessage(currentCommandManager, Messages.ALLY__INCOMING_INVITE, "{guild}", guild.name)
        target.addPendingAlly(guild)
    }

    @Subcommand("ally decline")
    @Description("{@@descriptions.ally-decline}")
    @CommandPermission(Constants.ALLY_PERM + "decline")
    @CommandCompletion("@allyInvites")
    @Syntax("<%syntax>")
    fun decline(player: Player, @Conditions("perm:perm=REMOVE_ALLY") guild: Guild, @Flags("other") target: Guild) {
        if (!guild.isAllyPending(target)) {
            return
        }

        guild.removePendingAlly(target)

        guild.sendMessage(currentCommandManager, Messages.ALLY__CURRENT_DECLINED, "{guild}", target.name)
        target.sendMessage(currentCommandManager, Messages.ALLY__TARGET_DECLINED, "{guild}", guild.name)
    }

    @Subcommand("ally remove")
    @Description("{@@descriptions.ally-remove}")
    @CommandPermission(Constants.ALLY_PERM + "remove")
    @CommandCompletion("@allies")
    @Syntax("<%syntax>")
    fun remove(player: Player, @Conditions("perm:perm=REMOVE_ALLY") guild: Guild, @Flags("other") target: Guild) {
        if (!guildHandler.isAlly(guild, target)) {
            throw ExpectationNotMet(Messages.ALLY__NOT_ALLIED)
        }

        val event = GuildRemoveAllyEvent(player, guild, target)
        guilds.server.pluginManager.callEvent(event)

        if (event.isCancelled) {
            return
        }

        guildHandler.removeAlly(guild, target)

        guild.sendMessage(currentCommandManager, Messages.ALLY__CURRENT_REMOVE, "{guild}", target.name)
        target.sendMessage(currentCommandManager, Messages.ALLY__TARGET_REMOVE, "{guild}", guild.name)
    }

    @Subcommand("ally list")
    @Description("{@@descriptions.ally-list}")
    @Syntax("")
    @CommandPermission(Constants.ALLY_PERM + "list")
    fun list(player: Player, guild: Guild) {
        if (!guild.hasAllies()) {
            throw ExpectationNotMet(Messages.ALLY__NONE)
        }
        currentCommandIssuer.sendInfo(Messages.ALLY__LIST, "{ally-list}", guild.allies.joinToString(", ") { guildHandler.getGuild(it).name })
    }
}
