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

import java.util.UUID;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandChallenge extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ArenaHandler arenaHandler;
    @Dependency private SettingsManager settingsManager;

    @Subcommand("challenges")
    @Description("{@@descriptions.challenges}")
    @Syntax("<guild>")
    @CommandPermission(Constants.WAR_PERM + "challenges")
    @CommandCompletion("@guilds")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@guilds") @Single String target) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        // Check if there are any open arenas
        if (arenaHandler.getAvailableArena() == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__ALL_FULL));

        // Get the guild
        Guild targetGuild = guildHandler.getGuild(target);

        // Check if null
        if (targetGuild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));

        // Check for online defenders to accept challenges
        if (guildHandler.getOnlineDefenders(targetGuild).isEmpty())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_DEFENDERS));

        // Create the new guild challenge
        GuildChallenge challenge = new GuildChallenge(UUID.randomUUID(), System.currentTimeMillis(), guild.getId(), targetGuild.getId(), false);

        // Add the new challenge to the handler
        guildHandler.getChallenges().add(challenge);

        // Set an int to the amount of time allowed to accept it an invite
        int acceptTime = settingsManager.getProperty(WarSettings.ACCEPT_TIME);

        // Send message to challenger saying that they've sent the challenge.
        getCurrentCommandIssuer().sendInfo(Messages.WAR__CHALLENGE_SENT, "{guild}", targetGuild.getName(), "{amount}", String.valueOf(acceptTime));

        // Send message to defending guild
        guildHandler.pingOnlineDefenders(targetGuild, getCurrentCommandManager(), guild.getName(), acceptTime);
    }

}
