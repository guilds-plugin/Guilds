/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
import co.aikar.commands.ACFUtil;
import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildListSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Guild {

    public Guild(UUID id) {
        this.id = id;
    }

    public Guild(UUID id, String name, String prefix, String motd, GuildMember guildMaster, GuildHome home, GuildSkull guildSkull, Status status, GuildTier tier, GuildScore guildScore, double balance, List<GuildMember> members, List<UUID> invitedMembers, List<UUID> allies, List<UUID> pendingAllies, List<GuildCode> codes, List<String> vaults, long lastDefended) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.motd = motd;
        this.guildMaster = guildMaster;
        this.home = home;
        this.guildSkull = guildSkull;
        this.status = status;
        this.tier = tier;
        this.guildScore = guildScore;
        this.balance = balance;
        this.members = members;
        this.invitedMembers = invitedMembers;
        this.allies = allies;
        this.pendingAllies = pendingAllies;
        this.codes = codes;
        this.vaults = vaults;
        this.lastDefended = lastDefended;
    }

    public static GuildBuilder builder() {
        return new GuildBuilder();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public void setGuildMaster(GuildMember guildMaster) {
        this.guildMaster = guildMaster;
    }

    public void setHome(GuildHome home) {
        this.home = home;
    }

    public void setGuildSkull(GuildSkull guildSkull) {
        this.guildSkull = guildSkull;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setTier(GuildTier tier) {
        this.tier = tier;
    }

    public void setGuildScore(GuildScore guildScore) {
        this.guildScore = guildScore;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setMembers(List<GuildMember> members) {
        this.members = members;
    }

    public void setInvitedMembers(List<UUID> invitedMembers) {
        this.invitedMembers = invitedMembers;
    }

    public void setAllies(List<UUID> allies) {
        this.allies = allies;
    }

    public void setPendingAllies(List<UUID> pendingAllies) {
        this.pendingAllies = pendingAllies;
    }

    public void setCodes(List<GuildCode> codes) {
        this.codes = codes;
    }

    public void setVaults(List<String> vaults) {
        this.vaults = vaults;
    }

    public void setLastDefended(long lastDefended) {
        this.lastDefended = lastDefended;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public enum Status {
        Public("Public"),
        Private("Private");

        Status(String s) {
        }
    }

    private final UUID id;
    private String name, prefix, motd;
    private GuildMember guildMaster;

    private GuildHome home;
    private GuildSkull guildSkull;
    private Status status;
    private GuildTier tier;
    private GuildScore guildScore;
    private double balance;

    private List<GuildMember> members;

    private List<UUID> invitedMembers;
    private List<UUID> allies;
    private List<UUID> pendingAllies;

    private List<GuildCode> codes;

    private List<String> vaults;

    private long lastDefended;
    private long creationDate;

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Get the itemstack of the guild skull
     * @return itemstack of skull
     */
    public ItemStack getSkull() {
        return SkullUtils.getSkull(guildSkull.getSerialized());
    }


    /**
     * Add player to a guild
     * @param player the player being added
     * @param guildHandler guild handler
     */
    public void addMember(OfflinePlayer player, GuildHandler guildHandler) {
        GuildMember member = new GuildMember(player.getUniqueId(), guildHandler.getLowestGuildRole());
        if (members.contains(member)) return;
        removeInvitedMember(member.getUuid());
        members.add(member);
        member.setJoinDate(System.currentTimeMillis());
    }

    /**
     * Invite a member by guild code
     * @param guildMember
     */
    public void addMemberByCode(GuildMember guildMember) {
        members.add(guildMember);
    }

    /**
     * Remove a member by their GuildMember object
     * @param guildMember the guildmember to remove
     */
    public void removeMember(GuildMember guildMember){
        members.remove(guildMember);
    }

    /**
     * Remove a member using it's OfflinePlayer object
     * @param player the OfflinePlayer to remove
     */
    public void removeMember(OfflinePlayer player){
        removeMember(getMember(player.getUniqueId()));
    }

    /**
     * Removes an ally's id from the list
     * @param guild the guild to remove
     */
    public void removeAlly(Guild guild) {
        allies.remove(guild.getId());
    }

    /**
     * Add an ally's id to the list
     * @param guild the guild to add
     */
    public void addAlly(Guild guild) {
        allies.add(guild.getId());
    }

    /**
     * Simple method to check if a guild has allies
     * @return if they have allies or not
     */
    public boolean hasAllies() {
        return !allies.isEmpty();
    }

    /**
     * Check if a guild has pending allies
     * @return if a guild has pending allies or not
     */
    public boolean hasPendingAllies() {
        return !pendingAllies.isEmpty();
    }

    /**
     * Adds a pending ally's id to the list
     * @param guild the guild to add
     */
    public void addPendingAlly(Guild guild) {
        pendingAllies.add(guild.getId());
    }

    /**
     * Check if an ally request is pending from another guild
     * @param guild the guild to check
     * @return if they have a pending invite
     */
    public boolean isAllyPending(Guild guild) {
        return pendingAllies.contains(guild.getId());
    }

    /**
     * Removes a pending ally's id from the list
     * @param guild the guild to remove
     */
    public void removePendingAlly(Guild guild) {
        pendingAllies.remove(guild.getId());
    }

    /**
     * Invites a member to this guild.
     *
     * @param uuid the UUID of the player.
     */
    public void inviteMember(UUID uuid) {
        if (invitedMembers.contains(uuid)) return;
        invitedMembers.add(uuid);
    }

    /**
     * Create a simple method to check if invited
     * @param player the player being checked
     * @return invited or not
     */
    public boolean checkIfInvited(Player player) {
        return invitedMembers.contains(player.getUniqueId());
    }

    /**
     * Removes an invited member
     *
     * @param uuid the member to remove from the invites.
     */
    public void removeInvitedMember(UUID uuid) {
        invitedMembers.remove(uuid);
    }

    /**
     * Get the amount of members
     * @return size of members list.
     */
    public int getSize() {
        return members.size();
    }

    /**
     * Returns amount of online members
     *
     * @return a List of online members
     */
    public List<GuildMember> getOnlineMembers() {
        return members.stream().filter(GuildMember::isOnline).collect(Collectors.toList());
    }

    /**
     * Get all online members as players
     * @return list of players
     */
    public List<Player> getOnlineAsPlayers() {
        return getOnlineMembers().stream().map(m -> Bukkit.getPlayer(m.getUuid())).collect(Collectors.toList());
    }

    /**
     * Get all online members as UUID
     * @return list of UUIDs
     */
    public List<UUID> getOnlineAsUUIDs() {
        return getOnlineMembers().stream().map(GuildMember::getUuid).collect(Collectors.toList());
    }

    /**
     * Get all players as a list
     * @return list of players
     */
    public List<OfflinePlayer> getAllAsPlayers() {
        return members.stream().map(GuildMember::getAsOfflinePlayer).collect(Collectors.toList());
    }

    /**
     * Check if a guild has a code
     * @param code the code being checked
     * @return if the guild has it or not
     */
    public boolean hasInviteCode(String code) {
        return codes.stream().anyMatch(c -> c.getId().equals(code));
    }

    /**
     * Get a guild code object by the id
     * @param code the code looking for
     * @return the guild code object
     */
    public GuildCode getCode(String code) {
        return codes.stream().filter(c -> c.getId().equals(code)).findFirst().orElse(null);
    }

    /**
     * Get a list of active codes
     * @return list of active codes
     */
    public List<GuildCode> getActiveCodes() {
        return codes.stream().filter(c -> c.getUses() > 0).collect(Collectors.toList());
    }

    /**
     * Add a new code to a guild
     * @param code the code being added
     * @param uses the amount of uses it has
     * @param creator the creator of the code
     */
    public void addCode(String code, int uses, Player creator) {
        codes.add(new GuildCode(code, uses, creator.getUniqueId(), new ArrayList<>()));
    }

    /**
     * Remove a code from a Guild if it exists
     * @param code the code being checked
     */
    public void removeCode(String code) {
        codes.removeIf(s -> s.getId().equals(code));
    }

    /**
     * Checks if they can make another code
     * @param amount the max amount
     * @return if they can make it or not
     */
    public boolean getActiveCheck(int amount) {
        return (getActiveCodes().size() >= amount);
    }

    /**
     * Get all the redeemers of a code
     * @param code the code being checked
     * @return a list of all redeemers for a code
     */
    public String getRedeemers(String code) {
        GuildCode gc = getCode(code);
        if (gc.getRedeemers().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        gc.getRedeemers().forEach(r -> builder.append(Bukkit.getOfflinePlayer(r).getName() + ", "));
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    /**
     * Send a message to the guild
     * @param manager get the manager to send custom messages
     * @param key the message to send
     * @param replacements any args we need to handle
     */
    public void sendMessage(CommandManager manager, Messages key, String... replacements) {
        getOnlineMembers().forEach(m -> manager.getCommandIssuer(Bukkit.getPlayer(m.getUuid())).sendInfo(key, replacements));
    }

    /**
     * Send a message to the guild
     * @param message the message to send
     */
    public void sendMessage(String message) {
        getOnlineAsPlayers().forEach(m -> m.sendMessage(message));
    }

    /**
     * Toggle the status of a guild
     */
    public void toggleStatus() {
        if (status == Status.Private) {
            setStatus(Status.Public);
        } else {
            setStatus(Status.Private);
        }
    }

    /**
     * Super simple method to set a home's location to null
     */
    public void delHome() {
        setHome(null);
    }

    /**
     * Simple method to create a new guild home
     * @param player the player running the command
     */
    public void setNewHome(Player player) {
        Location l = player.getLocation();
        setHome(new GuildHome(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
    }

    /**
     * Simple method to check if a guild is public or private
     * @return public or private
     */
    public boolean isPrivate() {
        return status == Status.Private;
    }

    /**
     * Simple method to check if the player in question is the guild master
     * @param player the player in question
     * @return master or not
     */
    public boolean isMaster(OfflinePlayer player) {
        return guildMaster.getUuid().equals(player.getUniqueId());
    }

    /**
     * Transfer a guild from one player to another
     * @param oldPlayer old player
     * @param newPlayer new player
     */
    public void transferGuild(Player oldPlayer, OfflinePlayer newPlayer) {

        GuildMember oldMaster = getMember(oldPlayer.getUniqueId());
        GuildMember newMaster = getMember(newPlayer.getUniqueId());

        GuildRole gm = oldMaster.getRole();

        if (newMaster.getRole().getLevel() != 1)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_OFFICER));

        oldMaster.setRole(newMaster.getRole());
        newMaster.setRole(gm);
        setGuildMaster(newMaster);
    }

    /**
     * Transfer a guild to another player via admin command
     * @param master The new master of the guild
     * @param guildHandler Guild handler
     */
    public void transferGuildAdmin(OfflinePlayer master, GuildHandler guildHandler) {

        GuildMember currentMaster = getMember(getGuildMaster().getUuid());
        GuildMember newMaster = getMember(master.getUniqueId());

        GuildRole masterRole = currentMaster.getRole();

        currentMaster.setRole(guildHandler.getGuildRole(masterRole.getLevel() + 1));
        newMaster.setRole(guildHandler.getGuildRole(0));

        setGuildMaster(newMaster);
    }

    /**
     * Simple method to add a buff to all online members
     * @param type the potion type
     * @param length the length of the potion
     * @param amplifier the strength of the potion
     */
    public void addPotion(String type, int length, int amplifier) {
        getOnlineAsPlayers().forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(type), length, amplifier)));
    }

    /**
     * Determine if a player has role permission
     * @param player the player to check
     * @param perm the permission as a string to check
     * @return has permission or not
     */
    public boolean memberHasPermission(Player player, String perm) {
        return memberHasPermission(player, GuildRolePerm.valueOf(perm));
    }

    /**
     * Determine if a player has role permission
     * @param player the player to check
     * @param perm the permission as an enum value
     * @return has permission or not
     */
    public boolean memberHasPermission(Player player, GuildRolePerm perm) {
        GuildMember member = getMember(player.getUniqueId());
        GuildRole role = member.getRole();
        return role.hasPerm(perm);
    }

    public void updateGuildSkull(Player player, SettingsManager settingsManager) {
        Guilds.newChain().async(() -> {
            try{
                guildSkull = new GuildSkull(player);
            } catch (Exception ex) {
                guildSkull = new GuildSkull(settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_DEFAULT_URL));
            }
        }).execute();
    }

    public void addPotion(PotionEffect effect) {
        getOnlineAsPlayers().forEach(p -> p.addPotionEffect(effect));
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getMotd() {
        return this.motd;
    }

    public GuildMember getGuildMaster() {
        return this.guildMaster;
    }

    public GuildHome getHome() {
        return this.home;
    }

    public GuildSkull getGuildSkull() {
        return this.guildSkull;
    }

    public Status getStatus() {
        return this.status;
    }

    public GuildTier getTier() {
        return this.tier;
    }

    public double getBalance() {
        return this.balance;
    }

    public List<GuildMember> getMembers() {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        return this.members;
    }

    public List<UUID> getInvitedMembers() {
        if (this.invitedMembers == null) {
            this.invitedMembers = new ArrayList<>();
        }
        return this.invitedMembers;
    }

    public List<UUID> getAllies() {
        if (this.allies == null) {
            this.allies = new ArrayList<>();
        }
        return this.allies;
    }

    public List<UUID> getPendingAllies() {
        if (this.pendingAllies == null) {
            this.pendingAllies = new ArrayList<>();
        }
        return this.pendingAllies;
    }

    public GuildScore getGuildScore() {
        if (this.guildScore == null) {
            this.guildScore = new GuildScore();
        }
        return this.guildScore;
    }

    public List<GuildCode> getCodes() {
        if (this.codes == null) {
            this.codes = new ArrayList<>();
        }
        return this.codes;
    }

    public List<String> getVaults() {
        if (this.vaults == null) {
            this.vaults = new ArrayList<>();
        }
        return this.vaults;
    }

    public long getLastDefended() {
        return lastDefended;
    }

    public static class GuildBuilder {
        private UUID id;
        private String name;
        private String prefix;
        private String motd;
        private GuildMember guildMaster;
        private GuildHome home;
        private GuildSkull guildSkull;
        private Status status;
        private GuildTier tier;
        private GuildScore guildScore;
        private double balance;
        private List<GuildMember> members;
        private List<UUID> invitedMembers;
        private List<UUID> allies;
        private List<UUID> pendingAllies;
        private List<GuildCode> codes;
        private List<String> vaults;
        private long lastDefended;

        GuildBuilder() {
        }

        public Guild.GuildBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public Guild.GuildBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Guild.GuildBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Guild.GuildBuilder motd(String motd) {
            this.motd = motd;
            return this;
        }

        public Guild.GuildBuilder guildMaster(GuildMember guildMaster) {
            this.guildMaster = guildMaster;
            return this;
        }

        public Guild.GuildBuilder home(GuildHome home) {
            this.home = home;
            return this;
        }

        public Guild.GuildBuilder guildSkull(GuildSkull guildSkull) {
            this.guildSkull = guildSkull;
            return this;
        }

        public Guild.GuildBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public Guild.GuildBuilder tier(GuildTier tier) {
            this.tier = tier;
            return this;
        }

        public Guild.GuildBuilder guildScore(GuildScore guildScore) {
            this.guildScore = guildScore;
            return this;
        }

        public Guild.GuildBuilder balance(double balance) {
            this.balance = balance;
            return this;
        }

        public Guild.GuildBuilder members(List<GuildMember> members) {
            this.members = members;
            return this;
        }

        public Guild.GuildBuilder invitedMembers(List<UUID> invitedMembers) {
            this.invitedMembers = invitedMembers;
            return this;
        }

        public Guild.GuildBuilder allies(List<UUID> allies) {
            this.allies = allies;
            return this;
        }

        public Guild.GuildBuilder pendingAllies(List<UUID> pendingAllies) {
            this.pendingAllies = pendingAllies;
            return this;
        }

        public Guild.GuildBuilder codes(List<GuildCode> codes) {
            this.codes = codes;
            return this;
        }

        public Guild.GuildBuilder vaults(List<String> vaults) {
            this.vaults = vaults;
            return this;
        }

        public Guild.GuildBuilder lastDefended(long lastDefended) {
            this.lastDefended = lastDefended;
            return this;
        }

        public Guild build() {
            return new Guild(id, name, prefix, motd, guildMaster, home, guildSkull, status, tier, guildScore, balance, members, invitedMembers, allies, pendingAllies, codes, vaults, lastDefended);
        }

        public String toString() {
            return "Guild.GuildBuilder(id=" + this.id + ", name=" + this.name + ", prefix=" + this.prefix + ", motd=" + this.motd + ", guildMaster=" + this.guildMaster + ", home=" + this.home + ", guildSkull=" + this.guildSkull + ", status=" + this.status + ", tier=" + this.tier + ", guildScore=" + this.guildScore + ", balance=" + this.balance + ", members=" + this.members + ", invitedMembers=" + this.invitedMembers + ", allies=" + this.allies + ", pendingAllies=" + this.pendingAllies + ", codes=" + this.codes + ", vaults=" + this.vaults + ", lastDefended=" + this.lastDefended + ")";
        }
    }
}

