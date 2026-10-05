package me.glaremasters.guilds.commands.management

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildTransferEvent
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandTransfer : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency lateinit var permission: Permission

    @Subcommand("transfer")
    @Description("{@@descriptions.transfer}")
    @CommandPermission(Constants.BASE_PERM + "transfer")
    @CommandCompletion("@members")
    @Syntax("%player")
    fun transfer(player: Player, @Conditions("perm:perm=TRANSFER_GUILD") guild: Guild, @Values("@members") @Single target: String) {
        val user = Bukkit.getOfflinePlayer(target)

        if (guild.guildMaster.uuid == user.uniqueId) {
            throw ExpectationNotMet(Messages.ERROR__TRANSFER_SAME_PERSON)
        }

        if (guild.getMember(user.uniqueId) == null) {
            throw ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", user.name.toString())
        }

        val event = GuildTransferEvent(player, guild, user)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        guild.transferGuild(player, user, guildHandler, permission)
        currentCommandIssuer.sendInfo(Messages.TRANSFER__SUCCESS)

        if (!user.isOnline) {
            return
        }

        currentCommandManager.getCommandIssuer(user).sendInfo(Messages.TRANSFER__NEWMASTER)
    }
}
