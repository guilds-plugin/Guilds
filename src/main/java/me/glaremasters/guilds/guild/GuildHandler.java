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

package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandManager;
import lombok.Getter;
import me.glaremasters.guilds.configuration.sections.TicketSettings;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.utils.ItemBuilder;
import me.glaremasters.guilds.utils.Serialization;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuildHandler {


    private List<Guild> guilds;
    private final List<GuildRole> roles;
    private final List<GuildTier> tiers;
    @Getter private final List<Player> spies;
    @Getter private final List<Player> guildChat;

    @Getter private Map<Guild, List<Inventory>> cachedVaults;

    private final DatabaseProvider databaseProvider;
    private final CommandManager commandManager;
    private final Permission permission;

    //as well as guild permissions from tiers using permission field and tiers list.

    public GuildHandler(DatabaseProvider databaseProvider, CommandManager commandManager, Permission permission, FileConfiguration config) throws IOException {
        this.databaseProvider = databaseProvider;
        this.commandManager = commandManager;
        this.permission = permission;

        roles = new ArrayList<>();
        tiers = new ArrayList<>();
        spies = new ArrayList<>();
        guildChat = new ArrayList<>();
        cachedVaults = new HashMap<>();

        //GuildRoles objects
        ConfigurationSection roleSection = config.getConfigurationSection("roles");

        for (String s : roleSection.getKeys(false)) {
            String path = s + ".permissions.";

            roles.add(GuildRole.builder().name(roleSection.getString(s + ".name"))
                    .node(roleSection.getString(s + ".permission-node"))
                    .level(Integer.parseInt(s))
                    .chat(roleSection.getBoolean(path + "chat"))
                    .allyChat(roleSection.getBoolean(path + "ally-chat"))
                    .invite(roleSection.getBoolean(path + "invite"))
                    .kick(roleSection.getBoolean(path + "kick"))
                    .promote(roleSection.getBoolean(path + "promote"))
                    .demote(roleSection.getBoolean(path + "demote"))
                    .addAlly(roleSection.getBoolean(path + "add-ally"))
                    .removeAlly(roleSection.getBoolean(path + "remove-ally"))
                    .changePrefix(roleSection.getBoolean(path + "change-prefix"))
                    .changeName(roleSection.getBoolean(path + "rename"))
                    .changeHome(roleSection.getBoolean(path + "change-home"))
                    .removeGuild(roleSection.getBoolean(path + "remove-guild"))
                    .changeStatus(roleSection.getBoolean(path + "toggle-guild"))
                    .openVault(roleSection.getBoolean(path + "open-vault"))
                    .transferGuild(roleSection.getBoolean(path + "transfer-guild"))
                    .activateBuff(roleSection.getBoolean(path + "activate-buff"))
                    .upgradeGuild(roleSection.getBoolean(path + "upgrade-guild"))
                    .depositMoney(roleSection.getBoolean(path + "deposit-money"))
                    .withdrawMoney(roleSection.getBoolean(path + "withdraw-money"))
                    .claimLand(roleSection.getBoolean(path + "claim-land"))
                    .unclaimLand(roleSection.getBoolean(path + "unclaim-land"))
                    .destroy(roleSection.getBoolean(path + "destroy"))
                    .place(roleSection.getBoolean(path + "place"))
                    .interact(roleSection.getBoolean(path + "interact"))
                    .createCode(roleSection.getBoolean(path + "create-code"))
                    .deleteCode(roleSection.getBoolean(path + "delete-code"))
                    .seeCodeRedeemers(roleSection.getBoolean(path + "see-code-redeemers"))
                    .build());
        }


        //GuildTier objects
        ConfigurationSection tierSection = config.getConfigurationSection("tiers.list");
        for (String key : tierSection.getKeys(false)) {
            tiers.add(GuildTier.builder()
                    .level(tierSection.getInt(key + ".level"))
                    .name(tierSection.getString(key + ".name"))
                    .cost(tierSection.getDouble(key + ".cost"))
                    .maxMembers(tierSection.getInt(key + ".max-members"))
                    .vaultAmount(tierSection.getInt(key + ".vault-amount"))
                    .mobXpMultiplier(tierSection.getDouble(key + ".mob-xp-multiplier"))
                    .damageMultiplier(tierSection.getDouble(key + ".damage-multiplier"))
                    .maxBankBalance(tierSection.getDouble(key + ".max-bank-balance"))
                    .membersToRankup(tierSection.getInt(key + ".members-to-rankup"))
                    .permissions(tierSection.getStringList(key + ".permissions"))
                    .build());
        }

        guilds = databaseProvider.loadGuilds();


        guilds.forEach(this::createVaultCache);

    }

    /**
     * Saves the data of guilds
     */
    public void saveData() throws IOException {
        guilds.forEach(this::saveVaultCache);
        databaseProvider.saveGuilds(guilds);
    }


    /**
     * This method is used to add a Guild to the list
     *
     * @param guild the guild being added
     */
    public void addGuild(@NotNull Guild guild) {
        guilds.add(guild);
        createVaultCache(guild);
    }

    /**
     * This method is used to remove a Guild from the list
     *
     * @param guild the guild being removed
     */
    public void removeGuild(@NotNull Guild guild) {
        cachedVaults.remove(guild);
        guilds.remove(guild);
    }

    /**
     * Retrieve a guild by it's name
     *
     * @return the guild object with given name
     */
    public Guild getGuild(@NotNull String name) {
        return guilds.stream().filter(guild -> guild.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild by a player
     *
     * @return the guild object by player
     */
    public Guild getGuild(@NotNull OfflinePlayer p) {
        return guilds.stream().filter(guild -> guild.getMember(p.getUniqueId()) != null).findFirst().orElse(null);
    }

    /**
     * Gets a guild by it's uuid
     * @param uuid the input
     * @return the output
     */
    public Guild getGuild(@NotNull UUID uuid) {
        return guilds.stream().filter(guild -> guild.getId().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Check the guild based on the invite code
     *
     * @param code the invite code being used
     * @return the guild who the code belong to
     */
    public Guild getGuildByCode(@NotNull String code) {
        return guilds.stream().filter(guild -> guild.hasInviteCode(code)).findFirst().orElse(null);
    }

    /**
     * Get a guild's name by it's ID
     * @param uuid the input ID
     * @return the output guild
     */
    public String getNameById(@NotNull UUID uuid) {
        return getGuild(uuid).getName();
    }

    /**
     * Retrieve a guild tier by name
     *
     * @param name the name of the tier
     * @return the tier object if found
     */
    public GuildTier getGuildTier(String name) {
        return tiers.stream().filter(tier -> tier.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild tier by level
     *
     * @param level the level of the tier
     * @return the tier object if found
     */
    public GuildTier getGuildTier(int level) {
        return tiers.stream().filter(tier -> tier.getLevel() == level).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild role by level
     *
     * @param level the level of the role
     * @return the role object if found
     */
    public GuildRole getGuildRole(int level) {
        return roles.stream().filter(guildRole -> guildRole.getLevel() == level).findFirst().orElse(null);
    }

    /**
     * Adds an ally to both guilds
     *
     * @param guild       the guild to ally
     * @param targetGuild the other guild to ally
     */
    public void addAlly(Guild guild, Guild targetGuild) {
        guild.addAlly(targetGuild);
        targetGuild.addAlly(guild);
        removePendingAlly(guild, targetGuild);
    }

    /**
     * Simple method to check if two guilds are allies
     * @param guild the first guild
     * @param target the second guild
     * @return allies or not
     */
    public boolean isAlly(Guild guild, Guild target) {
        return guild.getAllies().contains(target.getId());
    }


    /**
     * Removes an ally.
     *
     * @param guild       the guild to remove as ally
     * @param targetGuild the guild to remove as ally
     */
    public void removeAlly(Guild guild, Guild targetGuild) {
        guild.removeAlly(targetGuild);
        targetGuild.removeAlly(guild);
    }

    /**
     * Adds a pending ally
     *
     * @param guild       the first pending guild
     * @param targetGuild the second pending guild
     */
    public void addPendingAlly(Guild guild, Guild targetGuild) {
        guild.addPendingAlly(targetGuild);
        targetGuild.addPendingAlly(guild);
    }

    /**
     * Removes a pending ally
     *
     * @param guild       the first pending guild
     * @param targetGuild the second pending guild
     */
    public void removePendingAlly(Guild guild, Guild targetGuild) {
        guild.removePendingAlly(targetGuild);
        targetGuild.removePendingAlly(guild);
    }

    /**
     * Returns the amount of guilds existing
     *
     * @return an integer of size.
     */
    public int getGuildsSize() {
        return guilds.size();
    }

    /**
     * Returns the max tier level
     *
     * @return the max tier level
     */
    public int getMaxTierLevel() {
        return tiers.size();
    }

    /**
     * Simple method to check if guild is max tier
     * @param guild the guild to check
     * @return if they are max or not
     */
    public boolean isMaxTier(Guild guild) {
        return guild.getTier().getLevel() >= getMaxTierLevel();
    }

    /**
     * Simple method to increment a guild's tier
     * @param guild the tier being upgraded
     */
    public void incrementTier(Guild guild) {
        guild.setTier(getGuildTier(guild.getTier().getLevel() + 1));
    }

    /**
     * Returns the lowest guild role
     *
     * @return guild role
     */
    public GuildRole getLowestGuildRole() {
        return roles.get(roles.size() - 1);
    }

    /**
     * Upgrades the tier of a guild
     *
     * @param guild the guild upgrading
     */
    public void upgradeTier(Guild guild) {
        guild.setTier(getGuildTier(guild.getTier().getLevel() + 1));
    }

    /**
     * Returns a string list of all the guilds that a member is invited to
     *
     * @param uuid the uuid of the member
     * @return a string list of guilds's names.
     */
    public List<String> getInvitedGuilds(UUID uuid) {
        return guilds.stream().filter(guild -> guild.getInvitedMembers().contains(uuid)).map(Guild::getName).collect(Collectors.toList());
    }

    /**
     * Returns a string list of the name of all guilds on the server
     *
     * @return a string list of guild names
     */
    public List<String> getGuildNames() {
        return guilds.stream().map(Guild::getName).collect(Collectors.toList());
    }

    /**
     * Create the cache of a vault for the guild
     *
     * @param guild the guild being cached
     */
    private void createVaultCache(Guild guild) {
        List<Inventory> vaults = new ArrayList<>();
        if (guild.getVaults() == null) return;
        guild.getVaults().forEach(v -> {
            try {
                vaults.add(Serialization.deserializeInventory(v));
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });
        cachedVaults.put(guild, vaults);
    }

    /**
     * Save the vaults of a guild
     *
     * @param guild the guild being saved
     */
    private void saveVaultCache(Guild guild) {
        List<String> vaults = new ArrayList<>();
        if (guild.getVaults() == null) return;
        cachedVaults.get(guild).forEach(v -> vaults.add(Serialization.serializeInventory(v)));
        guild.setVaults(vaults);
    }

    /**
     * Open a guild vault
     *
     * @param guild the owner of the vault
     * @param vault which vault to open
     * @return the inventory to open
     */
    public Inventory getGuildVault(Guild guild, int vault) {
        return cachedVaults.get(guild).get(vault - 1);
    }

    /**
     * Check if player is a spy
     *
     * @param player the player being checked
     * @return if they are a spy
     */
    private boolean isSpy(Player player) {
        return spies.contains(player);
    }

    /**
     * Add a player to the list of spies
     *
     * @param player player being added
     */
    private void addSpy(CommandManager manager, Player player) {
        spies.add(player);
        manager.getCommandIssuer(player).sendInfo(Messages.ADMIN__SPY_ON);
    }

    /**
     * Remove a player from the list of spies
     *
     * @param player player being removed
     */
    public void removeSpy(CommandManager manager, Player player) {
        spies.remove(player);
        manager.getCommandIssuer(player).sendInfo(Messages.ADMIN__SPY_OFF);
    }

    /**
     * This method handles combining all the spy methods together to make a simple, clean method.
     *
     * @param player the player being modified
     */
    public void toggleSpy(CommandManager manager, Player player) {
        if (isSpy(player)) {
            removeSpy(manager, player);
        } else {
            addSpy(manager, player);
        }
    }

    /**
     * Check if a player is in guild chat mode or not
     *
     * @param player the player being checked
     * @return if they are in the mode or not
     */
    public boolean checkGuildChat(Player player) {
        return guildChat.contains(player);
    }

    /**
     * Add a player to guild chat mode
     *
     * @param player the player being checked
     */
    private void addGuildChat(CommandManager manager, Player player) {
        guildChat.add(player);
        manager.getCommandIssuer(player).sendInfo(Messages.CHAT__ENABLED);
    }

    /**
     * Remove a player from guild chat mode
     *
     * @param player the player being checked
     */
    public void removeGuildChat(CommandManager manager, Player player) {
        guildChat.remove(player);
        manager.getCommandIssuer(player).sendInfo(Messages.CHAT__DISABLED);
    }

    /**
     * Handler for taking players in and out of guild chat
     *
     * @param player the player being toggled
     */
    public void toggleGuildChat(CommandManager manager, Player player) {
        if (checkGuildChat(player)) {
            removeGuildChat(manager, player);
        } else {
            addGuildChat(manager, player);
        }
    }

    /**
     * This method is ran when a player logs out to ensure they aren't in the list.
     * @param player player being removed
     */
    public void chatLogout(Player player) {
        spies.remove(player);
        guildChat.remove(player);
    }

    /**
     * Simple method to check a player has any invites
     * @param manager the command manager
     * @param player the player being checked
     */
    public void checkInvites(CommandManager manager, Player player) {
        List<String> list = getInvitedGuilds(player.getUniqueId());

        if (list.isEmpty()) {
            manager.getCommandIssuer(player).sendInfo(Messages.ERROR__NO_GUILD);
            return;
        }

        manager.getCommandIssuer(player).sendInfo(Messages.PENDING__INVITES, "{number}", String.valueOf(list.size()), "{guilds}", String.join(",", list));
    }

    /**
     * Basically check if they can upgrade with the member check
     * @param guild the guild being checked
     * @return if they pass the check or not
     */
    public boolean memberCheck(Guild guild) {
        GuildTier tier = guild.getTier();
        return tier.getMembersToRankup() != 0 && guild.getMembers().size() < tier.getMembersToRankup();
    }

    /**
     * Check in a input name for the guild is proper
     * @param name the name input
     * @param settingsManager setting manager
     * @return valid or not
     */
    public boolean nameCheck(String name, SettingsManager settingsManager) {
        String regex = settingsManager.getProperty(GuildSettings.NAME_REQUIREMENTS);
        return name.matches(regex);
    }

    /**
     * Simple method to check in a prefix is valid or not
     * @param name the prefix
     * @param settingsManager setting manager
     * @return valid or not
     */
    public boolean prefixCheck(String name, SettingsManager settingsManager) {
        String regex = settingsManager.getProperty(GuildSettings.PREFIX_REQUIREMENTS);
        return name.matches(regex);
    }

    /**
     * Check if a word is in the blacklist or not
     * @param name name to check
     * @param settingsManager settings manager
     * @return blacklisted or not
     */
    public boolean blacklistCheck(String name, SettingsManager settingsManager) {
        return settingsManager.getProperty(GuildSettings.BLACKLIST_WORDS).stream().anyMatch(s -> s.toLowerCase().contains(name));
    }

    /**
     * Check if a guild has a specific vault unlocked
     * @param vault the vault being opened
     * @param guild the guild opening the vault
     * @return if they can open it or not
     */
    public boolean hasVaultUnlocked(Integer vault, Guild guild) {
        return vault > guild.getTier().getVaultAmount();
    }

    /**
     * Get a list of the online members that can invite people
     * @param guild the guild to check
     * @return list of online members
     */
    public List<Player> getOnlineInviters(Guild guild) {
        List<GuildMember> members = guild.getOnlineMembers().stream().filter(m -> m.getRole().isInvite()).collect(Collectors.toList());
        return members.stream().map(m -> Bukkit.getPlayer(m.getUuid())).collect(Collectors.toList());
    }

    /**
     * Simple method to inform all online inviters that someone wants to join
     * @param guild guild to be requested
     * @param commandManager command manager
     * @param player player requesting
     */
    public void pingOnlineInviters(Guild guild, CommandManager commandManager, Player player) {
        getOnlineInviters(guild).forEach(m -> commandManager.getCommandIssuer(m).sendInfo(Messages.REQUEST__INCOMING_REQUEST, "{player}", player.getName()));
    }

    /**
     * Create a guild upgrade ticket
     * @param settingsManager the settings manager
     * @param amount the amount of tickets to give
     * @return the guild upgrade ticket
     */
    public ItemStack getUpgradeTicket(SettingsManager settingsManager, int amount) {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(settingsManager.getProperty(TicketSettings.TICKET_MATERIAL)));
        builder.setAmount(amount);
        builder.setName(settingsManager.getProperty(TicketSettings.TICKET_NAME));
        builder.setLore(settingsManager.getProperty(TicketSettings.TICKET_LORE));
        return builder.build();
    }
}
