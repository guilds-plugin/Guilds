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
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandWarAccept extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    @Subcommand("war accept")
    @Description("{@@descriptions.war-accept}")
    @CommandPermission(Constants.WAR_PERM + "accept")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        // Check to make sure they have a pending challenge
        if (guildHandler.getChallengeByDefender(guild) == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));
     }

}
