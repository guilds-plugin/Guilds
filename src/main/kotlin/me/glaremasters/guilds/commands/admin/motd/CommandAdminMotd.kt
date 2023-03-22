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
package me.glaremasters.guilds.commands.admin.motd

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
import me.glaremasters.guilds.utils.StringUtils

@CommandAlias("%guilds")
internal class CommandAdminMotd : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin motd")
    @Description("{@@descriptions.admin-motd}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun get(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        val motd = guild.motd ?: throw ExpectationNotMet(Messages.MOTD__NOT_SET)
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD, "{guild}", guild.name, "{motd}", motd)
    }

    @Subcommand("admin motd remove")
    @Description("{@@descriptions.admin-motd-remove}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun remove(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        guild.motd = null
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD_REMOVE, "{guild}", guild.name)
    }

    @Subcommand("admin motd set")
    @Description("{@@descriptions.admin-motd-set}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %motd")
    fun set(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, motd: String) {
        guild.motd = StringUtils.color(motd)
        currentCommandIssuer.sendInfo(Messages.ADMIN__MOTD_SUCCESS, "{guild}", guild.name, "{motd}", guild.motd)
    }
}
