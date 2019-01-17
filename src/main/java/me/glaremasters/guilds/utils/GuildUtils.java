package me.glaremasters.guilds.utils;

import co.aikar.commands.ACFBukkitUtil;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Created by GlareMasters
 * Date: 1/16/2019
 * Time: 2:05 AM
 */

//todo move to guildhandler
//this is not a utiliy class, it has a  constructor and uses instance methods smh.
public class GuildUtils {

    private Guilds guilds;

    public GuildUtils(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Get a guild object
     * @param uuid
     * @return
     */
    public Guild getGuild(UUID uuid) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(uuid))).findFirst().orElse(null);
    }

    public Guild getGuild(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> guild.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Guild getGuild2(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> getNameColorless(guild).equalsIgnoreCase(guild.getName())).findAny().orElse(null);
    }

    /**
     * Check if guilds are allies
     * @param uuid1
     * @param uuid2
     * @return
     */
    public boolean areAllies(UUID uuid1, UUID uuid2) {
        Guild guild1 = getGuild(uuid1);
        Guild guild2 = getGuild(uuid2);

        return !(guild1 == null || guild2 == null) && guild1.getAllies().contains(guild2.getName());
    }

    public String getNameColorless(Guild guild) {
        return ACFBukkitUtil.removeColors(guild.getName());
    }


    /**
     * Get the tier name of a guild
     * @return
     */
    public String getTierName(Guild guild) {
        return guilds.getConfig().getString("tier" + guild.getTier() + ".name");
    }

    /**
     * Get the tier cost of a guild
     * @return
     */
    public int getTierCost(Guild guild) {
        if (guild.getTier() >= guilds.getConfig().getInt("max-number-of-tiers")) return 0;
        int newTier = guild.getTier() + 1;
        return guilds.getConfig().getInt("tier" + newTier + ".cost");
    }

    /**
     * Get the max members of a guild
     * @return
     */
    public int getMaxMembers(Guild guild) {
        return guilds.getConfig().getInt("tier" + guild.getTier() + ".max-members");
    }

    /**
     * Get the members required to rankup
     * @return
     */
    public int getMembersToRankup(Guild guild) {
        return guilds.getConfig().getInt("tier" + guild.getTier() + ".members-to-rankup");
    }

    /**
     * Get exp multiplier of a guild
     * @return
     */
    public double getExpMultiplier(Guild guild) {
        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".mob-xp-multiplier");
    }

    /**
     * Get the perms of a guild
     * @return
     */
    public List<String> getGuildPerms(Guild guild) {
        return guilds.getConfig().getStringList("tier" + guild.getTier() + ".permissions");
    }

    /**
     * Get the damage multiplier of a guild
     * @return
     */
    public double getDamageMultiplier(Guild guild) {
        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".damage-multiplier");
    }

    /**
     * Get the max bank balance of a guild
     * @return
     */
    public double getMaxBankBalance(Guild guild) {
        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".max-bank-balance");
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
    public void addMember(Guild guild, UUID uuid, GuildRole role) {
        guild.getMembers().add(new GuildMember(uuid, role.getLevel()));
        updateGuilds();
    }

    /**
     * Remove a member from a guild
     * @param uuid the uuid of the member
     */
    public void removeMember(Guild guild, UUID uuid) {
        GuildMember member = guild.getMember(uuid);
        if (member == null) return;
        if (member == guild.getGuildMaster()) {
            guilds.getDatabase().removeGuild(guild);
            Map<String, Guild> guildList = guilds.getGuildHandler().getGuilds();
            guildList.remove(guild.getName());
            guilds.getGuildHandler().setGuilds(guildList);
            return;
        }
        guild.getMembers().remove(member);
        updateGuilds();
    }

    /**
     * Removes all tier perms for all users in a guild
     * @param guild
     */
    public void removeGuildPerms(Guild guild) {
        guild.getMembers().forEach(member ->  {
            OfflinePlayer op = Bukkit.getOfflinePlayer(member.getUniqueId());
            getGuildPerms(guild).forEach(perm -> guilds.getPermissions().playerRemove(null, op, perm));
        });
    }

    /**
     * Removes all tier perms from a specific user
     * @param guild
     * @param player
     */
    public void removeGuildPerms(Guild guild, OfflinePlayer player) {
        getGuildPerms(guild).forEach(perm -> guilds.getPermissions().playerRemove(null, player, perm));
    }

    /**
     * Adds all tier perms for all users in a guild
     * @param guild
     */
    public void addGuildPerms(Guild guild) {
        guild.getMembers().forEach(member ->  {
            OfflinePlayer op = Bukkit.getOfflinePlayer(member.getUniqueId());
            getGuildPerms(guild).forEach(perm -> guilds.getPermissions().playerAdd(null, op, perm));
        });
    }

    /**
     * Adds all tier perms to a specific user
     * @param guild
     * @param player
     */
    public void addGuildPerms(Guild guild, OfflinePlayer player) {
        getGuildPerms(guild).forEach(perm -> guilds.getPermissions().playerAdd(null, player, perm));
    }

    /**
     * Invite a member to the guild
     * @param uuid the uuid of the member
     */
    public void inviteMember(Guild guild, UUID uuid) {
        guild.getInvitedMembers().add(uuid);
        updateGuilds();
    }

    /**
     * Remove invited player
     * @param uuid the uuid of the player
     */
    public void removeInvitedPlayer(Guild guild, UUID uuid) {
        guild.getInvitedMembers().remove(uuid);
        updateGuilds();
    }

    /**
     * Send a message to the guild
     * @param key the message key
     */
    public void sendMessage(Guild guild, Messages key, String... replacements) {
        guild.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUniqueId())).filter(Objects::nonNull).forEach(p -> guilds.getManager().getCommandIssuer(p).sendInfo(key, replacements));
    }

    /**
     * Add an ally to a guild
     * @param targetGuild
     */
    public void addAlly(Guild guild, Guild targetGuild) {
        guild.getAllies().add(targetGuild.getName());
        addGuildAlly(guild, targetGuild);
        updateGuilds();
    }

    /**
     * Remove an ally from a guild
     * @param targetGuild
     */
    public void removeAlly(Guild guild, Guild targetGuild) {
        guild.getAllies().remove(targetGuild.getName());
        removeGuildAlly(targetGuild, guild);
        updateGuilds();
    }

    /**
     * Add pending ally to guild
     * @param targetGuild
     */
    public void addPendingAlly(Guild guild, Guild targetGuild) {
        guild.getPendingAllies().add(targetGuild.getName());

        updateGuilds();
    }

    /**
     * Remove pending ally from guild
     * @param targetGuild
     */
    public void removePendingAlly(Guild guild, Guild targetGuild) {
        guild.getPendingAllies().remove(targetGuild.getName());

        updateGuilds();
    }

    /**
     * Uh idk why this is duplicated
     * @param targetGuild
     */
    public void addGuildAlly(Guild guild, Guild targetGuild) {
        guilds.getDatabase().addAlly(guild, targetGuild);
    }

    /**
     * This might be duplicated
     * @param targetGuild
     */
    public void removeGuildAlly(Guild guild, Guild targetGuild) {
        guilds.getDatabase().removeAlly(guild, targetGuild);
    }

    public void updateGuild(Guild guild) {
        guilds.getDatabase().updateGuild(guild);
    }

    public void updateGuilds() {
        guilds.getGuildHandler().getGuilds().values().forEach(this::updateGuild);
    }

}
