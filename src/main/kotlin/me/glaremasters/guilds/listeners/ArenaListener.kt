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
package me.glaremasters.guilds.listeners

import ch.jalu.configme.SettingsManager
import co.aikar.commands.ACFBukkitUtil
import java.util.UUID
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.configuration.sections.WarSettings
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class ArenaListener(private val guilds: Guilds, private val challengeHandler: ChallengeHandler, private val settingsManager: SettingsManager) : Listener {
    private val playerDeath = mutableMapOf<UUID, String>()

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        val challenge = challengeHandler.getChallenge(player) ?: return

        challengeHandler.announceDeath(challenge, guilds, player, player, ChallengeHandler.Cause.PLAYER_KILLED_QUIT)
        challengeHandler.handleFinish(guilds, settingsManager, player, challenge)
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        val challenge = challengeHandler.getChallenge(entity) ?: return

        if (!challenge.isStarted) {
            return
        }

        if (settingsManager.getProperty(WarSettings.KEEP_INVENTORY)) {
            keepInventory = true
        }

        if (settingsManager.getProperty(WarSettings.CLEAR_DROPS)) {
            drops.clear()
        }

        if (settingsManager.getProperty(WarSettings.KEEP_EXP)) {
            keepLevel = true
        }

        val death = challengeHandler.getAllPlayersAlive(challenge)[entity.uniqueId] ?: return

        playerDeath[entity.uniqueId] = death
        challengeHandler.announceDeath(challenge, guilds, entity, entity, ChallengeHandler.Cause.PLAYER_KILLED_UNKNOWN)
        challengeHandler.handleFinish(guilds, settingsManager, entity, challenge)
    }

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        if (player.uniqueId !in playerDeath) {
            return
        }
        Bukkit.getScheduler().runTaskLater(guilds, Runnable {
            val loc = ACFBukkitUtil.stringToLocation(playerDeath[player.uniqueId])
            player.teleport(loc)
            playerDeath.remove(player.uniqueId)
        }, 1L)
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onCommand() {
        if (!settingsManager.getProperty(WarSettings.DISABLE_COMMANDS)) {
            return
        }

        val challenge = challengeHandler.getChallenge(player) ?: return

        if (player.hasPermission(Constants.ADMIN_PERM)) {
            return
        }

        isCancelled = true
        guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.WAR__COMMANDS_BLOCKED)
    }
}
