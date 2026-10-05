package me.glaremasters.guilds.listeners

import ch.jalu.configme.SettingsManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerListener(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler, private val permission: Permission) : Listener {

    @EventHandler
    fun PlayerJoinEvent.onMOTD() {
        val guild = guildHandler.getGuild(player) ?: return
        val motd = guild.motd ?: return

        if (!settingsManager.getProperty(GuildSettings.MOTD_ON_LOGIN)) {
            return
        }
        Bukkit.getScheduler().runTaskLater(guilds, Runnable {
            guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.MOTD__MOTD, "{motd}", motd)
        }, 100L)
    }

    @EventHandler
    fun PlayerJoinEvent.onLastLoginUpdate() {
        val guild = guildHandler.getGuild(player) ?: return
        val member = guild.getMember(player.uniqueId)

        if (member.joinDate == 0L) {
            member.joinDate = System.currentTimeMillis()
        }

        member.lastLogin = System.currentTimeMillis()
    }

    @EventHandler
    fun PlayerJoinEvent.onUpdateSkullCheck() {
        val guild = guildHandler.getGuild(player) ?: return

        if (!guild.isMaster(player)) {
            return
        }

        guild.updateGuildSkull(player, settingsManager)
    }

    @EventHandler
    fun PlayerJoinEvent.onPermCheck() {
        guildHandler.addGuildPerms(permission, player)
        guildHandler.addRolePerm(permission, player)
    }

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        val guild = guildHandler.getGuild(player) ?: return
        val home = guild.home ?: return

        if (!settingsManager.getProperty(GuildSettings.RESPAWN_AT_HOME)) {
            return
        }

        respawnLocation = home.asLocation
    }
}
