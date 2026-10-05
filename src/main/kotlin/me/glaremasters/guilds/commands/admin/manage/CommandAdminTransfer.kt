package me.glaremasters.guilds.commands.admin.manage

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminTransfer : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var permission: Permission

    @Subcommand("admin transfer")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.admin-transfer}")
    @CommandCompletion("@guilds @members-admin")
    @Syntax("%guild %new-master")
    fun transfer(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, @Values("@members-admin") @Single master: String) {
        val transfer = Bukkit.getOfflinePlayer(master)

        if (guild.guildMaster.uuid == transfer.uniqueId) {
            throw ExpectationNotMet(Messages.ERROR__TRANSFER_SAME_PERSON)
        }

        if (guild.getMember(transfer.uniqueId) == null) {
            throw ExpectationNotMet(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", transfer.name.toString())
        }

        // tryTransferGuildAdmin refuses rather than half-applying, so a false here is a real
        // failure to report instead of a transfer that silently did not happen. It logs the reason
        // to the console; this is the player-facing half.
        if (!guild.tryTransferGuildAdmin(transfer, guildHandler, permission)) {
            throw ExpectationNotMet(Messages.TRANSFER__FAILED)
        }

        currentCommandIssuer.sendInfo(Messages.TRANSFER__SUCCESS)
    }
}
