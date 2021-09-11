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
import me.glaremasters.guilds.api.events.GuildRenameEvent
import me.glaremasters.guilds.claim.ClaimEditor
import me.glaremasters.guilds.configuration.sections.CostSettings
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.claim.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper

@CommandAlias("%guilds")
internal class CommandRename : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager

    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission(Constants.BASE_PERM + "rename")
    @Syntax("<name>")
    fun rename(player: Player, @Conditions("perm:perm=RENAME") guild: Guild, name: String) {
        if (guildHandler.checkGuildNames(name)) {
            throw ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN)
        }

        if (!guildHandler.nameCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__REQUIREMENTS)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        val renameCost = settingsManager.getProperty(CostSettings.RENAME)
        val charge = renameCost != 0.0

        if (charge && !EconomyUtils.hasEnough(guild.balance, renameCost)) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        val event = GuildRenameEvent(player, guild, name)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guild.balance = guild.balance - renameCost

        guild.name = StringUtils.color(name)

        if (ClaimUtils.isEnable(settingsManager)) {
            val wrapper = WorldGuardWrapper.getInstance()

            if (ClaimUtils.checkIfHaveClaims(wrapper, guild)) {
                for (claim in guild.claimedLand) {
                    ClaimEditor.setEnterMessage(wrapper, claim, settingsManager, guild)
                    ClaimEditor.setExitMessage(wrapper, claim, settingsManager, guild)
                }
            }
        }

        currentCommandIssuer.sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name)
    }
}
