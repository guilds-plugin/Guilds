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
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildInviteEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildHome;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildSkull;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//todo rewrite lol -> this has been rewritten mostly there are still quite a lot of todos due to things being unclear
// or not being added yet, make sure to fix these todos and commented out code and then we can start cleaning the warnings.
// xx lemmo.
@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandGuilds extends BaseCommand {

    private Guilds guilds;
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
     * Rename a guild
     * @param player the player renaming this guild
     * @param guild the guild being renamed
     * @param role the role of the player
     * @param name new name of guild
     */
    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission(Constants.BASE_PERM + "rename")
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
     * Change guild prefix
     * @param player the player changing the guild prefix
     * @param guild the guild which's prefix is getting changed
     * @param role the role of the player
     * @param prefix the new prefix
     */
    @Subcommand("prefix")
    @Description("{@@descriptions.prefix}")
    @CommandPermission(Constants.BASE_PERM + "prefix")
    @Syntax("<prefix>")
    public void onPrefix(Player player, Guild guild, GuildRole role, String prefix) {
        if (!role.isChangePrefix())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (!prefix.matches(settingsManager.getProperty(GuildSettings.PREFIX_REQUIREMENTS))) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL, "{prefix}", prefix);
        guild.setPrefix(StringUtils.color(prefix));
    }

    /**
     * Invite player to guild
     * @param player current player
     * @param targetPlayer player being invited
     * @param guild the guild that the targetPlayer is being invited to
     * @param role the role of the player
     */
    @Subcommand("invite")
    @Description("{@@descriptions.invite}")
    @CommandPermission(Constants.BASE_PERM + "invite")
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
    @CommandPermission(Constants.BASE_PERM + "upgrade")
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
    @CommandPermission(Constants.BASE_PERM + "transfer")
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
    @CommandPermission(Constants.BASE_PERM + "leave")
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

                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__GUILDMASTER_LEFT, "{player}", player.getName());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                } else {
                    guild.removeMember(player);

                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__PLAYER_LEFT, "{player}", player.getName());
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
    @CommandPermission(Constants.BASE_PERM + "list")
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
    @CommandPermission(Constants.BASE_PERM + "delete")
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
    @CommandPermission(Constants.BASE_PERM + "decline")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void onDecline(Player player, @Values("@invitedTo") @Single String name) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        if (guildHandler.getGuild(player) != null) return;
        if (!guild.checkIfInvited(player)) return;

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
    @CommandPermission(Constants.BASE_PERM + "boot")
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
        guild.sendMessage(getCurrentCommandManager(), Messages.BOOT__PLAYER_KICKED, "{player}", bootedPlayer.getName(), "{kicker}", player.getName());
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
    @CommandPermission(Constants.BASE_PERM + "vault")
    public void onVault(Player player, Guild guild, GuildRole role, @Default("1") Integer vault) {
        if (!role.isOpenVault()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        GuildTier tier = guild.getTier();

        if (vault > tier.getVaultAmount()) {
            getCurrentCommandIssuer().sendInfo(Messages.VAULTS__MAXED);
            return;
        }

        try {
            guildHandler.getGuildVault(guild, vault);
        } catch (Exception ex) {
            guildHandler.getCachedVaults().get(guild).add(Bukkit.createInventory(null, 54, "PlaceholderText"));
        }

        player.openInventory(guildHandler.getGuildVault(guild, vault));
    }

    /**
     * Accept a guild invite
     * @param player the player accepting the invite
     * @param name the name of the guild being accepted
     */
    @Subcommand("accept|join")
    @Description("{@@descriptions.accept}")
    @CommandPermission(Constants.BASE_PERM + "accept")
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

        if (guild.checkIfFull()) {
            getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.addMember(new GuildMember(player.getUniqueId(), guildHandler.getLowestGuildRole()));

        guild.sendMessage(getCurrentCommandManager(), Messages.ACCEPT__PLAYER_JOINED, "{player}", player.getName());
        getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__SUCCESSFUL, "{guild}", guild.getName());
    }

    /**
     * Check if you have any guild invites
     * @param player the player checking for invites
     */
    @Subcommand("check")
    @Description("{@@descriptions.check}")
    @CommandPermission(Constants.BASE_PERM + "check")
    public void onCheck(Player player) {
        Guild guild = guildHandler.getGuild(player);

        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }
        guildHandler.checkInvites(getCurrentCommandManager(), player);
    }

    /**
     * Request an invite
     *
     * @param player the player requesting
     * @param name   the name of the guild
     */
    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission(Constants.BASE_PERM + "request")
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
    @CommandPermission(Constants.BASE_PERM + "buff")
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
