package me.glaremasters.guilds.commands;

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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ConfirmAction;
import me.glaremasters.guilds.utils.GuildUtils;
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

@SuppressWarnings("unused")
@CommandAlias("guild|guilds")
public class CommandAdmin extends BaseCommand {

    private Guilds guilds;
    private GuildHandler guildHandler;
    private GuildUtils utils;

    public CommandAdmin(GuildUtils utils) {
        this.utils = utils;
    }

    /**
     * Admin command to remove a guild from the server
     * @param player the admin running the command
     * @param name the name of the guild being removed
     */
    @Subcommand("admin remove")
    @Description("{@@descriptions.admin-remove}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onGuildRemove(Player player, @Values("@guilds") @Single String name) {
        Guild guild = utils.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_WARNING, "{guild}", name);
        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.ADMIN_DELETED);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                guilds.getVaults().remove(guild);
                Guilds.checkForClaim(player.getWorld(), guild, guilds);
                utils.removeGuildPerms(guild);
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
    @CommandCompletion("@online @guilds")
    @Syntax("<player> <guild>")
    public void onAdminAddPlayer(Player player, @Values("@online") @Single String target, @Values("@guilds") @Single String guild) {
        OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(target);
        if (player == null || !player.isOnline()) return;
        if (utils.getGuild(playerToAdd.getUniqueId()) != null) return;
        Guild targetGuild = utils.getGuild(guild);
        if (targetGuild == null) return;
        utils.addMember(targetGuild, playerToAdd.getUniqueId(), GuildRole.getLowestRole());
        utils.addGuildPerms(targetGuild, playerToAdd);
        if (playerToAdd.isOnline()) {
            guilds.getManager().getCommandIssuer(playerToAdd).sendInfo(Messages.ADMIN__PLAYER_ADDED, "{guild}", targetGuild.getName());
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED, "{player}", playerToAdd.getName(), "{guild}", targetGuild.getName());
        utils.sendMessage(targetGuild, Messages.ADMIN__ADMIN_GUILD_ADD, "{player}", playerToAdd.getName());
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
        OfflinePlayer playerToRemove = Bukkit.getPlayerExact(target);
        if (player == null) return;
        if (utils.getGuild(playerToRemove.getUniqueId()) == null) return;
        Guild guild = utils.getGuild(playerToRemove.getUniqueId());
        utils.removeGuildPerms(guild, playerToRemove);
        utils.removeMember(guild, playerToRemove.getUniqueId());
        if (playerToRemove.isOnline()) {
            guilds.getManager().getCommandIssuer(playerToRemove).sendInfo(Messages.ADMIN__PLAYER_REMOVED, "{guild}", guild.getName());
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED, "{player}", playerToRemove.getName(), "{guild}", guild.getName());
        utils.sendMessage(guild, Messages.ADMIN__ADMIN_GUILD_REMOVE, "{player}", playerToRemove.getName());
    }

    /**
     * Admin command to upgrade a guild's tier
     * @param player the admin running the command
     * @param name the name of the guild being upgraded
     */
    @Subcommand("admin upgrade")
    @Description("{@@descriptions.admin-upgrade}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onAdminUpgradeGuild(Player player, @Values("@guilds") @Single String name) {
        Guild guild = utils.getGuild(name);
        if (guild == null) return;
        int tier = guild.getTier();
        if (tier >= utils.getMaxTier()) return;
        guild.setTier(tier + 1);
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_UPGRADE, "{guild}", guild.getName());
        utils.sendMessage(guild, Messages.ADMIN__ADMIN_GUILD_UPGRADE);
    }

    /**
     * Admin command to change a guild's status
     * @param player the admin running the command
     * @param name the guild to change the status of
     */
    @Subcommand("admin status")
    @Description("{@@descriptions.admin-status}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<name> <private/public>")
    public void onAdminGuildStatus(Player player, @Values("@guilds") @Single String name) {
        Guild guild = utils.getGuild(name);
        if (guild == null) return;
        String status = guild.getStatus();
        if (status.equalsIgnoreCase("private")) {
            status = "Public";
        } else {
            status = "Private";
        }
        guild.setStatus(StringUtils.capitalize(status));
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
    @CommandCompletion("@guilds")
    @Syntax("<name> <prefix>")
    public void onAdminGuildPrefix(Player player, @Values("@guilds") @Single String name, String prefix) {
        Guild guild = utils.getGuild(name);
        if (guild == null) return;
        guild.setPrefix(color(prefix));
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
    @CommandCompletion("@guilds")
    @Syntax("<name> <new name>")
    public void onAdminGuildRename(Player player, @Values("@guilds") @Single String name, String newName) {
        Guild guild = utils.getGuild(name);
        if (guild == null) return;
        String oldName = guild.getName();
        guilds.getDatabase().removeGuild(utils.getGuild(oldName));
        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", newName);
        guild.setName(color(name));
    }

    /**
     * Admin command to turn on Guild Chat Spy
     * @param player the player executing the command
     */
    @Subcommand("admin spy")
    @Description("{@@descriptions.admin-spy}")
    @CommandPermission("guilds.command.admin")
    public void onAdminSpy(Player player) {
        if (guilds.getSpy().contains(player)) {
            guilds.getSpy().remove(player);
            getCurrentCommandIssuer().sendInfo(Messages.ADMIN__SPY_OFF);
        } else {
            guilds.getSpy().add(player);
            getCurrentCommandIssuer().sendInfo(Messages.ADMIN__SPY_ON);
        }
    }

}
