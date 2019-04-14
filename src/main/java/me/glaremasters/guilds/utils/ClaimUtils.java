/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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
import co.aikar.commands.ACFBukkitUtil;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedDomain;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

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
    public static int getRaidus(SettingsManager settingsManager) {
        return settingsManager.getProperty(ClaimSettings.RADIUS);
    }

    /**
     * Get the smaller side of the guild claim
     * @param player the player running the command
     * @return smaller location
     */
    public static Location claimPointOne(Player player, SettingsManager settingsManager) {
        return player.getLocation().subtract(getRaidus(settingsManager), player.getLocation().getY(), getRaidus(settingsManager));
    }

    /**
     * Get the bigger side of the guild claim
     * @param player the player running the command
     * @return bigger location
     */
    public static Location claimPointTwo(Player player, SettingsManager settingsManager) {
        return player.getLocation().add(getRaidus(settingsManager), (player.getWorld().getMaxHeight() - player.getLocation().getY()), getRaidus(settingsManager));
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
     * @param player the player running the command
     * @param guild the guild of the player
     * @return if a claim already exists
     */
    public static boolean checkAlreadyExist(WorldGuardWrapper wrapper, Player player, Guild guild) {
        return wrapper.getRegion(player.getWorld(), getClaimName(guild)).isPresent();
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
     * Create a new guild claim
     * @param guild the guild making the claim
     * @param player the player running the command
     */
    public static void createClaim(WorldGuardWrapper wrapper, Guild guild, Player player, SettingsManager settingsManager) {
        wrapper.addCuboidRegion(getClaimName(guild), claimPointOne(player, settingsManager), claimPointTwo(player, settingsManager));
    }

    /**
     * Remove a guild claim
     * @param wrapper worldguard wrapper
     * @param guild the guild of the player
     * @param player the player running the command
     */
    public static void removeClaim(WorldGuardWrapper wrapper, Guild guild, Player player) {
        wrapper.removeRegion(player.getWorld(), getClaimName(guild));
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
     * Set the enter message for a guild claim
     * @param wrapper the worldguard wrapper
     * @param claim the claim to adjust
     * @param settingsManager the settings manager
     * @param guild the guild to set it for
     */
    public static void setEnterMessage(WorldGuardWrapper wrapper, IWrappedRegion claim, SettingsManager settingsManager, Guild guild) {
        claim.setFlag(wrapper.getFlag("greeting", String.class).orElse(null), ACFBukkitUtil.color(settingsManager.getProperty(ClaimSettings.ENTER_MESSAGE).replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())));
    }

    /**
     * Set the exit message to a claim
     * @param wrapper the worldguard wrapper
     * @param claim the claim to adjust
     * @param settingsManager the settings manager
     * @param guild the guild to set it for
     */
    public static void setExitMessage(WorldGuardWrapper wrapper, IWrappedRegion claim, SettingsManager settingsManager, Guild guild) {
        claim.setFlag(wrapper.getFlag("farewell", String.class).orElse(null), ACFBukkitUtil.color(settingsManager.getProperty(ClaimSettings.EXIT_MESSAGE).replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())));
    }

}
