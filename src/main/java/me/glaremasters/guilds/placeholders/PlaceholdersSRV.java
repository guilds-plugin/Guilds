package me.glaremasters.guilds.placeholders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlaceholdersSRV {


    public static String getGuild(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getName();
    }

    public static String getGuildMaster(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
    }

    public static String getGuildMemberCount(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return String.valueOf(guild.getMembers().size());
    }

    public static String getGuildMembersOnline(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return String.valueOf(
                guild.getMembers().stream()
                        .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                        .filter(OfflinePlayer::isOnline).count());
    }

    public static String getGuildMembers(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }
        List<String> lines = Arrays.asList(guild.getMembers().stream()
                .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName())
                .collect(Collectors.joining(", "))
                .replaceAll("(([a-zA-Z0-9_]+, ){3})", "$0\n")
                .split("\n"));
        return lines.get(0);
    }

    public static String getGuildStatus(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getStatus();
    }

    public static String getGuildPrefix(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getPrefix();
    }

    public static String getGuildRole(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return "";
        }
        GuildMember roleCheck = guild.getMember(player.getUniqueId());
        return GuildRole.getRole(roleCheck.getRole()).getName();
    }

    public static int getGuildTier(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getTier();
    }

    public static double getBankBalance(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getBankBalance();
    }

    public static double getUpgradeCost(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return 0;
        }

        return guild.getTierCost();
    }

    public static String getTierName(OfflinePlayer player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return "";
        }

        return guild.getTierName();
    }

}
