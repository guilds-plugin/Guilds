package me.glaremasters.guilds.commands.admin.war

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.configuration.sections.WarSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.tasks.GuildWarJoinTask
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player
import java.util.stream.Collectors
import java.util.stream.Stream

@CommandAlias("%guilds")
class CommandAdminWar : BaseCommand() {
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

    @Subcommand("admin war create")
    @Description("Force create a war between 2 guilds")
    @Syntax("<guild> <other guild>")
    @CommandAlias(Constants.ADMIN_PERM)
    fun create(player: Player, @Flags("other") @Values("@guilds") guild: Guild, @Flags("other") @Values("@guilds") targetGuild: Guild) {
        if (challengeHandler.getChallenge(guild) != null || challengeHandler.getChallenge(targetGuild) != null) {
            throw ExpectationNotMet("One of the guilds you chose is already in another war.")
        }

        val available = arenaHandler.getAvailableArena().isPresent

        if (!available) {
            throw ExpectationNotMet("All arenas currently full")
        }

        val arena = arenaHandler.getAvailableArena().get()

        if (guildHandler.isSameGuild(guild, targetGuild)) {
            throw ExpectationNotMet("You can not force a guild to war with itself.")
        }

        if (challengeHandler.getOnlineDefenders(guild).isEmpty() || challengeHandler.getOnlineDefenders(targetGuild).isEmpty()) {
            throw ExpectationNotMet("One of the guilds does not have any defenders online.")
        }

        if (arena.challengerLoc == null) {
            throw ExpectationNotMet(Messages.ARENA__LOCATION_ISSUE__CHALLENGER, "{arena}", arena.name)
        }

        if (arena.defenderLoc == null) {
            throw ExpectationNotMet(Messages.ARENA__LOCATION_ISSUE__DEFENDER, "{arena}", arena.name)
        }

        // Possibly some logic here and another config option for ignoring even players or not

        val challenge = challengeHandler.createNewChallenge(guild, targetGuild, 0, 100, arena)
        challengeHandler.addChallenge(challenge)
        arena.inUse = true

        //todo make these valid later
        val joinTime = settingsManager.getProperty(WarSettings.JOIN_TIME)
        val readyTime = settingsManager.getProperty(WarSettings.READY_TIME)

        guild.sendMessage("You will be automatically teleported to the arena shortly for a war.")
        targetGuild.sendMessage("You will be automatically teleported to the arena shortly for a war.")

        challenge.isAccepted = true
        challenge.isJoinble = true

        val joinMsg = currentCommandManager.locales.getMessage(currentCommandIssuer, Messages.WAR__ACTION_BAR_JOIN.messageKey)
        val readyMsg = currentCommandManager.locales.getMessage(currentCommandIssuer, Messages.WAR__ACTION_BAR_READY.messageKey)

        val online = Stream.concat(guild.onlineAsUUIDs.stream(), targetGuild.onlineAsUUIDs.stream()).collect(Collectors.toList())

        challenge.defender.onlineAsPlayers.forEach {
            challenge.defendPlayers.add(it.uniqueId)
        }

        challenge.challenger.onlineAsPlayers.forEach {
            challenge.challengePlayers.add(it.uniqueId)
        }

        GuildWarJoinTask(guilds, joinTime, readyTime, online, joinMsg, readyMsg, challenge, challengeHandler).runTaskTimer(guilds, 0L, 20L)
    }
}
