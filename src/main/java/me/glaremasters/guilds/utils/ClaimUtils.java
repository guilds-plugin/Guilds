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

package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedDomain;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 9:44 PM
 */
public class ClaimUtils {

    /**
     * Check if worldguard claims are enabled or not
     * @return valid or not
     */
    public static boolean isEnable(SettingsManager settingsManager) {
        return settingsManager.getProperty(HooksSettings.WORLDGUARD);
    }

    /**
     * Get the radius of a claim
     * @return the radius of a claim
     */
    public static int getRadius(SettingsManager settingsManager) {
        return settingsManager.getProperty(ClaimSettings.RADIUS);
    }

    /**
     * Get the smaller side of the guild claim
     * @param player the player running the command
     * @return smaller location
     */
    public static Location claimPointOne(Player player, SettingsManager settingsManager) {
        return player.getLocation().subtract(getRadius(settingsManager), player.getLocation().getY(), getRadius(settingsManager));
    }

    /**
     * Get the bigger side of the guild claim
     * @param player the player running the command
     * @return bigger location
     */
    public static Location claimPointTwo(Player player, SettingsManager settingsManager) {
        return player.getLocation().add(getRadius(settingsManager), (player.getWorld().getMaxHeight() - player.getLocation().getY()), getRadius(settingsManager));
    }

    /**
     * Get the probable name for the claim
     * @param guild the guild to get the id of
     * @return guild id
     */
    public static String getClaimName(Guild guild) {
        return guild.getId().toString();
    }

