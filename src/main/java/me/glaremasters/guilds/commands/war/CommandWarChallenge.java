package me.glaremasters.guilds.commands.war;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandWarChallenge extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ArenaHandler arenaHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private Guilds guilds;

    @Subcommand("war challenge")
    @Description("{@@descriptions.war-challenge}")
    @Syntax("<guild>")
    @CommandPermission(Constants.WAR_PERM + "challenge")
    @CommandCompletion("@guilds")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@guilds") @Single String target) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        // Make sure they aren't already challenging someone
        if (guildHandler.getChallengeByChallenger(guild) != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__ALREADY_CHALLENGING));

        // Make sure they aren't already being challenged by themselves
        if (guildHandler.getChallengeByDefender(guild) != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__ALREADY_CHALLENGING));

        // Check if there are any open arenas
        if (arenaHandler.getAvailableArena() == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__ALL_FULL));

        // Get the guild
        Guild targetGuild = guildHandler.getGuild(target);

        // Check if null
        if (targetGuild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        // Check if same guild
        if (guildHandler.isSameGuild(guild, targetGuild))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_SELF_CHALLENGE));

        // Check for online defenders to accept challenge
        if (guildHandler.getOnlineDefenders(targetGuild).isEmpty())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_DEFENDERS));

        // Min players
        int minPlayers = settingsManager.getProperty(WarSettings.MIN_PLAYERS);
        // Max players
        int maxPlayers = settingsManager.getProperty(WarSettings.MAX_PLAYERS);

        // Check to make sure both guilds have enough players on for a war
        if (!guildHandler.checkEnoughOnline(guild, targetGuild, minPlayers))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NOT_ENOUGH_ON));

        // Create the new guild challenge
        GuildChallenge challenge = guildHandler.createNewChallenge(guild, targetGuild, minPlayers, maxPlayers);

        // Add the new challenge to the handler
        guildHandler.addChallenge(challenge);

        // Set an int to the amount of time allowed to accept it an invite
        int acceptTime = settingsManager.getProperty(WarSettings.ACCEPT_TIME);

        // Send message to challenger saying that they've sent the challenge.
        getCurrentCommandIssuer().sendInfo(Messages.WAR__CHALLENGE_SENT, "{guild}", targetGuild.getName(), "{amount}", String.valueOf(acceptTime));

        // Send message to defending guild
        guildHandler.pingOnlineDefenders(targetGuild, getCurrentCommandManager(), guild.getName(), acceptTime);

        // After acceptTime is up, check if the challenge has been accepted or not
        Guilds.newChain().delay(acceptTime, TimeUnit.SECONDS).sync(() -> {
            // Check if it was denied
            if (guildHandler.getChallenge(challenge.getId()) != null) {
                // War system has already started if it's accepted so don't do anything
                if (challenge.isAccepted()) {
                    return;
                    // They have not accepted or denied it, so let's auto deny it
                } else {
                    // Send message to challenger saying they didn't accept it
                   guild.sendMessage(guilds.getCommandManager(), Messages.WAR__GUILD_EXPIRED_CHALLENGE, "{guild}", targetGuild.getName());
                   // Send message to defender saying they didn't accept it
                    targetGuild.sendMessage(guilds.getCommandManager(), Messages.WAR__TARGET_EXPIRED_CHALLENGE);
                    // Remove the challenge from the list
                    guildHandler.removeChallenge(challenge);
                }
            }
        }).execute();
    }

}
