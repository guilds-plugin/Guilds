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
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.api.events.GuildRemoveEvent
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.ClaimUtils
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandDelete : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var permission: Permission
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var actionHandler: ActionHandler

    @Subcommand("delete")
    @Description("{@@descriptions.delete}")
    @CommandPermission(Constants.BASE_PERM + "delete")
    @Syntax("")
    @Conditions("NotMigrating")
    fun delete(player: Player, @Conditions("perm:perm=REMOVE_GUILD") guild: Guild) {
        currentCommandIssuer.sendInfo(Messages.DELETE__WARNING)
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                val event = GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.PLAYER_DELETED)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    actionHandler.removeAction(player)
                    return
                }

                guild.members.forEach { member ->
                    guildHandler.removeFromChat(member.uuid)
                }

                guildHandler.removeGuildPermsFromAll(permission, guild)
                guildHandler.removeRolePermsFromAll(permission, guild)
                guildHandler.removeAlliesOnDelete(guild)
                guildHandler.notifyAllies(guild, guilds.commandManager)
                guild.sendMessage(currentCommandManager, Messages.LEAVE__GUILDMASTER_LEFT, "{player}", player.name)
                ClaimUtils.deleteWithGuild(guild, settingsManager)
                guildHandler.removeGuild(guild)
                currentCommandIssuer.sendInfo(Messages.DELETE__SUCCESSFUL, "{guild}", guild.name)
                actionHandler.removeAction(player)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.DELETE__CANCELLED)
                actionHandler.removeAction(player)
            }
        })
    }
}