    /**
     * Check if a guild claim already exists
     * @param guild the guild of the player
     * @return if a claim already exists
     */
    public static boolean checkAlreadyExist(WorldGuardWrapper wrapper, Guild guild) {
        for (World world : Bukkit.getWorlds()) {
            Optional<IWrappedRegion> tempRegion = wrapper.getRegion(world, getClaimName(guild));
            try {
                tempRegion.get().getId();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if a worldguard region already exists
     * @param wrapper the wrapper
     * @param player the player to get the world of
     * @param name the name of the region
     * @return exist or not
     */
    public static boolean checkAlreadyExist(WorldGuardWrapper wrapper, Player player, String name) {
        return wrapper.getRegion(player.getWorld(), name).isPresent();
    }

    /**
     * Get a list of regions around a player
     * @param player the player being checked
     * @return the list of surrounding regions
     */
    public static Set<IWrappedRegion> regions(WorldGuardWrapper wrapper, Player player, SettingsManager settingsManager) {
        return wrapper.getRegions(claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager));
    }

    /**
     * Check if there is any overlap with a claim
     * @param player the player checking
     * @return if there is an overlap
     */
    public static boolean checkOverlap(WorldGuardWrapper wrapper, Player player, SettingsManager settingsManager) {
        return regions(wrapper, player, settingsManager).size() > 0;
    }

    /**
     * Check if a world can be used for claiming
     * @param player the player to check
     * @param settingsManager list of disabled worlds
     * @return is in disabled worlds or not
     */
    public static boolean isInDisabledWorld(Player player, SettingsManager settingsManager) {
        return settingsManager.getProperty(ClaimSettings.DISABLED_WORLDS).contains(player.getWorld().getName());
    }

    /**
     * Create a new guild claim
     * @param guild the guild making the claim
     * @param player the player running the command
     */
    public static void createClaim(WorldGuardWrapper wrapper, Guild guild, Player player, SettingsManager settingsManager) {
        wrapper.addCuboidRegion(getClaimName(guild), claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager));
    }

    /**
     * Create a new claim from an existing one
     * @param wrapper the wrapper
     * @param guild the guild the claim is going to
     * @param selection the selection of the old claim.
     */
    public static void createClaim(WorldGuardWrapper wrapper, Guild guild, ICuboidSelection selection) {
        wrapper.addCuboidRegion(getClaimName(guild), selection.getMinimumPoint(), selection.getMaximumPoint());
    }

    /**
     * Remove a guild claim
     * @param wrapper worldguard wrapper
     * @param guild the guild of the player
     */
    public static void removeClaim(WorldGuardWrapper wrapper, Guild guild) {
        for (World world: Bukkit.getWorlds()) {
            wrapper.removeRegion(world, getClaimName(guild));
        }
    }

    /**
     * Get a guild claim
     * @param player the player running the command
     * @param guild get the guild the player is in
     * @return the guild claim
     */
    public static Optional<IWrappedRegion> getGuildClaim(WorldGuardWrapper wrapper, Player player, Guild guild) {
        return wrapper.getRegion(player.getWorld(), getClaimName(guild));
    }

    /**
     * Get a claim object
     * @param wrapper the wrapper
     * @param player the player to check world
     * @param name the name of the claim
     * @return claim
     */
    public static Optional<IWrappedRegion> getClaim(WorldGuardWrapper wrapper, Player player, String name) {
        return wrapper.getRegion(player.getWorld(), name);
    }

    /**
     * Get the selection of a claim
     * @param wrapper the wrapper
     * @param player the player to get the world of
     * @param name name of claim
     * @return selection
     */
    public static ICuboidSelection getSelection(WorldGuardWrapper wrapper, Player player, String name) {
        return (ICuboidSelection) wrapper.getRegion(player.getWorld(), name).get().getSelection();
    }

    /**
     * Add the guild master to the region as an owner
     * @param claim the guild claim
     * @param guild the guild the player is in
     */
    public static void addOwner(IWrappedRegion claim, Guild guild) {
        claim.getOwners().addPlayer(guild.getGuildMaster().getUuid());
    }

    /**
     * Get the members of a guild claim
     * @param claim the claim being checked
     * @return the domain of members
     */
    public static IWrappedDomain getMembers(IWrappedRegion claim) {
        return claim.getMembers();
    }

    /**
     * Add all the guild members to a guild claim
     * @param claim the claim they are being added to
     * @param guild the guild they are in
     */
    public static void addMembers(IWrappedRegion claim, Guild guild) {
        guild.getMembers().forEach(m -> getMembers(claim).addPlayer(m.getUuid()));
    }

    /**
     * Add a member to a claim
     * @param claim the claim to add
     * @param player the player to add
     */
    public static void addMember(IWrappedRegion claim, Player player) {
        getMembers(claim).addPlayer(player.getUniqueId());
    }

    /**
     * Remove a member from a claim
     * @param claim the claim to check
     * @param player the player to remove
     */
    public static void removeMember(IWrappedRegion claim, OfflinePlayer player) {
        getMembers(claim).removePlayer(player.getUniqueId());
    }

    /**
     * Kick a member from the guild region by force
     * @param playerKicked player being kicked
     * @param playerExecuting player executing
     * @param guild guild they are in
     * @param settingsManager settings manager
     */
    public static void kickMember(OfflinePlayer playerKicked, Player playerExecuting, Guild guild, SettingsManager settingsManager) {
        if (isEnable(settingsManager)) {
            WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
            getGuildClaim(wrapper, playerExecuting, guild).ifPresent(r -> removeMember(r, playerKicked));
        }
    }

    /**
     * Set the enter message for a guild claim
     * @param wrapper the worldguard wrapper
     * @param claim the claim to adjust
     * @param settingsManager the settings manager
     * @param guild the guild to set it for
     */
    public static void setEnterMessage(WorldGuardWrapper wrapper, IWrappedRegion claim, SettingsManager settingsManager, Guild guild) {
        claim.setFlag(wrapper.getFlag("greeting", String.class).orElse(null), StringUtils.color(settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())));
    }

    /**
     * Set the exit message to a claim
     * @param wrapper the worldguard wrapper
     * @param claim the claim to adjust
     * @param settingsManager the settings manager
     * @param guild the guild to set it for
     */
    public static void setExitMessage(WorldGuardWrapper wrapper, IWrappedRegion claim, SettingsManager settingsManager, Guild guild) {
        claim.setFlag(wrapper.getFlag("farewell", String.class).orElse(null), StringUtils.color(settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())));
    }

    /**
     * Check if PVP is enabled or not in a specific region
     * @param player the player to check
     * @return if pvp is disabled or not
     */
    public static boolean checkPvpDisabled(Player player) {
        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
        Optional<IWrappedFlag<WrappedState>> flag = wrapper.getFlag("pvp", WrappedState.class);
        if (!flag.isPresent()) {
            return false;
        }
        WrappedState state = flag.map(f -> wrapper.queryFlag(player, player.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
        return state == WrappedState.DENY;
    }

    /**
     * Easy method to delete a claim when a guild deletes
     * @param guild the guild they are in
     * @param settingsManager settings manager
     */
    public static void deleteWithGuild(Guild guild, SettingsManager settingsManager) {
        if (isEnable(settingsManager)) {
            WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
            if (checkAlreadyExist(wrapper, guild)) {
                removeClaim(wrapper, guild);
            }
        }
    }

}
