package me.glaremasters.guilds.placeholders;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 1/7/2018.
 */
public class Placeholders {

    /**
     * Get the guild of the player
     * @param player guild
     * @return name of guild
     */
    public static String getGuild(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getName();
    }

    /**
     * Get the guild master of a guild
     * @param player guild master
     * @return name of guild master
     */
    public static String getGuildMaster(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
    }

    /**
     * Get the amount of members in a guild
     * @param player member
     * @return amount of members in the guild
     */
    public static String getGuildMemberCount(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return String.valueOf(guild.getMembers().size());
    }

    /**
     * Get amount of members online
     * @param player member
     * @return amount of members online
     */
    public static String getGuildMembersOnline(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return String.valueOf(
                guild.getMembers().stream()
                        .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                        .filter(OfflinePlayer::isOnline).count());
    }

    /**
     * Status of a guild
     * @param player status
     * @return the status of a guild
     */
    public static String getGuildStatus(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getStatus();
    }

    /**
     * Prefix of a guild
     * @param player prefix
     * @return the prefix of a guild
     */
    public static String getGuildPrefix(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getPrefix();
    }

    /**
     * Get the role of a player
     * @param player role
     * @return the role of a player
     */
    public static String getGuildRole(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return "";
        }
        GuildMember roleCheck = guild.getMember(player.getUniqueId());
        return GuildRole.getRole(roleCheck.getRole()).getName();
    }

    /**
     * Get the tier of a guild
     * @param player tier
     * @return the tier of a guild
     */
    public static int getGuildTier(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getTier();
    }

    /**
     * Get the bank balance of a guild
     * @param player balance
     * @return the balance of the guild
     */
    public static double getBankBalance(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getBankBalance();
    }

    /**
     * Get the cost of an upgrade
     * @param player upgrade
     * @return the cost of an upgrade
     */
    public static double getUpgradeCost(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getTierCost();
    }

    /**
     * Get the name of a tier
     * @param player tier
     * @return name of tier
     */
    public static String getTierName(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getTierName();
    }

}
