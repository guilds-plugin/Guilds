package me.glaremasters.guilds.commands;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.*;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildBuilder;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ConfigUtils;
import me.glaremasters.guilds.utils.ConfirmAction;
import me.glaremasters.guilds.utils.Serialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.SkullType;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;

import static me.glaremasters.guilds.listeners.Players.GUILD_CHAT_PLAYERS;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 4:57 PM
 */
@CommandAlias("guild|guilds")
public class CommandGuilds extends BaseCommand {

    @Dependency
    private Guilds guilds;

    public static Inventory guildList = null;
    public static Map<UUID, Integer> playerPages = new HashMap<>();

    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission("guilds.command.create")
    @Syntax("<name> (optional) <prefix>")
    public void onCreate(Player player, String name, @Optional String prefix) {
        if (Guild.getGuild(player.getUniqueId()) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        int minLength = guilds.getConfig().getInt("name.min-length");
        int maxLength = guilds.getConfig().getInt("name.max-length");
        String regex = guilds.getConfig().getString("name.regex");

        if (name.length() < minLength || name.length() > maxLength || !name.matches(regex)) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return;
        }

        for (String guildName : guilds.getGuildHandler().getGuilds().keySet()) {
            if (guildName.equalsIgnoreCase(name)) {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__GUILD_NAME_TAKEN);
                return;
            }
        }

