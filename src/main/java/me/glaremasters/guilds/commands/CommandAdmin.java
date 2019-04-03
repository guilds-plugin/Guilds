/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.commands;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

//todo this todo is for all command classes.
//I have noticed that you only send certain messages on certain commands however these messages should also be sent in other places.
//Example I have seen is that you sent a ERROR__GUILD_NO_EXIST message in this admin command but not in the ally commands (haven't checked all commands classes)
//I've also noticed a lot of differences between Bukkit.getOfflinePlayer(String) and Bukkit.getPlayerExact(String) and Bukkit.getPlayer(String)
//You should really only be using the first one or the last 2 ones.

@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandAdmin extends BaseCommand {

    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private SettingsManager settingsManager;
    private final List<Player> spies = new ArrayList<>();

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
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_WARNING, "{guild}", name);
        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.ADMIN_DELETED);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                // check if they have a claim

                guildHandler.removeGuild(guild);

                getCurrentCommandIssuer().sendInfo(Messages.ADMIN__DELETE_SUCCESS, "{guild}", name);

                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                actionHandler.removeAction(player);
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

        //todo guildHandler.addGuildPerms(guild, playerToAdd);

        if (playerToAdd.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(playerToAdd).sendInfo(Messages.ADMIN__PLAYER_ADDED, "{guild}", guild.getName());
        }
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_ADDED, "{player}", playerToAdd.getName(), "{guild}", guild.getName());
        guild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_ADD, "{player}", playerToAdd.getName());
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
        OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(target);
        if (player == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND);
            return;
        }

        Guild guild = guildHandler.getGuild(playerToRemove);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        guild.removeMember(playerToRemove);

        //todo guildHandler.removeGuildPerms(guild, playerToRemove);

        if (playerToRemove.isOnline()) getCurrentCommandManager().getCommandIssuer(playerToRemove).sendInfo(Messages.ADMIN__PLAYER_REMOVED, "{guild}", guild.getName());
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_PLAYER_REMOVED, "{player}", playerToRemove.getName(), "{guild}", guild.getName());
        guild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_REMOVE, "{player}", playerToRemove.getName());
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
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        GuildTier tier = guild.getTier();
        if (tier.getLevel() >= guildHandler.getMaxTierLevel()) return;

        guild.setTier(guildHandler.getGuildTier(guild.getTier().getLevel() + 1));

        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__ADMIN_UPGRADE, "{guild}", guild.getName());
        guild.sendMessage(getCurrentCommandManager(), Messages.ADMIN__ADMIN_GUILD_UPGRADE);
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
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

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
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        guild.setPrefix(StringUtils.color(prefix));

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
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        guild.setName(StringUtils.color(newName));

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

    @Subcommand("admin vault")
    @Description("{@@descriptions.admin-vault}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<guild> <vault #>")
    public void onAdminVault(Player player, @Values("@guilds") @Single String name, Integer vault) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }
        Inventory inv = guildHandler.getGuildVault(guild, vault);
        if (inv != null) player.openInventory(inv);

    }

    /**
     * Give a player upgrade tickets
     * @param sender the executor of this command
     * @param player the player receiving the tickets
     * @param amount amount of tickets
     */
    @Subcommand("give")
    @Description("{@@descriptions.give}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<player> <amount>")
    public void onTicketGive(CommandSender sender, Player player, @Default("1") Integer amount) {
        if (player == null) return;

        /* todo add back in the config @Glare
        String ticketName = getString("upgrade-ticket.name");
        String ticketMaterial = getString("upgrade-ticket.material");
        String ticketLore = getString("upgrade-ticket.lore");

        ItemStack upgradeTicket = new ItemStack(Material.matchMaterial(settingsManager.getProperty()), amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ticketLore);
        meta.setDisplayName(ticketName);
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
        */
    }

    /**
     * Reload the config
     */
    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission("guilds.command.admin")
    public void onReload() {
        settingsManager.reload();
        getCurrentCommandIssuer().sendInfo(Messages.RELOAD__RELOADED);
    }

}
