package me.glaremasters.guilds.commands.motd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 5/22/2019
 * Time: 11:00 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandMotd extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    // View the actual MOTD
    @Subcommand("motd")
    @CommandPermission(Constants.BASE_PERM + "motd")
    public void execute(Player player, Guild guild, GuildRole guildRole) {

    }

}