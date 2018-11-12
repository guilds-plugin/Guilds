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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import static co.aikar.commands.ACFBukkitUtil.color;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:45 PM
 */
@CommandAlias("guild|guilds")
public class CommandAdmin extends BaseCommand {

    @Dependency private Guilds guilds;

    /**
     * Admin command to remove a guild from the server
     * @param player the admin running the command
     * @param name the name of the guild being removed
     */
    @Subcommand("admin remove")
    @Description("{@@descriptions.admin-remove}")
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
            }
        });
    }

    /**
     * Admin command to add a player to a guild
     * @param player the admin running the command
     * @param target the player being added to the guild
     * @param guild the guild the player is being added to
     */
    @Subcommand("admin addplayer")
    @Description("{@@descriptions.admin-addplayer}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<player> <guild>")
    public void onAdminAddPlayer(Player player, String target, String guild) {
        OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(target);
        if (player == null || !player.isOnline()) return;
        if (Guild.getGuild(playerToAdd.getUniqueId()) != null) return;
        Guild targetGuild = Guild.getGuild(guild);
        if (targetGuild == null) return;
        targetGuild.addMember(playerToAdd.getUniqueId(), GuildRole.getLowestRole());
        if (playerToAdd.isOnline()) {
            guilds.getManager().getCommandIssuer(playerToAdd).sendInfo(Messages.ADMIN__PLAYER_ADDED, "{guild}", targetGuild.getName());
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED, "{player}", playerToAdd.getName(), "{guild}", targetGuild.getName());
        targetGuild.sendMessage(Messages.ADMIN__ADMIN_GUILD_ADD, "{player}", playerToAdd.getName());
    }

    /**
     * Admin command to remove a player from a guild
     * @param player the admin running the command
     * @param target the guild the player is being removed from
     */
    @Subcommand("admin removeplayer")
    @Description("{@@descriptions.admin-removeplayer}")
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

    /**
     * Admin command to upgrade a guild's tier
     * @param player the admin running the command
     * @param name the name of the guild being upgraded
     */
    @Subcommand("admin upgrade")
    @Description("{@@descriptions.admin-upgrade}")
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

    /**
     * Admin command to change a guild's status
     * @param player the admin running the command
     * @param name the guild to change the status of
     */
    @Subcommand("admin status")
    @Description("{@@descriptions.admin-status}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name> <private/public>")
    public void onAdminGuildStatus(Player player, String name) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        String status = guild.getStatus();
        if (status.equalsIgnoreCase("private")) {
            status = "Public";
        } else {
            status = "Private";
        }
        guild.updateStatus(StringUtils.capitalize(status));
        getCurrentCommandIssuer().sendInfo(Messages.STATUS__SUCCESSFUL, "{status}", status);
    }

    /**
     * Admin command to change the prefix of a guild
     * @param player the admin running the command
     * @param name the name of a guild
     * @param prefix the new prefix of the guild
     */
    @Subcommand("admin prefix")
    @Description("{@@descriptions.admin-prefix}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name> <prefix>")
    public void onAdminGuildPrefix(Player player, String name, String prefix) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        guild.updatePrefix(color(prefix));
        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL);
    }

    /**
     * Admin command to rename a guild
     * @param player the admin running the command
     * @param name the name of the guild
     * @param newName the new name of the guild
     */
    @Subcommand("admin rename")
    @Description("{@@descriptions.admin-prefix}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<name> <new name>")
    public void onAdminGuildRename(Player player, String name, String newName) {
        Guild guild = Guild.getGuild(name);
        if (guild == null) return;
        String oldName = guild.getName();
        guilds.getDatabase().removeGuild(Guild.getGuild(oldName));
        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", newName);
        guild.updateName(color(name));
    }

}
