package me.glaremasters.guilds.commands;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildInviteEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ConfigUtils;
import me.glaremasters.guilds.utils.ConfirmAction;
import me.glaremasters.guilds.utils.GuildUtils;
import me.glaremasters.guilds.utils.HeadUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static javax.swing.UIManager.getInt;
import static me.glaremasters.guilds.listeners.PlayerListener.GUILD_CHAT_PLAYERS;
import static me.glaremasters.guilds.utils.ConfigUtils.*;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 4:57 PM
 */
@CommandAlias("guild|guilds")
public class CommandGuilds extends BaseCommand {

    public List<Player> home = new ArrayList<>();
    public List<Player> setHome = new ArrayList<>();
    public Map<Player, Location> warmUp = new HashMap<>();
    public static List<Inventory> vaults = new ArrayList<>();

    @Dependency
    private Guilds guilds;

    private GuildUtils utils;

    public static Inventory guildList = null;
    public static Map<UUID, Integer> playerPages = new HashMap<>();

    public CommandGuilds(GuildUtils utils) {
        this.utils = utils;
    }

    /**
     * Create a guild
     * @param player
     * @param name name of guild
     * @param prefix prefix of guild
     */
    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission("guilds.command.create")
    @Syntax("<name> (optional) <prefix>")
    public void onCreate(Player player, String name, @Optional String prefix) {
        if (utils.getGuild(player.getUniqueId()) != null) {
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
            for (String censor : blacklist) {
                if (name.toLowerCase().contains(censor)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return;
                }
            }
        }

        if (meetsCost(player, "cost.creation")) return;

        getCurrentCommandIssuer().sendInfo(Messages.CREATE__WARNING, "{amount}", String.valueOf(getDouble("cost.creation")));

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (meetsCost(player, "cost.creation")) return;
                guilds.getEconomy().withdrawPlayer(player, getDouble("cost.creation"));
                Guild.GuildBuilder gb = Guild.builder();
                gb.name(color(name));
                if (prefix == null) {
                    gb.prefix(color(name));
                } else {
                    if (!prefix.matches(getString("prefix.regex"))) return;
                    gb.prefix(color(prefix));
                }
                gb.status("Private");
                gb.texture(HeadUtils.getTextureUrl(player.getUniqueId()));
                gb.master(player.getUniqueId());
                Guild guild = gb.build();
                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                guilds.getDatabase().createGuild(guild);
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.getName());
                guilds.getActionHandler().removeAction(player);
                guilds.createNewVault(guild);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__CANCELLED);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    /**
     * Confirm an action
     * @param player
     */
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

    /**
     * Cancel an action
     * @param player
     */
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

    /**
     * Reload the config
     * @param sender
     */
    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission("guilds.command.reload")
    public void onReload(CommandSender sender) {
        guilds.reloadConfig();
        getCurrentCommandIssuer().sendInfo(Messages.RELOAD__RELOADED);
    }

    /**
     * Set a guild home
     * @param player
     * @param guild the guild that home is being set
     * @param role role of player
     */
    @Subcommand("sethome")
    @Description("{@@descriptions.sethome}")
    @CommandPermission("guilds.command.sethome")
    public void onSetHome(Player player, Guild guild, GuildRole role) {
        if (!role.canChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        if (meetsCost(player, "cost.sethome")) return;
        if (setHome.contains(player)) {
            getCurrentCommandIssuer().sendInfo(Messages.SETHOME__COOLDOWN, "{amount}", String.valueOf(getInt("cooldowns.sethome")));
            return;
        }
        guild.setHome(ACFBukkitUtil.fullLocationToString(player.getLocation()));
        guilds.getEconomy().withdrawPlayer(player, getDouble("cost.sethome"));
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
        setHome.add(player);
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(guilds, () -> setHome.remove(player), (20 * getInt("cooldowns.sethome")));
    }

    /**
     * Remove a guild home
     * @param player
     * @param guild the guild that the home is being removed
     * @param role role of player
     */
    @Subcommand("delhome")
    @Description("{@@descriptions.delhome}")
    @CommandPermission("guilds.command.delhome")
    public void onDelHome(Player player, Guild guild, GuildRole role) {
        if (!role.canChangeHome()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        guild.setHome("");
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
    }

    /**
     * Give a player upgrade tickets
     * @param sender
     * @param player
     * @param amount amount of tickets
     */
    @Subcommand("give")
    @Description("{@@descriptions.give}")
    @CommandPermission("guilds.command.give")
    @Syntax("<player> <amount>")
    public void onGive(CommandSender sender, Player player, @Default("1") Integer amount) {
        if (player == null) return;

        String ticketName = getString("upgrade-ticket.name");
        String ticketMaterial = getString("upgrade-ticket.material");
        String ticketLore = getString("upgrade-ticket.lore");

        ItemStack upgradeTicket = new ItemStack(Material.getMaterial(ticketMaterial), amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ticketLore);
        meta.setDisplayName(ticketName);
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
    }

    /**
     * Go to guild home
     * @param player
     * @param guild
     */
    @Subcommand("home")
    @Description("{@@descriptions.home}")
    @CommandPermission("guilds.command.home")
    public void onHome(Player player, Guild guild) {
        if (guild.getHome().equals("")) {
            getCurrentCommandIssuer().sendInfo(Messages.HOME__NO_HOME_SET);
            return;
        }
        if (home.contains(player)) {
            getCurrentCommandIssuer().sendInfo(Messages.HOME__COOLDOWN, "{amount}", String.valueOf(getInt("cooldowns.home")));
            return;
        }

        warmUp.put(player, player.getLocation());

        getCurrentCommandIssuer().sendInfo(Messages.HOME__WARMUP, "{amount}", String.valueOf(getInt("warmup.home")));

        Bukkit.getServer().getScheduler().runTaskLater(guilds, () -> {
            if (warmUp.get(player).distance(player.getLocation()) > 1) {
                getCurrentCommandIssuer().sendInfo(Messages.HOME__CANCELLED);
                warmUp.remove(player);
            } else {
                player.teleport(ACFBukkitUtil.stringToLocation(guild.getHome()));
                warmUp.remove(player);
                getCurrentCommandIssuer().sendInfo(Messages.HOME__TELEPORTED);
            }
            home.add(player);
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(guilds, () -> home.remove(player), (20 * getInt("cooldowns.home")));
        }, (20 * getInt("warmup.home")));
    }

    /**
     * Rename a guild
     * @param player
     * @param guild
     * @param role
     * @param name new name of guild
     */
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
            for (String censor : blacklist) {
                if (name.toLowerCase().contains(censor)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return;
                }
            }
        }

        String oldName = guild.getName();
        guilds.getDatabase().removeGuild(utils.getGuild(oldName));
        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", name);
        guild.setName(color(name));
    }

    /**
     * Toggles Guild Chat
     * @param player
     * @param guild
     * @param role
     */
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

    /**
     * Toggles Guild Status
     * @param player
     * @param guild
     * @param role
     */
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
        guild.setStatus(updatedStatus);
    }

    /**
     * Change guild prefix
     * @param player
     * @param guild
     * @param role
     * @param prefix new prefix
     */
    @Subcommand("prefix")
    @Description("{@@descriptions.prefix}")
    @CommandPermission("guilds.command.prefix")
    @Syntax("<prefix>")
    public void onPrefix(Player player, Guild guild, GuildRole role, String prefix) {
        if (!role.canChangePrefix()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        if (!prefix.matches(getString("prefix.regex"))) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL, "{prefix}", prefix);
        guild.setPrefix(color(prefix));
    }

    /**
     * Check version of plugin
     * @param sender
     */
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

    /**
     * Invite player to guild
     * @param player current player
     * @param targetPlayer player being invited
     * @param guild
     * @param role
     */
    @Subcommand("invite")
    @Description("{@@descriptions.invite}")
    @CommandPermission("guilds.command.invite")
    @CommandCompletion("@online")
    @Syntax("<name>")
    public void onInvite(Player player, Guild guild, GuildRole role, @Values("@online") @Single String targetPlayer) {

        if (!role.canInvite()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Player target = Bukkit.getPlayerExact(targetPlayer);

        if (target == null || !target.isOnline()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_FOUND, "{player}", targetPlayer);
            return;
        }
        Guild invitedPlayerGuild = utils.getGuild(target.getUniqueId());

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

        utils.inviteMember(guild, target.getUniqueId());
        guilds.getManager().getCommandIssuer(target).sendInfo(Messages.INVITE__MESSAGE, "{player}", player.getName(), "{guild}", guild.getName());
        getCurrentCommandIssuer().sendInfo(Messages.INVITE__SUCCESSFUL, "{player}", target.getName());

    }

    /**
     * Upgrade a guild
     * @param player
     * @param guild
     * @param role
     */
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
        if (utils.getMembersToRankup(guild) != 0 && guild.getMembers().size() < utils.getMembersToRankup(guild)) {
            getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__NOT_ENOUGH_MEMBERS, "{amount}", String.valueOf(utils.getMembersToRankup(guild)));
            return;
        }
        double balance = guild.getBalance();
        double upgradeCost = utils.getTierCost(guild);
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
                guild.setBalance(balance - upgradeCost);
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__SUCCESS);
                if (guilds.getConfig().getBoolean("carry-over-perms")) {
                    guild.setTier(tier + 1);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(guilds, () -> utils.addGuildPerms(guild), 60L);
                } else {
                    utils.removeGuildPerms(guild);
                    guild.setTier(tier + 1);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(guilds, () -> utils.addGuildPerms(guild), 60L);
                }
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__CANCEL);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    /**
     * Transfer a guild to a new user
     * @param player
     * @param guild
     * @param role
     * @param target new guild master
     */
    @Subcommand("transfer")
    @Description("{@@descriptions.transfer}")
    @CommandPermission("guilds.command.transfer")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void onTransfer(Player player, Guild guild, GuildRole role, @Values("@members") @Single String target) {
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
            utils.updateGuilds();
            getCurrentCommandIssuer().sendInfo(Messages.TRANSFER__SUCCESS);
            guilds.getManager().getCommandIssuer(transferPlayer).sendInfo(Messages.TRANSFER__NEWMASTER);
        }

    }

    /**
     * Leave a guild
     * @param player
     * @param guild
     */
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
                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.MASTER_LEFT);
                    guilds.getServer().getPluginManager().callEvent(removeEvent);
                    if (removeEvent.isCancelled()) return;
                    guilds.getVaults().remove(guild);
                    Guilds.checkForClaim(player, guild, guilds);
                    utils.sendMessage(guild, Messages.LEAVE__GUILDMASTER_LEFT, "{player}", player.getName());
                    utils.removeGuildPerms(guild);
                    guilds.getDatabase().removeGuild(guild);
                    utils.removeMember(guild, player.getUniqueId());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    guilds.getActionHandler().removeAction(player);
                } else {
                    utils.removeMember(guild, player.getUniqueId());
                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);
                    utils.sendMessage(guild, Messages.LEAVE__PLAYER_LEFT, "{player}", player.getName());
                    utils.removeGuildPerms(guild, player);
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

    /**
     * List all the guilds on the server
     * @param player
     */
    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @CommandPermission("guilds.command.list")
    public void onGuildList(Player player) {
        playerPages.put(player.getUniqueId(), 1);
        guildList = getSkullsPage(1);
        player.openInventory(guildList);
    }

    /**
     * List the info for you guild
     * @param player
     * @param guild
     */
    @Subcommand("info")
    @Description("{@@descriptions.info}")
    @CommandPermission("guilds.command.info")
    public void onGuildInfo(Player player, Guild guild) {

        Inventory heads = Bukkit.createInventory(null, InventoryType.HOPPER, getString("gui-name.info"));

        heads.setItem(1, createSkull(player));

        // Item 1: Paper
        List<String> paperlore = new ArrayList<>();
        paperlore.add(getString("info.guildname").replace("{guild-name}", guild.getName()));
        paperlore.add(getString("info.prefix").replace("{guild-prefix}", guild.getPrefix()));
        paperlore.add(getString("info.role").replace("{guild-role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName()));
        paperlore.add(getString("info.master").replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
        paperlore.add(getString("info.member-count").replace("{member-count}", String.valueOf(guild.getMembers().size())));
        paperlore.add(getString("info.guildstatus").replace("{guild-status}", guild.getStatus()));
        paperlore.add(getString("info.guildtier").replace("{guild-tier}", Integer.toString(guild.getTier())));
        heads.setItem(2, createItemStack(Material.PAPER, getString("info.info"), paperlore));

        // Item 2: Diamond
        List<String> diamondlore = new ArrayList<>();
        diamondlore.add(getString("info.balance").replace("{guild-balance}", String.valueOf(guild.getBalance())));
        diamondlore.add(getString("info.max-balance").replace("{guild-max-balance}", String.valueOf(utils.getMaxBankBalance(guild))));
        heads.setItem(3, createItemStack(Material.DIAMOND, getString("info.money"), diamondlore));

        // Open inventory
        player.openInventory(heads);

    }

    /**
     * Delete your guild
     * @param player
     * @param guild
     * @param role
     */
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
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.DELETED);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                guilds.getVaults().remove(guild);
                Guilds.checkForClaim(player, guild, guilds);
                getCurrentCommandIssuer().sendInfo(Messages.DELETE__SUCCESSFUL, "{guild}", guild.getName());
                utils.removeGuildPerms(guild);
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

    /**
     * Decline a guild invite
     * @param player
     * @param name
     */
    @Subcommand("decline")
    @Description("{@@descriptions.decline}")
    @CommandPermission("guilds.command.decline")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void onDecline(Player player, @Values("@invitedTo") @Single String name) {
        Guild guild = utils.getGuild2(name);
        if (utils.getGuild(player.getUniqueId()) != null) return;
        if (guild == null) return;
        if (!guild.getInvitedMembers().contains(player.getUniqueId())) return;
        getCurrentCommandIssuer().sendInfo(Messages.DECLINE__SUCCESS);
        utils.removeInvitedPlayer(guild, player.getUniqueId());
    }

    /**
     * Kick a player from the guild
     * @param player
     * @param guild
     * @param role
     * @param name
     */
    @Subcommand("boot|kick")
    @Description("Kick someone from your Guild")
    @CommandPermission("guilds.command.boot")
    @CommandCompletion("@members")
    @Syntax("<name>")
    public void onKick(Player player, Guild guild, GuildRole role, @Values("@members") @Single String name) {
        if (!role.canKick()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(name);
        if (bootedPlayer == null) return;
        if (bootedPlayer.getUniqueId() == null) return;

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());
        if (kickedPlayer == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__PLAYER_NOT_IN_GUILD, "{player}", name);
            return;
        }

        Guild targetGuild = utils.getGuild(kickedPlayer.getUniqueId());
        if (targetGuild == null) return;
        if (!guild.getName().equals(targetGuild.getName())) return;

        if (kickedPlayer.equals(guild.getGuildMaster())) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        utils.removeGuildPerms(guild, bootedPlayer);
        utils.removeMember(guild, kickedPlayer.getUniqueId());
        getCurrentCommandIssuer().sendInfo(Messages.BOOT__SUCCESSFUL, "{player}", bootedPlayer.getName());
        utils.sendMessage(guild, Messages.BOOT__PLAYER_KICKED, "{player}", bootedPlayer.getName(), "{kicker}", player.getName());
        if (bootedPlayer.isOnline()) {
            guilds.getManager().getCommandIssuer(bootedPlayer).sendInfo(Messages.BOOT__KICKED, "{kicker}", player.getName());
        }
    }

    /**
     * Opens the guild vault
     * @param player
     * @param guild
     * @param role
     */
    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @CommandPermission("guilds.command.vault")
    public void onVault(Player player, Guild guild, GuildRole role) {
        if (!role.canOpenVault()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        player.openInventory(guilds.getVaults().get(guild));
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
        utils.updateGuilds();

        getCurrentCommandIssuer().sendInfo(Messages.DEMOTE__DEMOTE_SUCCESSFUL, "{player}", demotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (demotedPlayer.isOnline()) {
            guilds.getManager().getCommandIssuer(demotedPlayer).sendInfo(Messages.DEMOTE__YOU_WERE_DEMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    /**
     * Promote a player in the guild
     * @param player
     * @param target
     * @param guild
     * @param role
     */
    @Subcommand("promote")
    @Description("{@@descriptions.promote}")
    @CommandPermission("guilds.command.promote")
    @CommandCompletion("@members")
    @Syntax("<player>")
    public void onPromote(Player player, @Values("@members") @Single String target, Guild guild, GuildRole role) {
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
        utils.updateGuilds();

        getCurrentCommandIssuer().sendInfo(Messages.PROMOTE__PROMOTE_SUCCESSFUL, "{player}", promotedPlayer.getName(), "{old}", oldRank, "{new}", newRank);
        if (promotedPlayer.isOnline()) {
            guilds.getManager().getCommandIssuer(promotedPlayer).sendInfo(Messages.PROMOTE__YOU_WERE_PROMOTED, "{old}", oldRank, "{new}", newRank);
        }
    }

    /**
     * Accept a guild invite
     * @param player
     * @param name
     */
    @Subcommand("accept|join")
    @Description("{@@descriptions.accept}")
    @CommandPermission("guilds.command.accept")
    @CommandCompletion("@invitedTo")
    @Syntax("<guild name>")
    public void onAccept(Player player, @Values("@invitedTo") @Single String name) {
        if (utils.getGuild(player.getUniqueId()) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }
        if (guilds.getGuildHandler().getGuilds().values().size() == 0) return;
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
                if (utils.getGuild2(name) != null) {
                    guild = utils.getGuild2(name);
                } else {
                    OfflinePlayer tempPlayer = Bukkit.getOfflinePlayer(name);
                    if (tempPlayer != null) {
                        guild = utils.getGuild(tempPlayer.getUniqueId());
                    }
                }
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

        if (guild.getMembers().size() >= utils.getMaxMembers(guild)) {
            getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        guilds.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        utils.sendMessage(guild, Messages.ACCEPT__PLAYER_JOINED, "{player}", player.getName());
        utils.addMember(guild, player.getUniqueId(), GuildRole.getLowestRole());
        utils.removeInvitedPlayer(guild, player.getUniqueId());
        getCurrentCommandIssuer().sendInfo(Messages.ACCEPT__SUCCESSFUL, "{guild}", guild.getName());
    }

    /**
     * Check if you have any guild invites
     * @param player
     */
    @Subcommand("check")
    @Description("{@@descriptions.check}")
    @CommandPermission("guilds.command.check")
    public void onCheck(Player player) {
        Guild guild2 = utils.getGuild(player.getUniqueId());
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
            getCurrentCommandIssuer().sendInfo(Messages.PENDING__INVITES, "{number}", String.valueOf(guildList.size()), "{guilds}", String.join(",", guildList));
        }
    }

    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission("guilds.command.request")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onRequest(Player player, @Values("@guilds") @Single String name) {
        Guild guild = utils.getGuild(player.getUniqueId());
        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }
        Guild targetGuild = utils.getGuild2(name);
        if (targetGuild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        for (GuildMember member : targetGuild.getMembers()) {
            GuildRole role = GuildRole.getRole(member.getRole());
            if (role.canInvite()) {
                OfflinePlayer guildPlayer = Bukkit.getOfflinePlayer(member.getUniqueId());
                if (guildPlayer.isOnline()) {
                    guilds.getManager().getCommandIssuer(guildPlayer).sendInfo(Messages.REQUEST__INCOMING_REQUEST, "{player}", player.getName());
                }
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.REQUEST__SUCCESS, "{guild}", targetGuild.getName());
    }

    /**
     * Open the guild buff menu
     * @param player
     * @param guild
     * @param role
     */
    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @CommandPermission("guilds.command.buff")
    public void onBuff(Player player, Guild guild, GuildRole role) {
        if (!role.canActivateBuff()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Inventory buff = Bukkit.createInventory(null, 9, getString("gui-name.buff"));
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

    /**
     * Help command for the plugin
     * @param help
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
        ItemStack paper = new ItemStack(mat);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        paper.setItemMeta(meta);
        return paper;
    }

    /**
     * Create an item for buff
     * @param buffName
     * @param name
     * @param buff
     * @param slot
     */
    private void createBuffItem(String buffName, List<String> name, Inventory buff, int slot) {
        getStringList("buff.description." + buffName).stream().map(ConfigUtils::color).forEach(name::add);
        name.add("");
        name.add(getString("buff.description.price") + getString("buff.price." + buffName));
        name.add(getString("buff.description.length") + getString("buff.time." + buffName));
        if (getBoolean("buff.display." + buffName)) {
            buff.setItem(slot, createItemStack(Material.getMaterial(getString("buff.icon." + buffName)), getString("buff.name." + buffName), name));
        }
        name.clear();
    }

    /**
     * Handling for the list page
     * @param page
     * @return
     */
    public static Inventory getSkullsPage(int page) {
        Map<UUID, ItemStack> skulls = new HashMap<>();
        Inventory inv = Bukkit.createInventory(null, 54, getString("guild-list.gui-name"));

        int startIndex = 0;
        int endIndex = 0;

        Guilds.getGuilds().getGuildHandler().getGuilds().values().forEach(guild -> {
            ItemStack skull = HeadUtils.getSkull(HeadUtils.getTextureUrl(guild.getGuildMaster().getUniqueId()));
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            List<String> lore = new ArrayList<>();

            getStringList("guild-list.head-lore").forEach(line -> lore.add(color(line)
                    .replace("{guild-name}", guild.getName())
                    .replace("{guild-prefix}", guild.getPrefix())
                    .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
                    .replace("{guild-status}", guild.getStatus())
                    .replace("{guild-tier}", String.valueOf(guild.getTier()))
                    .replace("{guild-balance}", String.valueOf(guild.getBalance()))
                    .replace("{guild-member-count}", String.valueOf(guild.getMembers().size()))));

            skullMeta.setLore(lore);

            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
            skullMeta.setDisplayName(getString("guild-list.item-name").replace("{player}", name).replace("{guild-name}", guild.getName()));
            skull.setItemMeta(skullMeta);
            skulls.put(guild.getGuildMaster().getUniqueId(), skull);
        });

        ItemStack previous = new ItemStack(Material.getMaterial(getString("guild-list.previous-page-item")), 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(getString("guild-list.previous-page-item-name"));
        previous.setItemMeta(previousMeta);
        ItemStack next = new ItemStack(Material.getMaterial(getString("guild-list.next-page-item")), 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(getString("guild-list.next-page-item-name"));
        next.setItemMeta(nextMeta);
        ItemStack barrier = new ItemStack(Material.getMaterial(getString("guild-list.page-number-item")), 1);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(getString("guild-list.page-number-item-name").replace("{page}", String.valueOf(page)));
        barrier.setItemMeta(barrierMeta);
        inv.setItem(53, next);
        inv.setItem(49, barrier);
        inv.setItem(45, previous);

        startIndex = (page - 1) * 45;
        endIndex = startIndex + 45;

        if (endIndex > skulls.values().size()) {
            endIndex = skulls.values().size();
        }

        int iCount = 0;
        for (int i1 = startIndex; i1 < endIndex; i1++) {
            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
            iCount++;
        }

        return inv;
    }

    /**
     * Create player skull
     * @param player
     * @return
     */
    public ItemStack createSkull(Player player) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(getString("info.playername").replace("{player-name}", player.getName()));

        List<String> info = new ArrayList<>();
        info.add(getString("info.kills").replace("{kills}", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS))));
        info.add(getString("info.deaths").replace("{deaths}", String.valueOf(player.getStatistic(Statistic.DEATHS))));
        meta.setLore(info);

        skull.setItemMeta(meta);
        return skull;
    }

    private boolean meetsCost(Player player, String type) {
        if (getDouble(type) > 0) {
            if (guilds.getEconomy().getBalance(player) < getDouble(type)) {
                getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
                return true;
            }
        }
        return false;
    }

}
