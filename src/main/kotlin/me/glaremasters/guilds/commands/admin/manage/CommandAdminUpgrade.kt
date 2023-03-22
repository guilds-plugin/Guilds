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
package me.glaremasters.guilds.commands.admin.manage

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.*
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.configuration.sections.TierSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission

@CommandAlias("%guilds")
internal class CommandAdminUpgrade : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var permission: Permission
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin upgrade")
    @Description("{@@descriptions.admin-upgrade}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun upgrade(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        if (guildHandler.isMaxTier(guild)) {
            throw ExpectationNotMet(Messages.UPGRADE__TIER_MAX)
        }

        guildHandler.removeGuildPermsFromAll(permission, guild)
        guildHandler.upgradeTier(guild)
        guildHandler.addGuildPermsToAll(permission, guild)

        currentCommandIssuer.sendInfo(Messages.ADMIN__ADMIN_UPGRADE, "{guild}", guild.name)
        guild.sendMessage(currentCommandManager, Messages.ADMIN__ADMIN_GUILD_UPGRADE)
    }
}
