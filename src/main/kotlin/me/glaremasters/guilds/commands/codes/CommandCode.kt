/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
package me.glaremasters.guilds.commands.codes

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.CodeSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.PlayerUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandCode : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("code create")
    @Description("{@@descriptions.code-create}")
    @Syntax("<uses>")
    @CommandPermission(Constants.CODE_PERM + "create")
    fun create(player: Player, @Conditions("perm:perm=CREATE_CODE") guild: Guild, @Default("1") uses: Int) {
        if (guild.getActiveCheck(settingsManager.getProperty(CodeSettings.ACTIVE_CODE_AMOUNT))) {
            throw ExpectationNotMet(Messages.CODES__MAX)
        }

        val code = StringUtils.generateString(settingsManager.getProperty(CodeSettings.CODE_LENGTH))

        guild.addCode(code, uses, player)

        currentCommandIssuer.sendInfo(Messages.CODES__CREATED, "{code}", code, "{amount}", uses.toString())
    }

    @Subcommand("code delete")
    @Description("{@@descriptions.code-delete}")
    @CommandPermission(Constants.CODE_PERM + "delete")
    @Syntax("<code>")
    @CommandCompletion("@activeCodes")
    fun delete(player: Player, @Conditions("perm:perm=DELETE_CODE") guild: Guild, @Values("@activeCodes") @Single code: String) {
        guild.removeCode(code)
        currentCommandIssuer.sendInfo(Messages.CODES__DELETED)
    }

    @Subcommand("code info")
    @Description("{@@descriptions.code-info}")
    @CommandPermission(Constants.CODE_PERM + "info")
    @Syntax("<code>")
    @CommandCompletion("@activeCodes")
    fun info(player: Player, @Conditions("perm:perm=SEE_CODE_REDEEMERS") guild: Guild, @Values("@activeCodes") @Single code: String) {
        val guildCode = guild.getCode(code) ?: throw ExpectationNotMet(Messages.CODES__INVALID_CODE)

        currentCommandIssuer.sendInfo(Messages.CODES__INFO, "{code}", guildCode.id,
                "{amount}", guildCode.uses.toString(),
                "{creator}", PlayerUtils.getPlayer(guildCode.creator).name,
                "{redeemers}", guild.getRedeemers(code))
    }

    @Subcommand("code list")
    @Description("{@@descriptions.code-list}")
    @CommandPermission(Constants.CODE_PERM + "list")
    @Syntax("")
    fun list(player: Player, guild: Guild) {
        if (guild.codes.isEmpty()) {
            throw ExpectationNotMet(Messages.CODES__EMPTY)
        }

        currentCommandIssuer.sendInfo(Messages.CODES__LIST_HEADER)

        if (settingsManager.getProperty(CodeSettings.LIST_INACTIVE_CODES)) {
            guildHandler.handleCodeList(guilds.commandManager, player, guild.codes)
        } else {
            guildHandler.handleCodeList(guilds.commandManager, player, guild.activeCodes)
        }
    }

    @Subcommand("code redeem")
    @Description("{@@descriptions.code-redeem}")
    @CommandPermission(Constants.CODE_PERM + "redeem")
    @Syntax("<code>")
    fun redeem(@Conditions("NoGuild") player: Player, code: String) {
        val guild = guildHandler.getGuildByCode(code) ?: throw ExpectationNotMet(Messages.CODES__INVALID_CODE)

        if (guildHandler.checkIfFull(guild)) {
            throw ExpectationNotMet(Messages.ACCEPT__GUILD_FULL)
        }

        guildHandler.handleInvite(guilds.commandManager, player, guild, guild.getCode(code))
    }
}
