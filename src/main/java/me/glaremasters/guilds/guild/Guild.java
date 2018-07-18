package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;
import me.glaremasters.guilds.Guilds;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.glaremasters.guilds.utils.ConfigUtils.getInt;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class Guild {

    @Expose(serialize = false)
    private String name;

    @Expose
    private String prefix, home, status;

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

    private Inventory inventory;

    public Guild(String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.invitedMembers = new ArrayList<>();
    }

    public Guild(String name, UUID master) {
        this.name = name;
        this.prefix = name.substring(0, getInt("prefix.max-length") > name.length() ? name.length() : getInt("prefix.max-length"));
        this.status = "";
        this.home = "";
        this.tier = 1;
        this.balance = 0.0;
        this.members = new ArrayList<>();
        this.members.add(new GuildMember(master, 0));
        this.invitedMembers = new ArrayList<>();
        this.pendingAllies = new ArrayList<>();
    }

    public static Guild getGuild(UUID uuid) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(uuid))).findFirst().orElse(null);
    }

    public static Guild getGuild(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> ChatColor.stripColor(guild.getName()).equalsIgnoreCase(name)).findFirst().orElse(null);
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

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTierCost() {
        if (getTier() >= getInt("max-number-of-tiers")) return 0;
        int upgradeTier = getTier() + 1;
        return getInt("tier" + upgradeTier + ".cost");
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

    public void updateGuild(String error, String... params) {
        try {
            Guilds.getGuilds().getDatabase().updateGuild(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
