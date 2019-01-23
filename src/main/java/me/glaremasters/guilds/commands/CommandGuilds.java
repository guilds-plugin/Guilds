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
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.*;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.*;
import me.glaremasters.guilds.configuration.CooldownSettings;
import me.glaremasters.guilds.configuration.CostSettings;
import me.glaremasters.guilds.configuration.GuiSettings;
import me.glaremasters.guilds.configuration.GuildSettings;
import me.glaremasters.guilds.configuration.TierSettings;
import me.glaremasters.guilds.guild.*;
import me.glaremasters.guilds.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

//todo rewrite lol -> this has been rewritten mostly there are still quite a lot of todos due to things being unclear
// or not being added yet, make sure to fix these todos and commented out code and then we can start cleaning the warnings.
// xx lemmo.
@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandGuilds extends BaseCommand {

    private GuildHandler guildHandler;
    public final static Inventory guildList = null;
    public final static Map<UUID, Integer> playerPages = new HashMap<>();
    //todo give me explanation pls @Glare
    public final List<Player> home = new ArrayList<>();
    public final List<Player> setHome = new ArrayList<>();
    public final Map<Player, Location> warmUp = new HashMap<>();
    private SettingsManager settingsManager;
    private ActionHandler actionHandler;
    private Economy economy;

    /**
     * Create a guild
     * @param player the player executing this command
     * @param name name of guild
     * @param prefix prefix of guild
     */
    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission("guilds.command.create")
    @Syntax("<name> (optional) <prefix>")
    public void onCreate(Player player, String name, @Optional String prefix) {
        if (guildHandler.getGuild(player) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        if (nameMeetsRequirements(name)) return;

        final String newPrefix;
        if (prefix == null || !prefix.matches(settingsManager.getProperty(GuildSettings.PREFIX_REQUIREMENTS))) {
            newPrefix = StringUtils.color(name);
        } else newPrefix = StringUtils.color(prefix);

        double creationCost = settingsManager.getProperty(CostSettings.CREATION);

        if (economy.getBalance(player) < creationCost) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.CREATE__WARNING, "{amount}", String.valueOf(creationCost));

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (economy.getBalance(player) < creationCost) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
                    return;
                }

                economy.withdrawPlayer(player, creationCost);

                Guild.GuildBuilder gb = Guild.builder();
                gb.id(UUID.randomUUID());
                gb.name(StringUtils.color(name));
                gb.prefix(newPrefix);

                gb.status(Guild.Status.Private);

                ItemStack masterHead = new ItemStack(Material.SKULL_ITEM, 1);
                SkullMeta masterHeadMeta = (SkullMeta) masterHead.getItemMeta();
                masterHeadMeta.setOwningPlayer(player);
                masterHeadMeta.setDisplayName(StringUtils.color(name));
                masterHead.setItemMeta(masterHeadMeta);
                gb.masterHead(masterHead);

                GuildMember guildMaster = new GuildMember(player.getUniqueId(), guildHandler.getGuildRole(0));
                gb.guildMaster(guildMaster);

                List<GuildMember> members = new ArrayList<>();
                members.add(guildMaster);
                gb.members(members);

                gb.tier(guildHandler.getGuildTier(1));

                //todo gb.inventory(Bukkit.createInventory()) (vault)

                Guild guild = gb.build();

                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                guildHandler.addGuild(guild);

                getCurrentCommandIssuer().sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.getName());

                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });
    }

    /**
     * Confirm an action
     * @param player the player confirming this
     */
    @Subcommand("confirm")
    @Description("{@@descriptions.confirm}")
    @CommandPermission("guilds.command.confirm")
    public void onConfirm(Player player) {
        ConfirmAction action = actionHandler.getAction(player);
        if (action == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CONFIRM__ERROR);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.CONFIRM__SUCCESS);
        action.accept();
    }

    /**
     * Cancel an action
     * @param player the player cancelling this
     */
    @Subcommand("cancel")
    @Description("{@@descriptions.cancel}")
    @CommandPermission("guilds.command.cancel")
    public void onCancel(Player player) {
        ConfirmAction action = actionHandler.getAction(player);
        if (action == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CANCEL__ERROR);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.CANCEL__SUCCESS);
            action.decline();
        }
    }

    /**
     * Reload the config
     */
    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission("guilds.command.reload")
    public void onReload() {
        settingsManager.reload();
        getCurrentCommandIssuer().sendInfo(Messages.RELOAD__RELOADED);
    }

    /**
     * Set a guild home
     * @param player the player setting the home
     * @param guild the guild that home is being set
     * @param role role of player
     */
    @Subcommand("sethome")
    @Description("{@@descriptions.sethome}")
    @CommandPermission("guilds.command.sethome")
    public void onSetHome(Player player, Guild guild, GuildRole role) {
        if (!role.isChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (economy.getBalance(player) < settingsManager.getProperty(CostSettings.SETHOME)) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
            return;
        }

        //todo
        if (setHome.contains(player)) {
            getCurrentCommandIssuer().sendInfo(Messages.SETHOME__COOLDOWN, "{amount}", String.valueOf(CooldownSettings.SETHOME));
            return;
        }

        guild.setHome(player.getLocation());

        economy.withdrawPlayer(player, settingsManager.getProperty(CostSettings.SETHOME));
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);

        /* todo
        setHome.add(player);
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(guilds, () -> setHome.remove(player), (20 * getInt("cooldowns.sethome")));
        */
    }

    /**
     * Remove a guild home
     * @param player the player removing the guild home
     * @param guild the guild that the home is being removed
     * @param role role of player
     */
    @Subcommand("delhome")
    @Description("{@@descriptions.delhome}")
    @CommandPermission("guilds.command.delhome")
    public void onDelHome(Player player, Guild guild, GuildRole role) {
        if (!role.isChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.setHome(null);
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
    }

    /**
     * Give a player upgrade tickets
     * @param sender the executor of this command
     * @param player the player receiving the tickets
     * @param amount amount of tickets
     */
    @Subcommand("give")
    @Description("{@@descriptions.give}")
    @CommandPermission("guilds.command.give")
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
     * Go to guild home
     * @param player the player teleporting
     * @param guild the guild to teleport to
     */
    @Subcommand("home")
    @Description("{@@descriptions.home}")
    @CommandPermission("guilds.command.home")
    public void onHome(Player player, Guild guild) {
        if (guild.getHome() == null) {
            getCurrentCommandIssuer().sendInfo(Messages.HOME__NO_HOME_SET);
            return;
        }

        //todo
        if (home.contains(player)) {
            getCurrentCommandIssuer().sendInfo(Messages.HOME__COOLDOWN, "{amount}", String.valueOf(settingsManager.getProperty(CooldownSettings.HOME)));
            return;
        }

        warmUp.put(player, player.getLocation());

        getCurrentCommandIssuer().sendInfo(Messages.HOME__WARMUP, "{amount}", String.valueOf(settingsManager.getProperty(CooldownSettings.WU_HOME)));

        //todo
        Bukkit.getServer().getScheduler().runTaskLater(null, () -> {

            if (warmUp.get(player).distance(player.getLocation()) > 1) {
                getCurrentCommandIssuer().sendInfo(Messages.HOME__CANCELLED);
                warmUp.remove(player);
                return;
            }

            player.teleport(guild.getHome());
            warmUp.remove(player);

            getCurrentCommandIssuer().sendInfo(Messages.HOME__TELEPORTED);

            home.add(player);
            //todo Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(guilds, () -> home.remove(player), (20 * getInt("cooldowns.home")));
        }, (20 * settingsManager.getProperty(CooldownSettings.WU_HOME)));
    }

    /**
     * Rename a guild
     * @param player the player renaming this guild
     * @param guild the guild being renamed
     * @param role the role of the player
     * @param name new name of guild
     */
    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission("guilds.command.rename")
    @Syntax("<name>")
    public void onRename(Player player, Guild guild, GuildRole role, String name) {
        if (!role.isChangeName()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (nameMeetsRequirements(name)) return;

        guild.setName(StringUtils.color(name));
        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name);
    }

    /**
     * Toggles Guild Chat
     * @param player the player toggling chat
     * @param guild the guild the player is from
     * @param role the role the player has
     */
    @Subcommand("chat")
    @Description("{@@descriptions.chat}")
    @CommandPermission("guilds.command.chat")
    public void onGuildChat(Player player, Guild guild, GuildRole role) {

        if (!role.isChat()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        //todo rewrite.
        /*
        if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {
            GUILD_CHAT_PLAYERS.remove(player.getUniqueId());
            getCurrentCommandIssuer().sendInfo(Messages.CHAT__DISABLED);
        } else {
            GUILD_CHAT_PLAYERS.add(player.getUniqueId());
            getCurrentCommandIssuer().sendInfo(Messages.CHAT__ENABLED);
        }
         */

    }

    /**
     * Toggles Guild Status
     * @param player the player toggling guild status
     * @param guild the guild that is getting toggled
     * @param role the player's role
     */
    @Subcommand("status")
    @Description("{@@descriptions.status}")
    @CommandPermission("guilds.command.status")
    public void onStatus(Player player, Guild guild, GuildRole role) {
        if (!role.isChangeStatus()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }


        if (guild.getStatus() == Guild.Status.Private) guild.setStatus(Guild.Status.Public);
        else guild.setStatus(Guild.Status.Private);

        getCurrentCommandIssuer().sendInfo(Messages.STATUS__SUCCESSFUL, "{status}", guild.getStatus().name());
    }

    /**
     * Change guild prefix
     * @param player the player changing the guild prefix
     * @param guild the guild which's prefix is getting changed
     * @param role the role of the player
     * @param prefix the new prefix
     */
    @Subcommand("prefix")
    @Description("{@@descriptions.prefix}")
    @CommandPermission("guilds.command.prefix")
    @Syntax("<prefix>")
    public void onPrefix(Player player, Guild guild, GuildRole role, String prefix) {
        if (!role.isChangePrefix()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (!prefix.matches(settingsManager.getProperty(GuildSettings.PREFIX_REQUIREMENTS))) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL, "{prefix}", prefix);
        guild.setPrefix(StringUtils.color(prefix));
    }

    // todo I feel like this is not very necessary, if we decide to keep this in we'll need to pass on the spigot updater instance.
//    /**
//     * Check for an update
//     * @param sender the executor of this command
//     */
//    @Subcommand("version|v|ver")
//    @Description("{@@descriptions.version}")
//    public void onVersion(CommandSender sender) {
//        SpigotUpdater updater = new SpigotUpdater(guilds, 48920);
//        PluginDescriptionFile pdf = guilds.getDescription();
//        try {
//            String message;
//            if (updater.getLatestVersion().equalsIgnoreCase(pdf.getVersion())) {
//                message = "";
//            } else {
//                message = "\n&8» &7An update has been found! &f- " + updater.getResourceLink();
//            }
//            sender.sendMessage(
//                    StringUtils.color("&8&m--------------------------------------------------"
//                            + "\n&8» &7Name - &a"
//                            + pdf.getName() + "\n&8» &7Version - &a" + pdf.getVersion()
//                            + "\n&8» &7Author - &a" + pdf.getAuthors() + "\n&8» &7Support - &a"
//                            + pdf.getWebsite() + message
//                            + "\n&8&m--------------------------------------------------"));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    /**
     * Invite player to guild
     * @param player current player
     * @param targetPlayer player being invited
     * @param guild the guild that the targetPlayer is being invited to
     * @param role the role of the player
     */
    @Subcommand("invite")
    @Description("{@@descriptions.invite}")
    @CommandPermission("guilds.command.invite")
    @CommandCompletion("@online")
    @Syntax("<name>")
    public void onInvite(Player player, Guild guild, GuildRole role, @Values("@online") @Single String targetPlayer) {

        if (!role.isInvite()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Player target = Bukkit.getPlayerExact(targetPlayer);

        if (target == null || !target.isOnline()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", targetPlayer);
            return;
        }

        if (guildHandler.getGuild(target) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        if (guild.getInvitedMembers().contains(target.getUniqueId())) {
            getCurrentCommandIssuer().sendInfo(Messages.INVITE__ALREADY_INVITED);
            return;
        }

        GuildInviteEvent event = new GuildInviteEvent(player, guild, target);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.inviteMember(target.getUniqueId());

        getCurrentCommandManager().getCommandIssuer(target).sendInfo(Messages.INVITE__MESSAGE, "{player}", player.getName(), "{guild}", guild.getName());
        getCurrentCommandIssuer().sendInfo(Messages.INVITE__SUCCESSFUL, "{player}", target.getName());

    }

    /**
     * Upgrade a guild
     * @param player the command executor
     * @param guild the guild being upgraded
     * @param role the player's role
     */
    @Subcommand("upgrade")
    @Description("{@@descriptions.upgrade}")
    @CommandPermission("guilds.command.upgrade")
    public void onUpgrade(Player player, Guild guild, GuildRole role) {
        if (!role.isUpgradeGuild()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        GuildTier tier = guild.getTier();

        if (tier.getLevel() >= guildHandler.getMaxTierLevel()) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__TIER_MAX);
            return;
        }

        if (tier.getMembersToRankup() != 0 && guild.getMembers().size() < tier.getMembersToRankup()) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MEMBERS, "{amount}", String.valueOf(tier.getMembersToRankup()));
            return;
        }

        double balance = guild.getBalance();
        double upgradeCost = tier.getCost();

        if (balance < upgradeCost) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance));
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__MONEY_WARNING, "{amount}", String.valueOf(upgradeCost));

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (balance < upgradeCost) {
                    getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance));
                    return;
                }

                guild.setBalance(balance - upgradeCost);

                //todo perms
                //if (!settingsManager.getProperty(TierSettings.CARRY_OVER)) guildHandler.removeGuildPerms(guild);

                guildHandler.upgradeTier(guild);

                //todo why is this delayed? @Glare
                //Bukkit.getScheduler().scheduleSyncDelayedTask(guilds, () -> guildHandler.addGuildPerms(guild), 60L);

                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__SUCCESS);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__CANCEL);
                actionHandler.removeAction(player);
            }
        });
    }

    /**
     * Transfer a guild to a new user
     * @param player the player transferring this guild
     * @param guild the guild being transferred
     * @param role the role of the player
     * @param target the new guild master
     */
    @Subcommand("transfer")
    @Description("{@@descriptions.transfer}")
    @CommandPermission("guilds.command.transfer")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void onTransfer(Player player, Guild guild, GuildRole role, @Values("@members") @Single String target) {
        if (!role.isTransferGuild()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Player transferPlayer = Bukkit.getPlayerExact(target);
        if (transferPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND);
            return;
        }

        GuildMember oldMaster = guild.getGuildMaster();
        GuildMember newMaster = guild.getMember(transferPlayer.getUniqueId());

        if (newMaster == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD);
            return;
        }
        if (newMaster.getRole().getLevel() != 1) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_OFFICER);
            return;
        }

        guild.setGuildMaster(newMaster);
        GuildRole oldRole = oldMaster.getRole();
        oldMaster.setRole(newMaster.getRole());
        newMaster.setRole(oldRole);

        getCurrentCommandIssuer().sendInfo(Messages.TRANSFER__SUCCESS);
        getCurrentCommandManager().getCommandIssuer(transferPlayer).sendInfo(Messages.TRANSFER__NEWMASTER);
    }

    /**
     * Leave a guild
     * @param player the player leaving the guild
     * @param guild the guild being left
     */
    @Subcommand("leave|exit")
    @Description("{@@descriptions.leave}")
    @CommandPermission("guilds.command.leave")
    public void onLeave(Player player, Guild guild) {

        if (guild.getGuildMaster().getUuid().equals(player.getUniqueId())) {
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING_GUILDMASTER);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING);
        }

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildLeaveEvent event = new GuildLeaveEvent(player, guild);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                if (guild.getGuildMaster().getUuid().equals(player.getUniqueId())) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.MASTER_LEFT);
                    Bukkit.getPluginManager().callEvent(removeEvent);
                    if (removeEvent.isCancelled()) return;

                    guildHandler.removeGuild(guild);

                    guildHandler.sendMessage(guild, Messages.LEAVE__GUILDMASTER_LEFT, "{player}", player.getName());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                } else {
                    guild.removeMember(player);

                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    guildHandler.sendMessage(guild, Messages.LEAVE__PLAYER_LEFT, "{player}", player.getName());
                }

                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });
    }

    /**
     * List all the guilds on the server
     * @param player the player executing this command
     */
    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @CommandPermission("guilds.command.list")
    public void onGuildList(Player player) {
        //todo after explanation waiting for @Glare
        playerPages.put(player.getUniqueId(), 1);
        // guildList = getSkullsPage(1);
        player.openInventory(guildList);
    }

    /**
     * Delete your guild
     * @param player the player deleting the guild
     * @param guild the guild being deleted
     * @param role the role of the player
     */
    @Subcommand("delete")
    @Description("{@@descriptions.delete}")
    @CommandPermission("guilds.command.delete")
    public void onDelete(Player player, Guild guild, GuildRole role) {
        if (!role.isRemoveGuild()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.DELETE__WARNING);

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.PLAYER_DELETED);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                guildHandler.removeGuild(guild);

                getCurrentCommandIssuer().sendInfo(Messages.DELETE__SUCCESSFUL, "{guild}", guild.getName());
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.DELETE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });
    }

    /**
     * Decline a guild invite
     * @param player the player declining the invite
     * @param name the name of the guild
     */
    @Subcommand("decline")
    @Description("{@@descriptions.decline}")
    @CommandPermission("guilds.command.decline")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void onDecline(Player player, @Values("@invitedTo") @Single String name) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        if (guildHandler.getGuild(player) != null) return;
        if (guild.getInvitedMembers().contains(player.getUniqueId())) return;

        guild.removeInvitedMember(player.getUniqueId());

        getCurrentCommandIssuer().sendInfo(Messages.DECLINE__SUCCESS);
    }

    /**
     * Kick a player from the guild
     * @param player the player executing the command
     * @param guild the guild the targetPlayer is being kicked from
     * @param role the role of the player
     * @param name the name of the targetPlayer
     */
    @Subcommand("boot|kick")
    @Description("Kick someone from your Guild")
    @CommandPermission("guilds.command.boot")
    @CommandCompletion("@members")
    @Syntax("<name>")
    public void onKick(Player player, Guild guild, GuildRole role, @Values("@members") @Single String name) {
        if (!role.isKick()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(name);
        if (bootedPlayer == null) return;

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());
        if (kickedPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", name);
            return;
        }

        if (kickedPlayer.getUuid().equals(guild.getGuildMaster().getUuid())) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.removeMember(kickedPlayer);

        getCurrentCommandIssuer().sendInfo(Messages.BOOT__SUCCESSFUL, "{player}", bootedPlayer.getName());
        guildHandler.sendMessage(guild, Messages.BOOT__PLAYER_KICKED, "{player}", bootedPlayer.getName(), "{kicker}", player.getName());
        if (bootedPlayer.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(bootedPlayer).sendInfo(Messages.BOOT__KICKED, "{kicker}", player.getName());
        }
    }

    /**
     * Opens the guild vault
     * @param player the player opening the vault
     * @param guild the guild's vault which's being opened
     * @param role the role of the player
     */
    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @CommandPermission("guilds.command.vault")
    public void onVault(Player player, Guild guild, GuildRole role) {
        if (!role.isOpenVault()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        player.openInventory(guild.getInventory());
    }

    /**
     * Demote a player in a guild
     * @param player the person running the command
     * @param target the player you want to demote
     * @param guild check player is in a guild
     * @param role check player can demote another player
     */
    @Subcommand("demote")
    @Description("{@@descriptions.demote}")
    @CommandPermission("guilds.command.demote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void onDemote(Player player, @Values("@members") @Single String target, Guild guild, GuildRole role) {
        if (!role.isDemote()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        //todo duplicate

        OfflinePlayer demotedPlayer = Bukkit.getOfflinePlayer(target);

        if (demotedPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", target);
            return;
        }

        GuildMember demotedMember = guild.getMember(demotedPlayer.getUniqueId());

        if (demotedMember == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", target);
            return;
        }

        if (demotedMember.getRole().getLevel() == 3 || demotedMember.getRole().getLevel() == 0) {
            getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__CANT_DEMOTE);
            return;
        }

        GuildRole demotedRole = guildHandler.getGuildRole(demotedMember.getRole().getLevel() + 1);
        String oldRank = demotedMember.getRole().getName();
        String newRank = demotedRole.getName();

        demotedMember.setRole(demotedRole);

        getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__DEMOTE_SUCCESSFUL, "{player}", demotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (demotedPlayer.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(demotedPlayer).sendInfo(Messages.DEMOTE__YOU_WERE_DEMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    /**
     * Promote a player in the guild
     * @param player the player executing the command
     * @param target the player being promoted yay
     * @param guild the guild which's member is being promoted
     * @param role the role of the player promoting
     */
    @Subcommand("promote")
    @Description("{@@descriptions.promote}")
    @CommandPermission("guilds.command.promote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void onPromote(Player player, @Values("@members") @Single String target, Guild guild, GuildRole role) {
        if (!role.isPromote()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        OfflinePlayer promotedPlayer = Bukkit.getOfflinePlayer(target);

        if (promotedPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", target);
            return;
        }

        GuildMember promotedMember = guild.getMember(promotedPlayer.getUniqueId());

        if (promotedMember == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", target);
            return;
        }

        if (promotedMember.getRole().getLevel() <= 1) {
            getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__CANT_PROMOTE);
            return;
        }

        GuildRole promotedRole = guildHandler.getGuildRole(promotedMember.getRole().getLevel() - 1);
        String oldRank = promotedMember.getRole().getName();
        String newRank = promotedRole.getName();

        promotedMember.setRole(promotedRole);

        getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__PROMOTE_SUCCESSFUL, "{player}", promotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (promotedPlayer.isOnline()) {
            getCurrentCommandManager().getCommandIssuer(promotedPlayer).sendInfo(Messages.PROMOTE__YOU_WERE_PROMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    /**
     * Accept a guild invite
     * @param player the player accepting the invite
     * @param name the name of the guild being accepted
     */
    @Subcommand("accept|join")
    @Description("{@@descriptions.accept}")
    @CommandPermission("guilds.command.accept")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void onAccept(Player player, @Values("@invitedTo") @Single String name) {
        if (guildHandler.getGuild(player) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        // what the fuck did he do. he wayyy too high lol.

        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        if (!guild.getInvitedMembers().contains(player.getUniqueId()) && guild.getStatus() == Guild.Status.Private) {
            getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__NOT_INVITED);
            return;
        }

        if (guild.getSize() >= guild.getTier().getMaxMembers()) {
            getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.addMember(new GuildMember(player.getUniqueId(), guildHandler.getLowestGuildRole()));

        guildHandler.sendMessage(guild, Messages.ACCEPT__PLAYER_JOINED, "{player}", player.getName());
        getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__SUCCESSFUL, "{guild}", guild.getName());
    }

    /**
     * Check if you have any guild invites
     * @param player the player checking for invites
     */
    @Subcommand("check")
    @Description("{@@descriptions.check}")
    @CommandPermission("guilds.command.check")
    public void onCheck(Player player) {
        Guild guild = guildHandler.getGuild(player);

        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        List<String> guildList = guildHandler.getInvitedGuilds(player.getUniqueId());

        if (guildList.size() > 0) {
            getCurrentCommandIssuer().sendInfo(Messages.PENDING__INVITES, "{number}", String.valueOf(guildList.size()), "{guilds}", String.join(",", guildList));
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.ERROR__NO_GUILD);
    }

    /**
     * Request an invite
     *
     * @param player the player requesting
     * @param name   the name of the guild
     */
    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission("guilds.command.request")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onRequest(Player player, @Values("@guilds") @Single String name) {
        Guild guild = guildHandler.getGuild(player);
        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        Guild targetGuild = guildHandler.getGuild(name);
        if (targetGuild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        for (GuildMember member : targetGuild.getMembers()) {
            GuildRole role = member.getRole();
            if (role.isInvite()) {
                OfflinePlayer guildPlayer = Bukkit.getOfflinePlayer(member.getUuid());
                if (guildPlayer.isOnline()) {
                    getCurrentCommandManager().getCommandIssuer(guildPlayer).sendInfo(Messages.REQUEST__INCOMING_REQUEST, "{player}", player.getName());
                }
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.REQUEST__SUCCESS, "{guild}", targetGuild.getName());
    }

    /**
     * Open the guild buff menu
     * @param player the player opening the menu
     * @param guild the guild which's player is opening the menu
     * @param role the role of the player
     */
    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @CommandPermission("guilds.command.buff")
    public void onBuff(Player player, Guild guild, GuildRole role) {
        if (!role.isActivateBuff()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Inventory buff = Bukkit.createInventory(null, 9, settingsManager.getProperty(GuiSettings.GUILD_BUFF_NAME));
        List<String> lore = new ArrayList<>();
/*        createBuffItem("haste", lore, buff, 0);
        createBuffItem("speed", lore, buff, 1);
        createBuffItem("fire-resistance", lore, buff, 2);
        createBuffItem("night-vision", lore, buff, 3);
        createBuffItem("invisibility", lore, buff, 4);
        createBuffItem("strength", lore, buff, 5);
        createBuffItem("jump", lore, buff, 6);
        createBuffItem("water-breathing", lore, buff, 7);
        createBuffItem("regeneration", lore, buff, 8);*/
        player.openInventory(buff);
    }

    /**
     * Help command for the plugin
     * @param help an instance of
     * @see CommandHelp
     */
    @HelpCommand
    @CommandPermission("guilds.command.help")
    @Syntax("")
    @Description("{@@descriptions.help}")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }

    /**
     * Create an item stack for the list
     * @param mat
     * @param name
     * @param lore
     * @return
     */
    private ItemStack createItemStack(Material mat, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(mat);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    //todo rewrite this + explanation plz @Glare
//    /**
//     * Create an item for buff
//     * @param buffName
//     * @param name
//     * @param buff
//     * @param slot
//     */
//    private void createBuffItem(String buffName, List<String> name, Inventory buff, int slot) {
//        getStringList("buff.description." + buffName).stream().map(ConfigUtils::StringUtils.color).forEach(name::add);
//        name.add("");
//        name.add(getString("buff.description.price") + getString("buff.price." + buffName));
//        name.add(getString("buff.description.length") + getString("buff.time." + buffName));
//        if (getBoolean("buff.display." + buffName)) {
//            buff.setItem(slot, createItemStack(Material.getMaterial(getString("buff.icon." + buffName)), getString("buff.name." + buffName), name));
//        }
//        name.clear();
//    }
//
//    /**
//     * Handling for the list page
//     * @param page
//     * @return
//     */
//    public static Inventory getSkullsPage(int page) {
//        Map<UUID, ItemStack> skulls = new HashMap<>();
//        Inventory inv = Bukkit.createInventory(null, 54, getString("guild-list.gui-name"));
//
//        int startIndex = 0;
//        int endIndex = 0;
//
//        Guilds.getGuilds().getGuildHandler().getGuilds().values().forEach(guild -> {
//            ItemStack skull = HeadUtils.getSkull(HeadUtils.getTextureUrl(guild.getGuildMaster().getUniqueId()));
//            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
//            List<String> lore = new ArrayList<>();
//
//            getStringList("guild-list.head-lore").forEach(line -> lore.add(StringUtils.color(line)
//                    .replace("{guild-name}", guild.getName())
//                    .replace("{guild-prefix}", guild.getPrefix())
//                    .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
//                    .replace("{guild-status}", guild.getStatus())
//                    .replace("{guild-tier}", String.valueOf(guild.getTier()))
//                    .replace("{guild-balance}", String.valueOf(guild.getBalance()))
//                    .replace("{guild-member-count}", String.valueOf(guild.getMembers().size()))));
//
//            skullMeta.setLore(lore);
//
//            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
//            skullMeta.setDisplayName(getString("guild-list.item-name").replace("{player}", name).replace("{guild-name}", guild.getName()));
//            skull.setItemMeta(skullMeta);
//            skulls.put(guild.getGuildMaster().getUniqueId(), skull);
//        });
//
//        ItemStack previous = new ItemStack(Material.getMaterial(getString("guild-list.previous-page-item")), 1);
//        ItemMeta previousMeta = previous.getItemMeta();
//        previousMeta.setDisplayName(getString("guild-list.previous-page-item-name"));
//        previous.setItemMeta(previousMeta);
//        ItemStack next = new ItemStack(Material.getMaterial(getString("guild-list.next-page-item")), 1);
//        ItemMeta nextMeta = next.getItemMeta();
//        nextMeta.setDisplayName(getString("guild-list.next-page-item-name"));
//        next.setItemMeta(nextMeta);
//        ItemStack barrier = new ItemStack(Material.getMaterial(getString("guild-list.page-number-item")), 1);
//        ItemMeta barrierMeta = barrier.getItemMeta();
//        barrierMeta.setDisplayName(getString("guild-list.page-number-item-name").replace("{page}", String.valueOf(page)));
//        barrier.setItemMeta(barrierMeta);
//        inv.setItem(53, next);
//        inv.setItem(49, barrier);
//        inv.setItem(45, previous);
//
//        startIndex = (page - 1) * 45;
//        endIndex = startIndex + 45;
//
//        if (endIndex > skulls.values().size()) {
//            endIndex = skulls.values().size();
//        }
//
//        int iCount = 0;
//        for (int i1 = startIndex; i1 < endIndex; i1++) {
//            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
//            iCount++;
//        }
//
//        return inv;
//    }
//
//    /**
//     * Create player skull
//     * @param player
//     * @return
//     */
//    public ItemStack createSkull(Player player) {
//        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
//
//        SkullMeta meta = (SkullMeta) skull.getItemMeta();
//        meta.setOwner(player.getName());
//        meta.setDisplayName(getString("info.playername").replace("{player-name}", player.getName()));
//
//        List<String> info = new ArrayList<>();
//        info.add(getString("info.kills").replace("{kills}", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS))));
//        info.add(getString("info.deaths").replace("{deaths}", String.valueOf(player.getStatistic(Statistic.DEATHS))));
//        meta.setLore(info);
//
//        skull.setItemMeta(meta);
//        return skull;
//    }
//
//
    /**
     * Checks the name requirements from the config.
     * @param name the name to check
     * @return a boolean if the name is wrong
     */
    private boolean nameMeetsRequirements(String name) {
        String regex = settingsManager.getProperty(GuildSettings.NAME_REQUIREMENTS);

        if (!name.matches(regex)) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return true;
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE)) {
            for (String word : settingsManager.getProperty(GuildSettings.BLACKLIST_WORDS)) {
                if (name.toLowerCase().contains(word)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return true;
                }
            }
        }

        return false;
    }

}
