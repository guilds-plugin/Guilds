package me.glaremasters.guilds.commands.admin;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
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
 * Time: 11:06 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandAdminMotdSet extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    // Admin set it, add in args
    @Subcommand("admin motd set")
    @Description("{@@descriptions.admin-motd-remove}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    public void execute(Player player, @Single String guild, String motd) {
        // Get the target guild
        Guild targetGuild = guildHandler.getGuild(guild);
        // Check if target guild is null, throw error
        if (targetGuild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        // Set the MOTD of the guild
        targetGuild.setMotd(ACFBukkitUtil.color(motd));
        // Tell the player that they set the motd
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__MOTD_SUCCESS, "{guild}", targetGuild.getName(), "{motd}", targetGuild.getMotd());
    }

}