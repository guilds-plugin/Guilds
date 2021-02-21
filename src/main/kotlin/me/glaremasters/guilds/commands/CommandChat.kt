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

package me.glaremasters.guilds.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.listeners.ChatListener
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandChat : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds

    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("gc")
    @Description("{@@descriptions.chat}")
    @CommandPermission(Constants.BASE_PERM + "chat")
    @Syntax("[msg]")
    fun guildChat(player: Player, @Conditions("perm:perm=CHAT") guild: Guild, @Optional msg: String?) {
        if (msg == null) {
            guilds.chatListener.handleToggle(player, ChatListener.ChatType.GUILD)
        } else {
            guildHandler.handleGuildChat(guild, player, msg)
        }
    }

    @Subcommand("ac")
    @Description("{@@descriptions.ally-chat}")
    @CommandPermission(Constants.BASE_PERM + "chat")
    @Syntax("[msg]")
    fun chat(player: Player, @Conditions("perm:perm=ALLY_CHAT") guild: Guild, @Optional msg: String?) {
        if (msg == null) {
            guilds.chatListener.handleToggle(player, ChatListener.ChatType.ALLY)
        } else {
            guildHandler.handleAllyChat(guild, player, msg)
        }
    }
}
