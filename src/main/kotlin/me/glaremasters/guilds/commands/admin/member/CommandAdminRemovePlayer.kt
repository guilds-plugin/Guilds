package me.glaremasters.guilds.commands.admin.member

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildKickEvent
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

// An admin cannot remove a guild's own master: the guild would be left with an owner who is not in
// it, which nothing can recover from. Transfer the guild first, or remove the guild itself.
@CommandAlias("%guilds")
internal class CommandAdminRemovePlayer : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var permission: Permission

    @Subcommand("admin removeplayer")
    @Description("{@@descriptions.admin-removeplayer}")
    @CommandPermission(Constants.ADMIN_PERM)
    @Syntax("%name")
    fun remove(player: Player, target: String) {
        val user = Bukkit.getOfflinePlayer(target)
        val guild = guildHandler.getGuild(user) ?: throw ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST)
        val event = GuildKickEvent(player, guild, user, GuildKickEvent.Cause.ADMIN_KICKED)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        // Checked before anything is mutated. Removing the master used to leave guildMaster
        // pointing at somebody who is no longer a member, after which every later read of it threw
        // and the guild could never be transferred to anyone again.
        if (guild.isMaster(user)) {
            throw ExpectationNotMet(Messages.ADMIN__CANT_REMOVE_MASTER, "{player}", user.name ?: name, "{guild}", guild.name)
        }

        ClaimUtils.kickMember(user, player, guild, settingsManager)

        guildHandler.removeRolePerm(permission, user)
        guildHandler.removeGuildPerms(permission, user)

        guild.removeMember(user)
        guildHandler.removeFromMemberCache(user.uniqueId)

        if (user.isOnline) {
            currentCommandManager.getCommandIssuer(user).sendInfo(Messages.ADMIN__PLAYER_REMOVED, "{guild}", guild.name)
        }

        guildHandler.removeFromChat(user.uniqueId)

        currentCommandIssuer.sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED, "{player}", user.name, "{guild}", guild.name)
        guild.sendMessage(currentCommandManager, Messages.ADMIN__ADMIN_GUILD_REMOVE, "{player}", user.name, "{guild}", guild.name)
    }
}
