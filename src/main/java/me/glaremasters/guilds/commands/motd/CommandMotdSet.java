package me.glaremasters.guilds.commands.motd;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
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
public class CommandMotdSet extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    // Set the MOTD
    @Subcommand("motd set")
    @Description("{@@descriptions.motd-set}")
    @CommandPermission(Constants.MOTD_PERM + "modify")
    public void execute(Player player, Guild guild, GuildRole role, String motd) {
        if (!role.isModifyMotd())
            ACFUtil.sneaky(new InvalidPermissionException());

        guild.setMotd(ACFBukkitUtil.color(motd));

        getCurrentCommandIssuer().sendInfo(Messages.MOTD__SUCCESS);
    }

}