package me.glaremasters.guilds.commands.war;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandWarDeny extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ChallengeHandler challengeHandler;

    @Subcommand("war deny")
    @Description("{@@descriptions.war-deny}")
    @CommandPermission(Constants.WAR_PERM + "deny")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        GuildChallenge challenge = challengeHandler.getChallenge(guild);

        // Check to make sure they have a pending challenge
        if (challenge == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));

        // Get the challenger guild cause we assume this is the defender
        Guild challenger = challenge.getChallenger();

        // Should never be null, but just in case
        if (challenger == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        // Send message to challenger saying that the challenge has been denied
        challenger.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGE_DENIED_CHALLENGER, "{guild}", guild.getName());
        // Send message to defender saying they've denied the challenge
        guild.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGE_DENIED_DEFENDER, "{guild}", challenger.getName());
        // Remove the challenge
        challengeHandler.removeChallenge(challenge);
    }

}
