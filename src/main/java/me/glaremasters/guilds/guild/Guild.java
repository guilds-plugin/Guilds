package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;
import me.glaremasters.guilds.Guilds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

import static me.glaremasters.guilds.utils.ConfigUtils.*;

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

    public Guild(String name, UUID master) {
        this.name = name;
        this.prefix = "";
        this.status = "";
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

    public static Guild getGuild(UUID uuid) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(uuid))).findFirst().orElse(null);
    }

    public static Guild getGuild(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public static boolean areAllies(UUID uuid1, UUID uuid2) {
        Guild guild1 = getGuild(uuid1);
        Guild guild2 = getGuild(uuid2);

        return !(guild1 == null || guild2 == null) && guild1.getAllies().contains(guild2.getName());
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void updatePrefix(String prefix) {
        setPrefix(prefix);
        updateGuild("Error occurred while update a guild prefix", prefix, this.name);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void updateStatus(String status) {
        setStatus(status);
        updateGuild("Error occured while updating a guild status", status, this.name);
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void updateHome(String home) {
        setHome(home);
        updateGuild("Error occurred while updating a guild home", home, this.name);
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public void updateInventory(String inventory) {
        setInventory(inventory);
        updateGuild("", inventory, this.name);
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public void updateTier(Integer tier) {
        setTier(tier);
        updateGuild("", String.valueOf(tier), this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        setName(name);
        updateGuild("", name, this.name);
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void updateBalance(Double balance) {
        setBalance(balance);
        updateGuild("Error updating guild balance", String.valueOf(balance), this.name);
    }

    public List<String> getPendingAllies() {
        return pendingAllies;
    }

    public List<String> getAllies() {
        return allies;
    }

    public List<GuildMember> getMembers() {
        return members;
    }

    public List<UUID> getInvitedMembers() {
        return invitedMembers;
    }

    public GuildMember getGuildMaster() {
        return this.members.stream().filter(member -> member.getRole() == 0).findFirst().orElse(null);
    }

    public String getTierName() {
        return getString("tier" + getTier() + ".name");
    }

    public int getTierCost() {
        if (getTier() >= getInt("max-number-of-tiers")) return 0;
        int newTier = getTier() + 1;
        return getInt("tier" + newTier + ".cost");
    }

    public int getMaxMembers() {
        return getInt("tier" + getTier() + ".max-members");
    }

    public int getMembersToRankup() {
        return getInt("tier" + tier + ".members-to-rankup");
    }

    public double getExpMultiplier() {
        return getDouble("tier" + getTier() + ".mob-xp-multiplier");
    }

    public List<String> getGuildPerms() {
        return getStringList("tier" + getTier() + ".permissions");
    }

    public double getDamageMultiplier() {
        return getDouble("tier" + getTier() + ".damage-multiplier");
    }

    public double getMaxBankBalance() {
        return getDouble("tier" + getTier() + ".max-bank-balance");
    }

    public void addMember(UUID uuid, GuildRole role) {
        this.members.add(new GuildMember(uuid, role.getLevel()));
        updateGuild("", uuid.toString(), this.name);
    }

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

    public void inviteMember(UUID uuid) {
        invitedMembers.add(uuid);
        updateGuild("An error occurred while inviting a member with the UUID of '%s' to guild '%s'", uuid.toString(), this.name);
    }

    public void removeInvitedPlayer(UUID uuid) {
        invitedMembers.remove(uuid);
        updateGuild("An error occurred while removing an invited member member with the UUID of '%s' to guild '%s'", uuid.toString(), this.name);
    }

    public void sendMessage(String message) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> p.sendMessage(color(message)));
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> p.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    public void sendTitleOld(String title, String subtitle) {
        members.stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> p.sendTitle(title, subtitle));
    }

    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }


    public void updateGuild(String error, String... params) {
        try {
            Guilds.getGuilds().getDatabase().updateGuild(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
