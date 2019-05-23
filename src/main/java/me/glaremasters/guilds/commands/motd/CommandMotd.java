package me.glaremasters.guilds.commands.motd;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
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
    @Description("{@@descriptions.kick}")
    @CommandPermission(Constants.BASE_PERM + "motd")
    public void execute(Player player, Guild guild) {
        // Check if motd is null
        if (guild.getMotd() == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.MOTD__NOT_SET));
        // Tell the user their motd
        getCurrentCommandIssuer().sendInfo(Messages.MOTD__MOTD, "{motd}", guild.getMotd());
    }

}