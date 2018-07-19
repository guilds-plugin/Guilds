package me.glaremasters.guilds.placeholders;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 2:15 PM
 */
public class Placeholders {

    /**
     * Get the guild of a player
     * @param player the players you're getting the guild of
     * @return the guild that the player is in
     */
    public static String getGuild(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : guild.getName();
    }

    /**
     * Get the guild master of a guild
     * @param player the player you're getting the guild of
     * @return name of the guild master
     */
    public static String getGuildMaster(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
    }

    /**
     * Get the amount of members in a guild
     * @param player the player you're getting the guild of
     * @return amount of members in the guild
     */
    public static String getGuildMemberCount(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : String.valueOf(guild.getMembers().size());
    }

    /**
     * Get amount of members online
     * @param player member
     * @return amount of members online
     */
    public static String getGuildMembersOnline(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : String.valueOf(guild.getMembers().stream().map(m -> Bukkit.getOfflinePlayer(m.getUniqueId())).filter(OfflinePlayer::isOnline).count());
    }

    /**
     * Status of a guild
     * @param player status
     * @return the status of a guild
     */
    public static String getGuildStatus(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : guild.getStatus();
    }

    /**
     * Prefix of a guild
     * @param player prefix
     * @return the prefix of a guild
     */
    public static String getGuildPrefix(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : guild.getPrefix();
    }

    /**
     * Get the role of a player
     * @param player role
     * @return the role of a player
     */
    public static String getGuildRole(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName();
    }

    /**
     * Get the name of a tier
     * @param player tier
     * @return name of tier
     */
    public static String getTierName(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? "" : guild.getTierName();
    }

    /**
     * Get the tier of a guild
     * @param player tier
     * @return the tier of a guild
     */
    public static int getGuildTier(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? 0 : guild.getTier();
    }

    /**
     * Get the bank balance of a guild
     * @param player balance
     * @return the balance of the guild
     */
    public static double getBankBalance(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        return (guild == null) ? 0 : guild.getBalance();
    }

}
