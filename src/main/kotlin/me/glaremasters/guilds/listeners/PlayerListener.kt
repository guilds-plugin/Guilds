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

package me.glaremasters.guilds.listeners

import ch.jalu.configme.SettingsManager
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.StringUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerListener(private val guilds: Guilds, private val settingsManager: SettingsManager, private val guildHandler: GuildHandler, private val permission: Permission) : Listener {
    private val informed = mutableSetOf<UUID>()

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        if (!settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_IN_GAME)) {
            return
        }
        if (!player.isOp) {
            return
        }
        if (informed.contains(player.uniqueId)) {
            return
        }
        Guilds.newChain<Any>().delay(5, TimeUnit.SECONDS).async {
            try {
                val hover = HoverEvent.showText(Component.text(StringUtils.getAnnouncements(guilds)))
                val click = ClickEvent.openUrl(guilds.description.website.toString())
                val announcement = Component.text(StringUtils.color("&f[&aGuilds&f]&r Announcements (Hover over me for more information)")).clickEvent(click).hoverEvent(hover)
                guilds.adventure.sender(player).sendMessage(announcement)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.execute()
        informed.add(player.uniqueId)
    }

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
        guildHandler.addPerms(permission, player, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC))
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
