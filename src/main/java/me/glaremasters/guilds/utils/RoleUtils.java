/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
package me.glaremasters.guilds.utils;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 11:35 PM
 */
public class RoleUtils {

    /**
     * Simple method to check if a user is in the same guild
     * @param guild the guild being checked
     * @param player the player being checked
     * @return if in guild or not
     */
    public static boolean inGuild(Guild guild, OfflinePlayer player) {
        return guild.getMember(player.getUniqueId()) != null;
    }

    /**
     * Compare two players by identity rather than by name.
     *
     * <p>{@link OfflinePlayer#getName()} is nullable and follows whatever name a player currently
     * has, so a name comparison silently fails to recognise a player who renamed themselves. The
     * UUID is the stable identity and is what the rest of the plugin keys on.
     *
     * @param one   the first player, may be null
     * @param other the second player, may be null
     * @return true when both are non-null and refer to the same player
     */
    public static boolean isSamePlayer(@Nullable OfflinePlayer one, @Nullable OfflinePlayer other) {
        return one != null && other != null && one.getUniqueId().equals(other.getUniqueId());
    }

    /**
     * Simple method to check if the player being promote can be promoted
     * @param guild the guild of the player
     * @param player the player being checked
     * @return can be promoted or not
     */
    public static boolean canPromote(Guild guild, OfflinePlayer player) {
        final GuildMember member = guild.getMember(player.getUniqueId());
        return member != null && member.getRole().getLevel() >= 1;
    }

    /**
     * Check if player is officer or not
     * @param guild the guild the check
     * @param player the player being checked
     * @return if officer or not
     */
    public static boolean isOfficer(Guild guild, OfflinePlayer player) {
        final GuildMember member = guild.getMember(player.getUniqueId());
        return member != null && member.getRole().getLevel() == 1;
    }

    /**
     * Check if you a user can promote another user
     * @param guild the guild they are in
     * @param target the target to check
     * @param player the player to check
     * @return if they can promote or not
     */
    public static boolean checkPromote(Guild guild, OfflinePlayer target, OfflinePlayer player) {
        final GuildMember targetMember = guild.getMember(target.getUniqueId());
        final GuildMember playerMember = guild.getMember(player.getUniqueId());

        if (targetMember == null || playerMember == null) {
            return false;
        }

        return (targetMember.getRole().getLevel() - 1) == playerMember.getRole().getLevel();
    }

    /**
     * Check if two users have the same role
     * @param guild the guild they are in
     * @param player the player to check
     * @param target the target to check
     * @return same role or not
     */
    public static boolean sameRole(Guild guild, OfflinePlayer player, OfflinePlayer target) {
        final GuildMember playerMember = guild.getMember(player.getUniqueId());
        final GuildMember targetMember = guild.getMember(target.getUniqueId());

        if (playerMember == null || targetMember == null) {
            return false;
        }

        return playerMember.getRole().getLevel() == targetMember.getRole().getLevel();
    }

    /**
     * Resolve the role a member would move to if they were promoted one step.
     *
     * <p>Returns null at the top of the hierarchy. That is exactly the case a caller has to refuse:
     * promoting there resolves to a role that does not exist, and writing that null into the
     * member's role field makes every later permission check for that member fail.
     *
     * @param guildHandler guild handler
     * @param member the member being promoted
     * @return the next higher role, or null when the member is already at the top
     */
    @Nullable public static GuildRole getNextHigherRole(GuildHandler guildHandler, @Nullable GuildMember member) {
        if (member == null) {
            return null;
        }

        return guildHandler.getGuildRole(member.getRole().getLevel() - 1);
    }

    /**
     * Resolve the role a member would move to if they were demoted one step.
     *
     * @param guildHandler guild handler
     * @param member the member being demoted
     * @return the next lower role, or null when none is configured
     */
    @Nullable public static GuildRole getNextLowerRole(GuildHandler guildHandler, @Nullable GuildMember member) {
        if (member == null) {
            return null;
        }

        return guildHandler.getGuildRole(member.getRole().getLevel() + 1);
    }

    /**
     * Simple method to promote a user
     * @param guildHandler the guild handler
     * @param guild the guild of the player
     * @param player the player being promoted
     * @return true when the role was changed, false when there is no higher role to move to
     */
    public static boolean promote(final GuildHandler guildHandler, final Guild guild, final OfflinePlayer player) {
        final GuildMember member = guild.getMember(player.getUniqueId());
        final GuildRole nextRole = getNextHigherRole(guildHandler, member);

        if (member == null || nextRole == null) {
            return false;
        }

        final Permission permission = guildHandler.getGuildsPlugin().getPermissions();
        guildHandler.removeRolePerm(permission, player);
        member.setRole(nextRole);
        guildHandler.addRolePerm(permission, player);
        return true;
    }

    /**
     * Demote a player
     * @param guildHandler guild handler
     * @param guild the guild they are in
     * @param player the player being demoted
     * @return true when the role was changed, false when there is no lower role to move to
     */
    public static boolean demote(final GuildHandler guildHandler, final Guild guild, final OfflinePlayer player) {
        final GuildMember member = guild.getMember(player.getUniqueId());
        final GuildRole nextRole = getNextLowerRole(guildHandler, member);

        if (member == null || nextRole == null) {
            return false;
        }

        final Permission permission = guildHandler.getGuildsPlugin().getPermissions();
        guildHandler.removeRolePerm(permission, player);
        member.setRole(nextRole);
        guildHandler.addRolePerm(permission, player);
        return true;
    }

    /**
     * Get the name of the role
     * @param member the member being checked
     * @return the name of role
     */
    public static String getCurrentRoleName(GuildMember member) {
        return member.getRole().getName();
    }

    /**
     * Get the role of a user before they were promoted
     * @param guildHandler guild handler
     * @param member the member being looked at, which has already been promoted
     * @return the name of the pre promoted role name
     */
    @Nullable public static String getPrePromotedRoleName(GuildHandler guildHandler, @Nullable GuildMember member) {
        if (member == null) {
            return null;
        }

        final GuildRole previous = guildHandler.getGuildRole(member.getRole().getLevel() + 1);
        return previous == null ? null : previous.getName();
    }

    /**
     * Get the role of a user before they were demoted
     * @param guildHandler guild handler
     * @param member the member being looked at
     * @return name of pre demoted role
     */
    @Nullable public static String getPreDemotedRoleName(GuildHandler guildHandler, @Nullable GuildMember member) {
        if (member == null) {
            return null;
        }

        final GuildRole previous = guildHandler.getGuildRole(member.getRole().getLevel() - 1);
        return previous == null ? null : previous.getName();
    }

    /**
     * Check if role is lowest role
     * @param guildHandler guild handler
     * @param member member being checked
     * @return if user is lowest role
     */
    public static boolean isLowest(GuildHandler guildHandler, GuildMember member) {
        return guildHandler.getLowestGuildRole().getLevel() == member.getRole().getLevel();
    }

    /**
     * Check if a players role is higher than another
     * @param target the player checking against
     * @param member player being checked
     * @return if their role is lower
     */
    public static boolean isLower(@Nullable GuildMember target, @Nullable GuildMember member) {
        if (target == null || member == null) {
            return false;
        }

        return target.getRole().getLevel() < member.getRole().getLevel();
    }

}
