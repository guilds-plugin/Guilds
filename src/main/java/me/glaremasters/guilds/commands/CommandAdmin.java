package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:45 PM
 */
@CommandAlias("guild|guilds")
public class CommandAdmin extends BaseCommand {

    @Dependency private Guilds guilds;

    @Subcommand("admin remove")
    @Description("Admin command to remove a Guild from the server.")
    @CommandPermission("guilds.command.admin")
    @Syntax("<guild name>")
    public void onGuildRemove(Player player, String name) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_WARNING, "{guild}", name);
        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                guilds.getDatabase().removeGuild(guild);
                guilds.getActionHandler().removeAction(player);
                getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_SUCCESSFUL, "{guild}", name);
            }

            @Override
            public void decline() {
                guilds.getActionHandler().removeAction(player);
                // send message saying guild has not been removed
            }
        });
    }

    @Subcommand("admin addplayer")
    @Description("Admin command to add a player to a Guild")
    @CommandPermission("guilds.command.admin")
    @Syntax("<player> <guild>")
    public void onAdminAddPlayer(Player player, String target, String guild) {
        Player playerToAdd = Bukkit.getPlayerExact(target);
        if (player == null || !player.isOnline()) return;
        if (Guild.getGuild(playerToAdd.getUniqueId()) != null) return;
        Guild targetGuild = Guild.getGuild(guild);
        if (targetGuild == null) return;
        targetGuild.addMember(playerToAdd.getUniqueId(), GuildRole.getLowestRole());
        // send message to player saying they've been added to guild
        // send message to admin saying player has been added to guild
    }

    @Subcommand("admin removeplayer")
    @Description("Admin command to remove a player from a Guild")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name>")
    public void onAdminRemovePlayer(Player player, String target) {
        Player playerToRemove = Bukkit.getPlayerExact(target);
        if (player == null || !player.isOnline()) return;
        if (Guild.getGuild(playerToRemove.getUniqueId()) == null) return;
        Guild guild = Guild.getGuild(playerToRemove.getUniqueId());
        guild.removeMember(playerToRemove.getUniqueId());
        // send message to player saying they've been removed from the guild
        // send message to admin saying player has been removed from the guild
    }

    @Subcommand("admin upgrade")
    @Description("Admin command to upgrade a Guild's tier")
    @CommandPermission("guilds.command.admin")
    @Syntax("<guild name>")
    public void onAdminUpgradeGuild(Player player, String name) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        int tier = guild.getTier();
        if (tier >= guild.getMaxTier()) return;
        guild.updateTier(tier + 1);
        // send message to admin saying guild was upgraded
    }

    @Subcommand("admin status")
    @Description("Admin command to change a Guild's status")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name> <private/public>")
    public void onAdminGuildStatus(Player player, String name, String status) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        if (!status.equalsIgnoreCase("private") && !status.equalsIgnoreCase("public")) {
            // send message saying choose one or other
            return;
        }
        guild.updateStatus(StringUtils.capitalize(status));
        // send message to admin saying the status has been changed
    }

    @Subcommand("admin prefix")
    @Description("Admin command to change a Guild's prefix")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name> <prefix>")
    public void onAdminGuildPrefix(Player player, String name, String prefix) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        guild.updatePrefix(color(prefix));
        // send message to admin saying the prefix has been changed
    }

}
