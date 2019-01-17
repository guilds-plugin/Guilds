package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.*;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

import static co.aikar.commands.ACFBukkitUtil.color;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:45 PM
 */

@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandAdmin extends BaseCommand {

    private GuildHandler guildHandler;
    private List<Player> spies;

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
     * @param name the guild the player is being added to
     */
    @Subcommand("admin addplayer")
    @Description("{@@descriptions.admin-addplayer}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@online @guilds")
    @Syntax("<player> <guild>")
    public void onAdminAddPlayer(Player player, @Values("@online") @Single String target, @Values("@guilds") @Single String name) {
        OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(target);
        if (player == null) return;

        Guild guild = guildHandler.getGuild(name);
        if (guild == null) return;

        guild.addMember(new GuildMember(playerToAdd.getUniqueId(), guildHandler.getLowestGuildRole()));
        //todo
        guildHandler.addGuildPerms(guild, playerToAdd);

        if (playerToAdd.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(playerToAdd).sendInfo(Messages.ADMIN__PLAYER_ADDED, "{guild}", guild.getName());
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED, "{player}", playerToAdd.getName(), "{guild}", guild.getName());
        guildHandler.sendMessage(guild, Messages.ADMIN__ADMIN_GUILD_ADD, "{player}", playerToAdd.getName());
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

        Guild guild = guildHandler.getGuild(playerToRemove);
        if (guild == null) return;

        guild.removeMember(playerToRemove);

        //todo
        guildHandler.removeGuildPerms(guild, playerToRemove);

        if (playerToRemove.isOnline()) getCurrentCommandManager().getCommandIssuer(playerToRemove).sendInfo(Messages.ADMIN__PLAYER_REMOVED, "{guild}", guild.getName());
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED, "{player}", playerToRemove.getName(), "{guild}", guild.getName());
        guildHandler.sendMessage(guild, Messages.ADMIN__ADMIN_GUILD_REMOVE, "{player}", playerToRemove.getName());
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
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) return;

        GuildTier tier = guild.getTier();
        if (tier.getLevel() >= guildHandler.getMaxTierLevel()) return;

        guild.setTier(guildHandler.getGuildTier(guild.getTier().getLevel() + 1));

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_UPGRADE, "{guild}", guild.getName());
        guildHandler.sendMessage(guild, Messages.ADMIN__ADMIN_GUILD_UPGRADE);
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
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) return;

        Guild.Status status = guild.getStatus();
        if (status == Guild.Status.Private) guild.setStatus(Guild.Status.Public);
        else guild.setStatus(Guild.Status.Private);

        getCurrentCommandIssuer().sendInfo(Messages.STATUS__SUCCESSFUL, "{status}", status.name());
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
        Guild guild = guildHandler.getGuild(name);
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
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) return;

        guild.setName(color(newName));

        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", newName);
    }

    /**
     * Admin command to turn on Guild Chat Spy
     * @param player the player executing the command
     */
    @Subcommand("admin spy")
    @Description("{@@descriptions.admin-spy}")
    @CommandPermission("guilds.command.admin")
    public void onAdminSpy(Player player) {
        if (spies.contains(player)) {
            spies.remove(player);
            getCurrentCommandIssuer().sendInfo(Messages.ADMIN__SPY_OFF);
        } else {
            spies.add(player);
            getCurrentCommandIssuer().sendInfo(Messages.ADMIN__SPY_ON);
        }
    }

}
