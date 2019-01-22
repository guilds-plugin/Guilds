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

import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.database.DatabaseProvider;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuildHandler {


    private List<Guild> guilds;
    private final List<GuildRole> roles;
    private final List<GuildTier> tiers;

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
                    .build());
        }


        //GuildTier objects
        ConfigurationSection tierSection = config.getConfigurationSection("tiers.list");
        for (String key : tierSection.getKeys(false)){
            tiers.add(GuildTier.builder()
                    .level(tierSection.getInt(key + ".level"))
                    .name(tierSection.getString(key + ".name"))
                    .cost(tierSection.getDouble(key + ".cost"))
                    .maxMembers(tierSection.getInt(key + ".max-members"))
                    .mobXpMultiplier(tierSection.getInt(key + ".mob-xp-multiplier"))
                    .damageMultiplier(tierSection.getInt(key + ".damage-multiplier"))
                    .maxBankBalance(tierSection.getDouble(key + ".max-bank-balance"))
                    .membersToRankup(tierSection.getInt(key + ".members-to-rankup"))
                    .permissions(tierSection.getStringList(key + ".permissions"))
                    .build());
        }

        guilds = databaseProvider.loadGuilds();

    }

    /**
     * Saves the data of guilds
     */
    public void saveData() throws IOException {
        databaseProvider.saveGuilds(guilds);
    }


    /**
     * This method is used to add a Guild to the list
     * @param guild the guild being added
     */
    public void addGuild(@NotNull Guild guild) {
        guilds.add(guild);
    }

    /**
     * This method is used to remove a Guild from the list
     * @param guild the guild being removed
     */
    public void removeGuild(@NotNull Guild guild) {
        guilds.remove(guild);
        //todo removeGuildPermissions();
    }

    /**
     * Retrieve a guild by it's name
     * @return the guild object with given name
     */
    public Guild getGuild(@NotNull String name) {
        return guilds.stream().filter(guild -> guild.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild by a player
     * @return the guild object by player
     */
    public Guild getGuild(@NotNull OfflinePlayer p){
        return guilds.stream().filter(guild -> guild.getMember(p.getUniqueId()) != null).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild tier by name
     * @param name the name of the tier
     * @return the tier object if found
     */
    public GuildTier getGuildTier(String name){
        return tiers.stream().filter(tier -> tier.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild tier by level
     * @param level the level of the tier
     * @return the tier object if found
     */
    public GuildTier getGuildTier(int level){
        return tiers.stream().filter(tier -> tier.getLevel() == level).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild role by level
     * @param level the level of the role
     * @return the role object if found
     */
    public GuildRole getGuildRole(int level){
        return roles.stream().filter(guildRole -> guildRole.getLevel() == level).findFirst().orElse(null);
    }

    /**
     * Sends a message to all online members in a guild
     * @param guild the guild from which members to send it to
     * @param key the message key to send
     * @param replacements any replacements
     */
    public void sendMessage(Guild guild, Messages key, String... replacements) {
        guild.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUuid())).filter(Objects::nonNull).forEach(p -> commandManager.getCommandIssuer(p).sendInfo(key, replacements));
    }

    /**
     * Adds an ally to both guilds
     * @param guild the guild to ally
     * @param targetGuild the other guild to ally
     */
    public void addAlly(Guild guild, Guild targetGuild) {
        guild.addAlly(targetGuild);
        targetGuild.addAlly(guild);
        removePendingAlly(guild, targetGuild);
    }


    /**
     * Removes an ally.
     * @param guild the guild to remove as ally
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
     * @return an integer of size.
     */
    public int getGuildsSize(){
        return guilds.size();
    }

    /**
     * Returns the max tier level
     * @return the max tier level
     */
    public int getMaxTierLevel() {
        return tiers.size();
    }

    /**
     * Returns the lowest guild role
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
}
