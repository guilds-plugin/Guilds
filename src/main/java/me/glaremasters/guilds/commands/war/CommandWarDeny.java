package me.glaremasters.guilds.commands.war;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
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

    @Subcommand("war deny")
    @Description("{@@descriptions.war-deny}")
    @CommandPermission(Constants.WAR_PERM + "deny")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        GuildChallenge challenge = guildHandler.getChallengeByDefender(guild);

        // Check to make sure they have a pending challenge
        if (challenge == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));

        // Delete the challenge and notify both guilds
        Guild challenger = guildHandler.getGuild(challenge.getChallenger());

        // Should never be null, but just in case
        if (challenger == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        // Send message to challenger saying that the challenge has been denied
        challenger.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGE_DENIED_CHALLENGER, "{guild}", guild.getName());
        // Send message to defender saying they've denied the challenge
        getCurrentCommandIssuer().sendInfo(Messages.WAR__CHALLENGE_DENIED_DEFENDER, "{guild}", challenger.getName());
        // Remove the challenge
        guildHandler.removeChallenge(challenge);
    }

}
