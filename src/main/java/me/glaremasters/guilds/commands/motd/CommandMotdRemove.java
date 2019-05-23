package me.glaremasters.guilds.commands.motd;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 5/22/2019
 * Time: 11:02 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandMotdRemove extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    // Remove the MOTD
    @Subcommand("motd remove")
    @Description("{@@descriptions.motd-remove}")
    @CommandPermission(Constants.MOTD_PERM + "modify")
    public void execute(Player player, Guild guild, GuildRole role) {
        // Check if role can modify the motd
        if (!role.isModifyMotd())
            ACFUtil.sneaky(new InvalidPermissionException());
        // Remove the motd
        guild.setMotd(null);
        // Tell user they removed the motd
        getCurrentCommandIssuer().sendInfo(Messages.MOTD__REMOVE);
    }

}