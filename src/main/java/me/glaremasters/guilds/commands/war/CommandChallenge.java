package me.glaremasters.guilds.commands.war;

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
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandChallenge extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ArenaHandler arenaHandler;

    @Subcommand("challenge")
    @Description("{@@descriptions.challenge}")
    @Syntax("<guild>")
    @CommandPermission(Constants.WAR_PERM + "challenge")
    @CommandCompletion("@guilds")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@guilds") @Single String target) {
        if (!role.isInitiateWar())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (arenaHandler.getAvailableArena() == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__ALL_FULL));

        Guild targetGuild = guildHandler.getGuild(target);

        if (targetGuild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
    }

}
