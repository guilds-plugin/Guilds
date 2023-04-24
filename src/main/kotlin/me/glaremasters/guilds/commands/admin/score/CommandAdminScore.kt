/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
package me.glaremasters.guilds.commands.admin.score

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
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants

@CommandAlias("%guilds")
internal class CommandAdminScore : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler

    @Subcommand("admin score setwins")
    @Description("{@@descriptions.admin-score-setwins}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %amount")
    fun setWins(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Int) {
        guild.guildScore.wins = amount
        currentCommandIssuer.sendInfo(Messages.ADMIN__SCORE_SETWINS, "{guild}", guild.name, "{amount}", amount.toString())
    }

    @Subcommand("admin score setloses")
    @Description("{@@descriptions.admin-score-setloses}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %amount")
    fun setLoses(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, amount: Int) {
        guild.guildScore.loses = amount
        currentCommandIssuer.sendInfo(Messages.ADMIN__SCORE_SETLOSES, "{guild}", guild.name, "{amount}", amount.toString())
    }

    @Subcommand("admin score resetall")
    @Description("{@@descriptions.admin-score-resetall}")
    @CommandPermission(Constants.ADMIN_PERM)
    fun resetAll(issuer: CommandIssuer) {
        guildHandler.guilds.values.forEach { guild ->
            guild.guildScore.reset()
        }
        currentCommandIssuer.sendInfo(Messages.ADMIN__SCORE_RESETALL)
    }
}
