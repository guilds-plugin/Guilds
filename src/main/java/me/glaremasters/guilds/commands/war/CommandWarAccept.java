package me.glaremasters.guilds.commands.war;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.tasks.GuildWarJoinTask;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandWarAccept extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ChallengeHandler challengeHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private Guilds guilds;

    @Subcommand("war accept")
    @Description("{@@descriptions.war-accept}")
    @CommandPermission(Constants.WAR_PERM + "accept")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        GuildChallenge challenge = challengeHandler.getChallenge(guild);

        // Check to make sure they have a pending challenge
        if (challenge == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));

        // Get the challenger guild cause we assume this is the defender
        Guild challenger = challenge.getChallenger();

        // Check again when accepting to make sure there are still enough players online
        if (!challengeHandler.checkEnoughOnline(challenger, guild, challenge.getMinPlayersPerSide()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NOT_ENOUGH_ON));

        // Variable for join time
        int joinTime = settingsManager.getProperty(WarSettings.JOIN_TIME);

        // Variable for ready time
        int readyTime = settingsManager.getProperty(WarSettings.READY_TIME);

        // Send message to challenger
        challenger.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGER_WAR_ACCEPTED,
                "{guild}", guild.getName(),
                "{amount}", String.valueOf(joinTime));

        // Send message to defender
        guild.sendMessage(getCurrentCommandManager(), Messages.WAR__DEFENDER_WAR_ACCEPTED,
                "{guild}", challenger.getName(),
                "{amount}", String.valueOf(joinTime));

        // Mark the challenge as accepted
        challenge.setAccepted(true);
        challenge.setJoinble(true);

        // Get all the players to send the action bar to
        List<UUID> online = Stream.concat(guild.getOnlineAsUUIDs().stream(), challenger.getOnlineAsUUIDs().stream()).collect(Collectors.toList());

        // The message to send
        String joinMsg = getCurrentCommandManager().getLocales().getMessage(getCurrentCommandIssuer(), Messages.WAR__ACTION_BAR_JOIN.getMessageKey());
        String readyMsg = getCurrentCommandManager().getLocales().getMessage(getCurrentCommandIssuer(), Messages.WAR__ACTION_BAR_READY.getMessageKey());

        // Send the ActionBar for the join time
        new GuildWarJoinTask(guilds, joinTime, readyTime, online, joinMsg, readyMsg, challenge, challengeHandler).runTaskTimer(guilds, 0L, 20L);
    }

}
