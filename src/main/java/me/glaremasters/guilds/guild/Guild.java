package me.glaremasters.guilds.guild;

import co.aikar.locales.MessageKeyProvider;
import com.google.gson.annotations.Expose;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class Guild {

    private static final Guilds guilds = Guilds.getGuilds();

    @Expose(serialize = false)
    private String name;

    @Expose
    private String prefix, home, status, inventory;

    @Expose
    private Integer tier;

    @Expose
    private Double balance;

    @Expose
    private List<GuildMember> members;

    @Expose
    private List<String> allies;

    @Expose
    private List<UUID> invitedMembers;

    @Expose
    private List<String> pendingAllies;

    public Guild(String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.invitedMembers = new ArrayList<>();
    }

    public Guild(String name, String prefix, String status, UUID master) {
        this.name = name;
        this.prefix = prefix;
        this.status = status;
        this.home = "";
        this.inventory = "";
        this.tier = 1;
        this.balance = 0.0;
        this.members = new ArrayList<>();
        this.members.add(new GuildMember(master, 0));
        this.allies = new ArrayList<>();
        this.invitedMembers = new ArrayList<>();
        this.pendingAllies = new ArrayList<>();
    }

    /**
     * Get a guild object
     * @param uuid
     * @return
     */
    public static Guild getGuild(UUID uuid) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(uuid))).findFirst().orElse(null);
    }

    public static Guild getGuild(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    /**
     * Check if guilds are allies
     * @param uuid1
     * @param uuid2
     * @return
     */
    public static boolean areAllies(UUID uuid1, UUID uuid2) {
        Guild guild1 = getGuild(uuid1);
        Guild guild2 = getGuild(uuid2);

        return !(guild1 == null || guild2 == null) && guild1.getAllies().contains(guild2.getName());
    }

    /**
     * Get the name of a guild
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of a guild
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Update the name of a guild
     * @param name
     */
    public void updateName(String name) {
        setName(name);
        updateGuild("", name, this.name);
    }

    /**
     * Get the prefix for a guild
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix of a guild
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Update a guild prefix
     * @param prefix
     */
    public void updatePrefix(String prefix) {
        setPrefix(prefix);
        updateGuild("Error occurred while update a guild prefix", prefix, this.name);
    }

    /**
     * Get the status of a guild
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status of a guild
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Update the status of a guild
     * @param status
     */
    public void updateStatus(String status) {
        setStatus(status);
        updateGuild("Error occured while updating a guild status", status, this.name);
    }

    /**
     * Get the home of a guild
     * @return
     */
    public String getHome() {
        return home;
    }

    /**
     * Set the home of a guild
     * @param home
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * Update the home of a guild
     * @param home
     */
    public void updateHome(String home) {
        setHome(home);
        updateGuild("Error occurred while updating a guild home", home, this.name);
    }

    /**
     * Get the inventory of a guild
     * @return
     */
    public String getInventory() {
        return inventory;
    }

    /**
     * Set the inventory of a guild
     * @param inventory
     */
    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    /**
     * Update the inventory of a guild
     * @param inventory
     */
    public void updateInventory(String inventory) {
        setInventory(inventory);
        updateGuild("", inventory, this.name);
    }

    /**
     * Get the tier of a guild
     * @return
     */
    public Integer getTier() {
        return tier;
    }

    /**
     * Set the tier of a guild
     * @param tier
     */
    public void setTier(Integer tier) {
        this.tier = tier;
    }

    /**
     * Update the tier of a guild
     * @param tier
     */
    public void updateTier(Integer tier) {
        setTier(tier);
        updateGuild("", String.valueOf(tier), this.name);
    }

    /**
     * Get the balance of a guild
     * @return
     */
    public Double getBalance() {
        return balance;
    }

    /**
     * Set the balance of a guild
     * @param balance
     */
    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * Update the balance of the guild
     * @param balance
     */
    public void updateBalance(Double balance) {
        setBalance(balance);
        updateGuild("Error updating guild balance", String.valueOf(balance), this.name);
    }

    /**
     * Get all the pending allies of a guild
     * @return
     */
    public List<String> getPendingAllies() {
        return pendingAllies;
    }

    /**
     * Get the allies of a guild
     * @return
     */
    public List<String> getAllies() {
        return allies;
    }

    /**
     * Get the members of a guild
     * @return
     */
    public List<GuildMember> getMembers() {
        return members;
    }

    /**
     * Get the invited members of a guild
     * @return
     */
    public List<UUID> getInvitedMembers() {
        return invitedMembers;
    }

    /**
     *  Get the guild master of a guild
     * @return
     */
    public GuildMember getGuildMaster() {
        return this.members.stream().filter(member -> member.getRole() == 0).findFirst().orElse(null);
    }

    /**
     * Get the tier name of a guild
     * @return
     */
    public String getTierName() {
        return guilds.getConfig().getString("tier" + getTier() + ".name");
    }

    /**
     * Get the tier cost of a guild
     * @return
     */
    public int getTierCost() {
        if (getTier() >= guilds.getConfig().getInt("max-number-of-tiers")) return 0;
        int newTier = getTier() + 1;
        return guilds.getConfig().getInt("tier" + newTier + ".cost");
    }

    /**
     * Get the max members of a guild
     * @return
     */
    public int getMaxMembers() {
        return guilds.getConfig().getInt("tier" + getTier() + ".max-members");
    }

    /**
     * Get the members required to rankup
     * @return
     */
    public int getMembersToRankup() {
        return guilds.getConfig().getInt("tier" + tier + ".members-to-rankup");
    }

    /**
     * Get exp multiplier of a guild
     * @return
     */
    public double getExpMultiplier() {
        return guilds.getConfig().getDouble("tier" + getTier() + ".mob-xp-multiplier");
    }

    /**
     * Get the perms of a guild
     * @return
     */
    public List<String> getGuildPerms() {
        return guilds.getConfig().getStringList("tier" + getTier() + ".permissions");
    }

    /**
     * Get the damage multiplier of a guild
     * @return
     */
    public double getDamageMultiplier() {
        return guilds.getConfig().getDouble("tier" + getTier() + ".damage-multiplier");
    }

    /**
     * Get the max bank balance of a guild
     * @return
     */
    public double getMaxBankBalance() {
        return guilds.getConfig().getDouble("tier" + getTier() + ".max-bank-balance");
    }

    /**
     * Get the max tier of a guild
     * @return
     */
    public int getMaxTier() {
        return guilds.getConfig().getInt("max-number-of-tiers");
    }

    /**
     * Add a member to the guild
     * @param uuid the uuid of the member
     * @param role the role to set them to
     */
    public void addMember(UUID uuid, GuildRole role) {
        this.members.add(new GuildMember(uuid, role.getLevel()));
        updateGuild("", uuid.toString(), this.name);
    }

    /**
     * Remove a member from a guild
     * @param uuid the uuid of the member
     */
    public void removeMember(UUID uuid) {
        GuildMember member = getMember(uuid);
        if (member == null) return;
        if (member == getGuildMaster()) {
            guilds.getDatabase().removeGuild(this);
            HashMap<String, Guild> guild = guilds.getGuildHandler().getGuilds();
            guild.remove(this.name);
            guilds.getGuildHandler().setGuilds(guild);
            return;
        }
        this.members.remove(member);
        updateGuild("", uuid.toString(), this.name);
    }

    /**
     * Invite a member to the guild
     * @param uuid the uuid of the member
     */
    public void inviteMember(UUID uuid) {
        invitedMembers.add(uuid);
        updateGuild("An error occurred while inviting a member with the UUID of '%s' to guild '%s'", uuid.toString(), this.name);
    }

    /**
     * Remove invited player
     * @param uuid the uuid of the player
     */
    public void removeInvitedPlayer(UUID uuid) {
        invitedMembers.remove(uuid);
        updateGuild("An error occurred while removing an invited member member with the UUID of '%s' to guild '%s'", uuid.toString(), this.name);
    }

    /**
     * Send a message to the guild
     * @param key the message key
     */
    public void sendMessage(Messages key, String... replacements) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> guilds.getManager().getCommandIssuer(p).sendInfo(key, replacements));
    }


    /**
     * Send a title to the guild
     * @param title the title message
     * @param subtitle the subtitle message
     * @param fadeIn seconds
     * @param stay seconds
     * @param fadeOut seconds
     */
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> p.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    /**
     * Send title support for 1.8
     * @param title
     * @param subtitle
     */
    public void sendTitleOld(String title, String subtitle) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> p.sendTitle(title, subtitle));
    }

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Add an ally to a guild
     * @param targetGuild
     */
    public void addAlly(Guild targetGuild) {
        allies.add(targetGuild.getName());
        addGuildAlly(targetGuild);
        updateGuild("", targetGuild.getName(), this.getName());
    }

    /**
     * Remove an ally from a guild
     * @param targetGuild
     */
    public void removeAlly(Guild targetGuild) {
        allies.remove(targetGuild.getName());
        removeGuildAlly(targetGuild);
        updateGuild("", targetGuild.getName(), this.getName());
    }

    /**
     * Add pending ally to guild
     * @param targetGuild
     */
    public void addPendingAlly(Guild targetGuild) {
        pendingAllies.add(targetGuild.getName());

        updateGuild("Something went wrong while adding pending ally %s from guild %s",
                targetGuild.getName(), this.getName());
    }

    /**
     * Remove pending ally from guild
     * @param targetGuild
     */
    public void removePendingAlly(Guild targetGuild) {
        pendingAllies.remove(targetGuild.getName());

        updateGuild("Something went wrong while removing pending ally %s from guild %s",
                targetGuild.getName(), this.getName());
    }

    /**
     * Uh idk why this is duplicated
     * @param targetGuild
     */
    public void addGuildAlly(Guild targetGuild) {
        guilds.getDatabase().addAlly(this, targetGuild);
    }

    /**
     * This might be duplicated
     * @param targetGuild
     */
    public void removeGuildAlly(Guild targetGuild) {
        guilds.getDatabase().removeAlly(this, targetGuild);
    }

    /**
     * Update guild data
     * @param error the error message
     * @param params the data to update
     */
    public void updateGuild(String error, String... params) {
        try {
            Guilds.getGuilds().getDatabase().updateGuild(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
