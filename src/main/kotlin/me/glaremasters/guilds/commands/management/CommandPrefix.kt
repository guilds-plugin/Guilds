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
import me.glaremasters.guilds.api.events.GuildPrefixEvent
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandPrefix : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager

    @Subcommand("prefix")
    @Description("{@@descriptions.prefix}")
    @CommandPermission(Constants.BASE_PERM + "prefix")
    @Syntax("<prefix>")
    fun prefix(player: Player, @Conditions("perm:perm=CHANGE_PREFIX") guild: Guild, prefix: String) {
        if (settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)) {
            throw ExpectationNotMet(Messages.PREFIX__DISABLED)
        }

        if (!guildHandler.prefixCheck(prefix, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__PREFIX_TOO_LONG)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(prefix, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        val event = GuildPrefixEvent(player, guild, prefix)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        currentCommandIssuer.sendInfo(Messages.PREFIX__SUCCESSFUL, "{prefix}", prefix)
        guild.prefix = StringUtils.color(prefix)
    }
}