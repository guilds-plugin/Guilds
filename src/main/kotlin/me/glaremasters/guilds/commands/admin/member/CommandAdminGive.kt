package me.glaremasters.guilds.commands.admin.member

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants

@CommandAlias("%guilds")
internal class CommandAdminGive : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin give")
    @Description("{@@descriptions.give}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@players")
    @Syntax("%player %amount")
    fun give(issuer: CommandIssuer, @Values("@players") player: OnlinePlayer, @Default("1") amount: Int) {
        player.player.inventory.addItem(guildHandler.getUpgradeTicket(settingsManager, amount))
        currentCommandIssuer.sendInfo(Messages.CONFIRM__SUCCESS)
    }
}