        if (guilds.getConfig().getBoolean("enable-blacklist")) {
            List<String> blacklist = guilds.getConfig().getStringList("blacklist");

            System.out.println(blacklist);
            for (String censor : blacklist) {
                if (name.toLowerCase().contains(censor)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return;
                }
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.CREATE__WARNING);

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildBuilder gb = new GuildBuilder();
                gb.setName(color(name));
                if (prefix == null) {
                    gb.setPrefix(color(name));
                } else {
                    gb.setPrefix(color(prefix));
                }
                gb.setStatus("Private");
                gb.setMaster(player.getUniqueId());
                Guild guild = gb.createGuild();
                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                guilds.getDatabase().createGuild(guild);
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.getName());
                guilds.getActionHandler().removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__CANCELLED);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("confirm")
    @Description("{@@descriptions.confirm}")
    @CommandPermission("guilds.command.confirm")
    public void onConfirm(Player player) {
        ConfirmAction action = guilds.getActionHandler().getActions().get(player);
        if (action == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CONFIRM__ERROR);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.CONFIRM__SUCCESS);
            action.accept();
        }
    }

    @Subcommand("cancel")
    @Description("{@@descriptions.cancel}")
    @CommandPermission("guilds.command.cancel")
    public void onCancel(Player player) {
        ConfirmAction action = guilds.getActionHandler().getActions().get(player);
        if (action == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CANCEL__ERROR);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.CANCEL__SUCCESS);
            action.decline();
        }
    }

    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission("guilds.command.reload")
    public void onReload(CommandSender sender) {
        guilds.reloadConfig();
        getCurrentCommandIssuer().sendInfo(Messages.RELOAD__RELOADED);
    }

    @Subcommand("sethome")
    @Description("{@@descriptions.sethome}")
    @CommandPermission("guilds.command.sethome")
    public void onSetHome(Player player, Guild guild, GuildRole role) {
        if (!role.canChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.updateHome(ACFBukkitUtil.fullLocationToString(player.getLocation()));
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
    }

    @Subcommand("delhome")
    @Description("{@@descriptions.delhome}")
    @CommandPermission("guilds.command.delhome")
    public void onDelHome(Player player, Guild guild, GuildRole role) {
        if (!role.canChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.updateHome("");
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
    }

    @Subcommand("give")
    @Description("{@@descriptions.give}")
    @CommandPermission("guilds.command.give")
    @Syntax("<player> <amount>")
    public void onGive(CommandSender sender, Player player, Integer amount) {
        if (player == null) return;

        String ticketName = color(guilds.getConfig().getString("upgrade-ticket.name"));
        String ticketMaterial = guilds.getConfig().getString("upgrade-ticket.material");
        String ticketLore = color(guilds.getConfig().getString("upgrade-ticket.lore"));

        ItemStack upgradeTicket = new ItemStack(Material.getMaterial(ticketMaterial), amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ticketLore);
        meta.setDisplayName(ticketName);
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
    }

    @Subcommand("home")
    @Description("{@@descriptions.home}")
    @CommandPermission("guilds.command.home")
    public void onHome(Player player, Guild guild) {
        if (guild.getHome().equals("")) {
            getCurrentCommandIssuer().sendInfo(Messages.HOME__NO_HOME_SET);
            return;
        }
        player.teleport(ACFBukkitUtil.stringToLocation(guild.getHome()));
        getCurrentCommandIssuer().sendInfo(Messages.HOME__TELEPORTED);
    }

    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission("guilds.command.rename")
    @Syntax("<name>")
    public void onRename(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canChangeName()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        int minLength = guilds.getConfig().getInt("name.min-length");
        int maxLength = guilds.getConfig().getInt("name.max-length");
        String regex = guilds.getConfig().getString("name.regex");

        if (name.length() < minLength || name.length() > maxLength || !name.matches(regex)) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return;
        }

        for (String guildName : guilds.getGuildHandler().getGuilds().keySet()) {
            if (guildName.equalsIgnoreCase(name)) {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__GUILD_NAME_TAKEN);
                return;
            }
        }

        if (guilds.getConfig().getBoolean("enable-blacklist")) {
            List<String> blacklist = guilds.getConfig().getStringList("blacklist");

            System.out.println(blacklist);
            for (String censor : blacklist) {
                if (name.toLowerCase().contains(censor)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return;
                }
            }
        }

        String oldName = guild.getName();
        guilds.getDatabase().removeGuild(Guild.getGuild(oldName));
        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name);
        guild.updateName(color(name));
    }

    @Subcommand("chat")
    @Description("{@@descriptions.chat}")
    @CommandPermission("guilds.command.chat")
    public void onGuildChat(Player player, Guild guild, GuildRole role) {

        if (!role.canChat()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {
            GUILD_CHAT_PLAYERS.remove(player.getUniqueId());
            getCurrentCommandIssuer().sendInfo(Messages.CHAT__DISABLED);
        } else {
            GUILD_CHAT_PLAYERS.add(player.getUniqueId());
            getCurrentCommandIssuer().sendInfo(Messages.CHAT__ENABLED);
        }

    }

    @Subcommand("status")
    @Description("{@@descriptions.status}")
    @CommandPermission("guilds.command.status")
    public void onStatus(Player player, Guild guild, GuildRole role) {
        String status = guild.getStatus();
        if (!role.canChangeStatus()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        if (status.equalsIgnoreCase("private")) {
            status = "Public";
        } else {
            status = "Private";
        }
        String updatedStatus = StringUtils.capitalize(status);
        getCurrentCommandIssuer().sendInfo(Messages.STATUS__SUCCESSFUL, "{status}", status);
        guild.updateStatus(updatedStatus);
    }

    @Subcommand("prefix")
    @Description("{@@descriptions.prefix}")
    @CommandPermission("guilds.command.prefix")
    @Syntax("<prefix>")
    public void onPrefix(Player player, Guild guild, GuildRole role, String prefix) {
        if (!role.canChangePrefix()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL);
        guild.updatePrefix(color(prefix));
    }

    @Subcommand("version|v|ver")
    @Description("{@@descriptions.version}")
    public void onVersion(CommandSender sender) {
        SpigotUpdater updater = new SpigotUpdater(guilds, 48920);
        PluginDescriptionFile pdf = guilds.getDescription();
        try {
            String message;
            if (updater.getLatestVersion().equalsIgnoreCase(pdf.getVersion())) {
                message = "";
            } else {
                message = "\n&8» &7An update has been found! &f- " + updater.getResourceURL();
            }
            sender.sendMessage(
                    color("&8&m--------------------------------------------------"
                            + "\n&8» &7Name - &a"
                            + pdf.getName() + "\n&8» &7Version - &a" + pdf.getVersion()
                            + "\n&8» &7Author - &a" + pdf.getAuthors() + "\n&8» &7Support - &a"
                            + pdf.getWebsite() + message
                            + "\n&8&m--------------------------------------------------"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subcommand("invite")
    @Description("{@@descriptions.invite}")
    @CommandPermission("guilds.command.invite")
    @Syntax("<name>")
    public void onInvite(Player player, String targetPlayer, Guild guild, GuildRole role) {

        if (!role.canInvite()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Player target = Bukkit.getPlayerExact(targetPlayer);

        if (target == null || !target.isOnline()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", targetPlayer);
            return;
        }
        Guild invitedPlayerGuild = Guild.getGuild(target.getUniqueId());

        if (invitedPlayerGuild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        if (guild.getInvitedMembers().contains(target.getUniqueId())) {
            getCurrentCommandIssuer().sendInfo(Messages.INVITE__ALREADY_INVITED);
            return;
        }

        GuildInviteEvent event = new GuildInviteEvent(player, guild, target);
        guilds.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.inviteMember(target.getUniqueId());
        guilds.getManager().getCommandIssuer(target).sendInfo(Messages.INVITE__MESSAGE, "{player}", player.getName(), "{guild}", guild.getName());
        getCurrentCommandIssuer().sendInfo(Messages.INVITE__SUCCESSFUL, "{player}", target.getName());

    }

    @Subcommand("upgrade")
    @Description("{@@descriptions.upgrade}")
    @CommandPermission("guilds.command.upgrade")
    public void onUpgrade(Player player, Guild guild, GuildRole role) {
        if (!role.canUpgradeGuild()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        int tier = guild.getTier();
        if (tier >= guilds.getConfig().getInt("max-number-of-tiers")) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__TIER_MAX);
            return;
        }
        if (guild.getMembersToRankup() != 0 && guild.getMembers().size() < guild.getMembersToRankup()) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MEMBERS, "{amount}", String.valueOf(guild.getMembersToRankup()));
            return;
        }
        double balance = guild.getBalance();
        double upgradeCost = guild.getTierCost();
        if (balance < upgradeCost) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance));
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__MONEY_WARNING, "{amount}", String.valueOf(upgradeCost));
        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (balance < upgradeCost) {
                    getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance));
                    return;
                }
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__SUCCESS);
                // Carry over perms check
                guild.updateTier(tier + 1);
                // Add new perms
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__CANCEL);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("transfer")
    @Description("{@@descriptions.transfer}")
    @CommandPermission("guilds.command.transfer")
    @Syntax("<player>")
    public void onTransfer(Player player, Guild guild, GuildRole role, String target) {
        if (!role.canTransfer()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Player transferPlayer = Bukkit.getPlayerExact(target);
        if (transferPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND);
            return;
        }

        GuildMember oldMaster = guild.getMember(player.getUniqueId());
        GuildMember newMaster = guild.getMember(transferPlayer.getUniqueId());

        if (newMaster == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD);
            return;
        }
        if (newMaster.getRole() != 1) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_OFFICER);
            return;
        }

        GuildRole newRole, oldRole;
        int currentLevel = newMaster.getRole();
        newRole = GuildRole.getRole(currentLevel - 1);
        oldRole = GuildRole.getRole(currentLevel + 1);

        if (oldMaster.getRole() == 0) {
            oldMaster.setRole(oldRole);
            newMaster.setRole(newRole);
            guild.updateGuild("", guild.getName(), Guild.getGuild(guild.getName()).getName());
            getCurrentCommandIssuer().sendInfo(Messages.TRANSFER__SUCCESS);
            guilds.getManager().getCommandIssuer(transferPlayer).sendInfo(Messages.TRANSFER__NEWMASTER);
        }

    }

    @Subcommand("leave|exit")
    @Description("{@@descriptions.leave}")
    @CommandPermission("guilds.command.leave")
    public void onLeave(Player player, Guild guild) {

        if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING_GUILDMASTER);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING);
        }

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildLeaveEvent event = new GuildLeaveEvent(player, guild);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                // If guild master remove perms from all members
                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                    guilds.getServer().getPluginManager().callEvent(removeEvent);
                    if (removeEvent.isCancelled()) return;
                    guilds.getDatabase().removeGuild(guild);
                    guild.removeMember(player.getUniqueId());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    guilds.getActionHandler().removeAction(player);
                } else {
                    guild.removeMember(player.getUniqueId());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    guilds.getActionHandler().removeAction(player);
                }
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__CANCELLED);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @CommandPermission("guilds.command.list")
    public void onGuildList(Player player) {
        playerPages.put(player.getUniqueId(), 1);
        guildList = getSkullsPage(1);
        player.openInventory(guildList);
    }

    @Subcommand("info")
    @Description("{@@descriptions.info}")
    @CommandPermission("guilds.command.info")
    public void onGuildInfo(Player player, Guild guild) {

        Inventory heads = Bukkit.createInventory(null, InventoryType.HOPPER, guilds.getConfig().getString("gui-name.info"));

        heads.setItem(1, createSkull(player));

        // Item 1: Paper
        ArrayList<String> paperlore = new ArrayList<String>();
        paperlore.add(color(guilds.getConfig().getString("info.guildname").replace("{guild-name}", guild.getName())));
        paperlore.add(color(guilds.getConfig().getString("info.prefix").replace("{guild-prefix}", guild.getPrefix())));
        paperlore.add(color(guilds.getConfig().getString("info.role").replace("{guild-role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName())));
        paperlore.add(color(guilds.getConfig().getString("info.master").replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())));
        paperlore.add(color(guilds.getConfig().getString("info.member-count").replace("{member-count}", Integer.toString(guild.getMembers().size()))));
        paperlore.add(color(guilds.getConfig().getString("info.guildstatus").replace("{guild-status}", guild.getStatus())));
        paperlore.add(color(guilds.getConfig().getString("info.guildtier").replace("{guild-tier}", Integer.toString(guild.getTier()))));
        heads.setItem(2, createItemStack(Material.PAPER, guilds.getConfig().getString("info.info"), paperlore));

        // Item 2: Diamond
        ArrayList<String> diamondlore = new ArrayList<String>();
        diamondlore.add(color(guilds.getConfig().getString("info.balance").replace("{guild-balance}", Double.toString(guild.getBalance()))));
        diamondlore.add(color(guilds.getConfig().getString("info.max-balance").replace("{guild-max-balance}", Double.toString(guild.getMaxBankBalance()))));
        heads.setItem(3, createItemStack(Material.DIAMOND, guilds.getConfig().getString("info.money"), diamondlore));

        // Open inventory
        player.openInventory(heads);

    }

    @Subcommand("delete")
    @Description("{@@descriptions.delete}")
    @CommandPermission("guilds.command.delete")
    public void onDelete(Player player, Guild guild, GuildRole role) {
        if (!role.canRemoveGuild()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.DELETE__WARNING);
        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                getCurrentCommandIssuer().sendInfo(Messages.DELETE__SUCCESSFUL, "{guild}", guild.getName());
                // Todo - Something about perms
                guilds.getDatabase().removeGuild(guild);
                guilds.getActionHandler().removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.DELETE__CANCELLED);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("decline")
    @Description("{@@descriptions.decline}")
    @CommandPermission("guilds.command.decline")
    @Syntax("<guild name>")
    public void onDecline(Player player, String name) {
        Guild guild = Guild.getGuild(name);
        if (Guild.getGuild(player.getUniqueId()) != null) return;
        if (guild == null) return;
        if (!guild.getInvitedMembers().contains(player.getUniqueId())) return;
        getCurrentCommandIssuer().sendInfo(Messages.DECLINE__SUCCESS);
        guild.removeInvitedPlayer(player.getUniqueId());
    }

    @Subcommand("boot|kick")
    @Description("Kick someone from your Guild")
    @CommandPermission("guilds.command.boot")
    @Syntax("<name>")
    public void onKick(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canKick()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(name);
        if (bootedPlayer == null) return;
        if (bootedPlayer.getUniqueId() == null) return;

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());
        if (kickedPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", name);
            return;
        }

        Guild targetGuild = Guild.getGuild(kickedPlayer.getUniqueId());
        if (targetGuild == null) return;
        if (!guild.getName().equals(targetGuild.getName())) return;

        if (kickedPlayer.equals(guild.getGuildMaster())) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.removeMember(kickedPlayer.getUniqueId());
        getCurrentCommandIssuer().sendInfo(Messages.BOOT__SUCCESSFUL, "{player}", bootedPlayer.getName());
        // Todo - Send message to all online members saying <user> was kicked.
        guilds.getManager().getCommandIssuer(bootedPlayer).sendInfo(Messages.BOOT__KICKED);
    }

    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @CommandPermission("guilds.command.vault")
    public void onVault(Player player, Guild guild, GuildRole role) {
        if (!role.canOpenVault()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        if (guild.getInventory().equalsIgnoreCase("")) {
            Inventory inv = Bukkit.createInventory(null, 54, guild.getName() + "'s Guild Vault");
            player.openInventory(inv);
            return;
        }
        try {
            player.openInventory(Serialization.deserializeInventory(guild.getInventory()));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
    @Syntax("<player>")
    public void onDemote(Player player, String target, Guild guild, GuildRole role) {
        if (!role.canDemote()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

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

        if (demotedMember.getRole() == 3 || demotedMember.getRole() == 0) {
            getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__CANT_DEMOTE);
            return;
        }

        GuildRole demotedRole = GuildRole.getRole(demotedMember.getRole() + 1);
        String oldRank = GuildRole.getRole(demotedMember.getRole()).getName();
        String newRank = demotedRole.getName();

        demotedMember.setRole(demotedRole);
        guild.updateGuild("");

        getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__DEMOTE_SUCCESSFUL, "{player}", demotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (demotedPlayer.isOnline()) {
            guilds.getManager().getCommandIssuer(demotedPlayer).sendInfo(Messages.DEMOTE__YOU_WERE_DEMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    @Subcommand("promote")
    @Description("{@@descriptions.promote}")
    @CommandPermission("guilds.command.promote")
    @Syntax("<player>")
    public void onPromote(Player player, String target, Guild guild, GuildRole role) {
        if (!role.canPromote()) {
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

        if (promotedMember.getRole() <= 1) {
            getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__CANT_PROMOTE);
            return;
        }

        GuildRole promotedRole = GuildRole.getRole(promotedMember.getRole() - 1);
        String oldRank = GuildRole.getRole(promotedMember.getRole()).getName();
        String newRank = promotedRole.getName();

        promotedMember.setRole(promotedRole);
        guild.updateGuild("");

        getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__PROMOTE_SUCCESSFUL, "{player}", promotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (promotedPlayer.isOnline()) {
            guilds.getManager().getCommandIssuer(promotedPlayer).sendInfo(Messages.PROMOTE__YOU_WERE_PROMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    @Subcommand("accept")
    @Description("{@@descriptions.accept}")
    @CommandPermission("guilds.command.accept")
    @CommandAlias("join")
    @Syntax("<guild name>")
    public void onAccept(Player player, String name) {
        if (Guild.getGuild(player.getUniqueId()) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }
        Guild guild = (Guild) guilds.getGuildHandler().getGuilds().values().toArray()[0];
        try {
            if (name == null) {
                int invites = 0;
                int indexes = 0;
                for (int i = 0; i < guilds.getGuildHandler().getGuilds().values().size(); i++) {
                    Guild guildtmp = (Guild) guilds.getGuildHandler().getGuilds().values().toArray()[i];
                    if (guildtmp.getInvitedMembers().contains(player.getUniqueId())) {
                        invites++;
                        indexes = i;
                    }
                }
                if (invites == 1) {
                    guild = (Guild) guilds.getGuildHandler().getGuilds().values().toArray()[indexes];
                } else {
                    getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__NOT_INVITED);
                    return;
                }
            } else {
                guild = Guild.getGuild(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        if (guild.getStatus().equalsIgnoreCase("private")) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__NOT_INVITED);
                return;
            }
        }

        if (guild.getMembers().size() >= guild.getMaxMembers()) {
            getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        guilds.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.sendMessage(Messages.ACCEPT__PLAYER_JOINED, "{player}", player.getName());
        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
        guild.removeInvitedPlayer(player.getUniqueId());
        getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__SUCCESSFUL, "{guild}", guild.getName());
    }

    @Subcommand("check")
    @Description("{@@descriptions.check}")
    @CommandPermission("guilds.command.check")
    public void onCheck(Player player) {
        Guild guild2 = Guild.getGuild(player.getUniqueId());
        if (!(guild2 == null)) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }
        List<String> guildList = new ArrayList<>();
        for (Guild guild : guilds.getGuildHandler().getGuilds().values()) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                continue;
            }
            guildList.add(guild.getName());
        }
        if (guildList.size() > 0) {
            getCurrentCommandIssuer().sendInfo(Messages.PENDING__INVITES, "{number}", String.valueOf(guildList.size()), "{guild}", String.join(",", guildList));
        }
    }

    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @CommandPermission("guilds.command.buff")
    public void onBuff(Player player, Guild guild, GuildRole role) {
        if (!role.canActivateBuff()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Inventory buff = Bukkit.createInventory(null, 9, "Guild Buffs");
        List<String> lore = new ArrayList<>();
        createBuffItem("haste", lore, buff, 0);
        createBuffItem("speed", lore, buff, 1);
        createBuffItem("fire-resistance", lore, buff, 2);
        createBuffItem("night-vision", lore, buff, 3);
        createBuffItem("invisibility", lore, buff, 4);
        createBuffItem("strength", lore, buff, 5);
        createBuffItem("jump", lore, buff, 6);
        createBuffItem("water-breathing", lore, buff, 7);
        createBuffItem("regeneration", lore, buff, 8);
        player.openInventory(buff);
    }

    @HelpCommand
    @CommandPermission("guilds.command.help")
    @Syntax("")
    @Description("{@@descriptions.help}")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }

    private ItemStack createItemStack(Material mat, String name, List<String> lore) {
        ItemStack paper = new ItemStack(mat);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        paper.setItemMeta(meta);
        return paper;
    }

    private void createBuffItem(String buffName, List<String> name, Inventory buff, int slot) {
        guilds.getConfig().getStringList("buff.description." + buffName).stream().map(ConfigUtils::color).forEach(name::add);
        name.add("");
        name.add(color(guilds.getConfig().getString("buff.description.price") + guilds.getConfig().getInt("buff.price." + buffName)));
        name.add(color(guilds.getConfig().getString("buff.description.length") + guilds.getConfig().getInt("buff.time." + buffName)));
        if (guilds.getConfig().getBoolean("buff.display." + buffName)) {
            buff.setItem(slot, createItemStack(Material.getMaterial(guilds.getConfig().getString("buff.icon." + buffName)), guilds.getConfig().getString("buff.name." + buffName), name));
        }
        name.clear();
    }

    public static Inventory getSkullsPage(int page) {
        HashMap<UUID, ItemStack> skulls = new HashMap<>();
        Inventory inv = Bukkit.createInventory(null, 54, color(Guilds.getGuilds().getConfig().getString("guild-list.gui-name")));

        int startIndex = 0;
        int endIndex = 0;

        for (int i = 0; i < Guilds.getGuilds().getGuildHandler().getGuilds().values().size(); i++) {
            Guild guild = (Guild) Guilds.getGuilds().getGuildHandler().getGuilds().values().toArray()[i];
            ItemStack item = new ItemStack(Material.getMaterial(randomItem()));
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();
            for (String text : Guilds.getGuilds().getConfig().getStringList("guild-list.head-lore")) {
                lore.add(color(text).
                        replace("{guild-name}", guild.getName())
                        .replace("{guild-prefix}", guild.getPrefix())
                        .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
                        .replace("{guild-status}", guild.getStatus())
                        .replace("{guild-tier}", String.valueOf(guild.getTier()))
                        .replace("{guild-balance}", String.valueOf(guild.getBalance()))
                        .replace("{guild-member-count}", String.valueOf(guild.getMembers().size())));
            }
            itemMeta.setLore(lore);
            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
            itemMeta.setDisplayName(color(Guilds.getGuilds().getConfig().getString("guild-list.item-name").replace("{player}", name)));
            item.setItemMeta(itemMeta);
            skulls.put(guild.getGuildMaster().getUniqueId(), item);
        }

        ItemStack previous = new ItemStack(Material.getMaterial(Guilds.getGuilds().getConfig().getString("guild-list.previous-page-item")), 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(color(Guilds.getGuilds().getConfig().getString("guild-list.previous-page-item-name")));
        previous.setItemMeta(previousMeta);
        ItemStack next = new ItemStack(Material.getMaterial(Guilds.getGuilds().getConfig().getString("guild-list.next-page-item")), 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(color(Guilds.getGuilds().getConfig().getString("guild-list.next-page-item-name")));
        next.setItemMeta(nextMeta);
        ItemStack barrier = new ItemStack(Material.getMaterial(Guilds.getGuilds().getConfig().getString("guild-list.page-number-item")), 1);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(color(Guilds.getGuilds().getConfig().getString("guild-list.page-number-item-name").replace("{page}", String.valueOf(page))));
        barrier.setItemMeta(barrierMeta);
        inv.setItem(53, next);
        inv.setItem(49, barrier);
        inv.setItem(45, previous);

        startIndex = (page - 1) * 45;
        endIndex = startIndex + 45;

        if (endIndex > skulls.values().size()) { endIndex = skulls.values().size(); }

        int iCount = 0;
        for (int i1 = startIndex; i1 < endIndex; i1++) {
            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
            iCount++;
        }

        return inv;
    }

    public static String randomItem() {

        List<String> items = Guilds.getGuilds().getConfig().getStringList("random-items");

        int random = new Random().nextInt(items.size());
        String mat_name = items.get(random);
        return mat_name;
    }

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public ItemStack createSkull(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        final Guilds instance = Guilds.getGuilds();
        meta.setDisplayName(color(guilds.getConfig().getString("info.playername").replace("{player-name}", player.getName())));

        ArrayList<String> info = new ArrayList<String>();
        info.add(color(guilds.getConfig().getString("info.kills").replace("{kills}", Integer.toString(player.getStatistic(Statistic.PLAYER_KILLS)))));
        info.add(color(guilds.getConfig().getString("info.deaths").replace("{deaths}", Integer.toString(player.getStatistic(Statistic.DEATHS)))));
        meta.setLore(info);

        skull.setItemMeta(meta);
        return skull;
    }

}
