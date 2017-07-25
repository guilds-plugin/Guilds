package me.glaremasters.guilds.placeholders;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Placeholders {


  public static String getGuild(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return guild.getName();
  }

  public static String getGuildMaster(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return Bukkit.getPlayer(guild.getGuildMaster().getUniqueId()).getName();
  }

  public static String getGuildMemberCount(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return String.valueOf(guild.getMembers().size());
  }

  public static String getGuildMembersOnline(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return String.valueOf(
        guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
            .filter(OfflinePlayer::isOnline).count());
  }

  public static String getGuildStatus(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return guild.getStatus();
  }

  public static String getGuildPrefix(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      return "";
    }

    return guild.getPrefix();
  }

  public static String getGuildRole(Player player) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    GuildMember roleCheck = guild.getMember(player.getUniqueId());
    if (guild == null) {
      return "";
    }
    return GuildRole.getRole(roleCheck.getRole()).getName();
  }
}
