/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
package me.glaremasters.guilds.commands.war

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import java.util.stream.Collectors
import java.util.stream.Stream
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.challenges.GuildWarAcceptEvent
import me.glaremasters.guilds.api.events.challenges.GuildWarChallengeEvent
import me.glaremasters.guilds.api.events.challenges.GuildWarDeclineEvent
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.configuration.sections.WarSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.tasks.GuildWarChallengeCheckTask
import me.glaremasters.guilds.tasks.GuildWarJoinTask
import me.glaremasters.guilds.utils.Constants
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandWar : BaseCommand() {
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Dependency
    lateinit var settingsManager: SettingsManager

    @Dependency
    lateinit var challengeHandler: ChallengeHandler

    @Dependency
    lateinit var arenaHandler: ArenaHandler

    @Dependency
    lateinit var guilds: Guilds

    @Subcommand("war accept")
    @Description("{@@descriptions.war-accept}")
    @Syntax("")
    @CommandPermission(Constants.WAR_PERM + "accept")
    fun accept(player: Player, @Conditions("perm:perm=INITIATE_WAR") guild: Guild) {
        val challenge = challengeHandler.getChallenge(guild) ?: throw ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE)
        val challenger = challenge.challenger

        if (challenge.isAccepted) {
            throw ExpectationNotMet(Messages.WAR__ALREADY_ACCEPTED)
        }

        val event = GuildWarAcceptEvent(player, guild, challenger)
        Bukkit.getPluginManager().callEvent(event)

        val joinTime = settingsManager.getProperty(WarSettings.JOIN_TIME)
        val readyTime = settingsManager.getProperty(WarSettings.READY_TIME)

        challenger.sendMessage(currentCommandManager, Messages.WAR__CHALLENGER_WAR_ACCEPTED, "{guild}", guild.name, "{amount}", joinTime.toString())
        guild.sendMessage(currentCommandManager, Messages.WAR__DEFENDER_WAR_ACCEPTED, "{guild}", challenger.name, "{amount}", joinTime.toString())

        challenge.isAccepted = true
        challenge.isJoinble = true

        val online = Stream.concat(guild.onlineAsUUIDs.stream(), challenger.onlineAsUUIDs.stream()).collect(Collectors.toList())

        val joinMsg = currentCommandManager.locales.getMessage(currentCommandIssuer, Messages.WAR__ACTION_BAR_JOIN.messageKey)
        val readyMsg = currentCommandManager.locales.getMessage(currentCommandIssuer, Messages.WAR__ACTION_BAR_READY.messageKey)

        GuildWarJoinTask(guilds, joinTime, readyTime, online, joinMsg, readyMsg, challenge, challengeHandler).runTaskTimer(guilds, 0L, 20L)
    }

    @Subcommand("war challenge")
    @Description("{@@descriptions.war-challenge}")
    @Syntax("<%syntax>")
    @CommandPermission(Constants.WAR_PERM + "challenge")
    @CommandCompletion("@guilds")
    fun challenge(player: Player, @Conditions("perm:perm=INITIATE_WAR") guild: Guild, @Flags("other") targetGuild: Guild) {
        if (challengeHandler.getChallenge(guild) != null) {
            throw ExpectationNotMet(Messages.WAR__ALREADY_CHALLENGING)
        }

        val available = arenaHandler.getAvailableArena().isPresent

        if (!available) {
            throw ExpectationNotMet(Messages.ARENA__ALL_FULL)
        }

        val arena = arenaHandler.getAvailableArena().get()

        if (guildHandler.isSameGuild(guild, targetGuild)) {
            throw ExpectationNotMet(Messages.WAR__NO_SELF_CHALLENGE)
        }

        if (!challengeHandler.notOnCooldown(targetGuild, settingsManager)) {
            throw ExpectationNotMet(Messages.WAR__DEFEND_COOLDOWN, "{guild}", targetGuild.name)
        }

        if (challengeHandler.getOnlineDefenders(targetGuild).isEmpty()) {
            throw ExpectationNotMet(Messages.WAR__NO_DEFENDERS)
        }

        if (arena.challengerLoc == null) {
            throw ExpectationNotMet(Messages.ARENA__LOCATION__ISSUE__CHALLENGER, "{arena}", arena.name)
        }

        if (arena.defenderLoc == null) {
            throw ExpectationNotMet(Messages.ARENA__LOCATION__ISSUE__DEFENDER, "{arena}", arena.name)
        }

        val event = GuildWarChallengeEvent(player, guild, targetGuild)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }


        val minPlayers = settingsManager.getProperty(WarSettings.MIN_PLAYERS)
        val maxPlayers = settingsManager.getProperty(WarSettings.MAX_PLAYERS)
        val acceptTime = settingsManager.getProperty(WarSettings.ACCEPT_TIME)

        if (!challengeHandler.checkEnoughOnline(guild, targetGuild, minPlayers)) {
            throw ExpectationNotMet(Messages.WAR__NOT_ENOUGH_ON)
        }

        val challenge = challengeHandler.createNewChallenge(guild, targetGuild, minPlayers, maxPlayers, arena)
        challengeHandler.addChallenge(challenge)

        arena.inUse = true

        currentCommandIssuer.sendInfo(Messages.WAR__CHALLENGE_SENT, "{guild}", targetGuild.name, "{amount}", acceptTime.toString())

        challengeHandler.pingOnlineDefenders(targetGuild, guilds.commandManager, guild.name, acceptTime)

        GuildWarChallengeCheckTask(guilds, challenge, challengeHandler).runTaskLater(guilds, (acceptTime * 20).toLong())
    }

    @Subcommand("war deny")
    @Description("{@@descriptions.war-deny}")
    @CommandPermission(Constants.WAR_PERM + "deny")
    @Syntax("")
    fun deny(player: Player, @Conditions("perm:perm=INITIATE_WAR") guild: Guild) {
        val challenge = challengeHandler.getChallenge(guild) ?: throw ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE)
        val challenger = challenge.challenger

        val event = GuildWarDeclineEvent(player, challenger, guild)
        Bukkit.getPluginManager().callEvent(event)

        challenger.sendMessage(currentCommandManager, Messages.WAR__CHALLENGE_DENIED_CHALLENGER, "{guild}", guild.name)
        guild.sendMessage(currentCommandManager, Messages.WAR__CHALLENGE_DENIED_DEFENDER, "{guild}", challenger.name)

        challenge.arena.inUse = false

        challengeHandler.removeChallenge(challenge)
    }

    @Subcommand("war join")
    @Description("{@@descriptions.war-join}")
    @Syntax("")
    @CommandPermission(Constants.WAR_PERM + "join")
    fun join(player: Player, guild: Guild) {
        val challenge = challengeHandler.getChallenge(guild) ?: throw ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE)

        if (!challenge.isJoinble) {
            throw ExpectationNotMet(Messages.WAR__NOT_JOINABLE)
        }

        if (challenge.defender == guild) {
            if (challenge.defendPlayers.contains(player.uniqueId)) {
                throw ExpectationNotMet(Messages.WAR__ALREADY_JOINED)
            }
            if (challenge.defendPlayers.size == challenge.maxPlayersPerSide) {
                throw ExpectationNotMet(Messages.WAR__ALREADY_AT_MAX)
            }
            challenge.defendPlayers.add(player.uniqueId)
        } else {
            if (challenge.challengePlayers.contains(player.uniqueId)) {
                throw ExpectationNotMet(Messages.WAR__ALREADY_JOINED)
            }
            if (challenge.challengePlayers.size == challenge.maxPlayersPerSide) {
                throw ExpectationNotMet(Messages.WAR__ALREADY_AT_MAX)
            }
            challenge.challengePlayers.add(player.uniqueId)
        }

        guild.sendMessage(currentCommandManager, Messages.WAR__WAR_JOINED, "{player}", player.name)
    }
}
