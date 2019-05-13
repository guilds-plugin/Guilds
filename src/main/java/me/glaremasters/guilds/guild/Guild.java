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

package me.glaremasters.guilds.guild;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.CommandManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Guild {

    public enum Status {
        Public("Public"),
        Private("Private");

        Status(String s) {
        }
    }

    private final UUID id;
    private String name, prefix;
    private GuildMember guildMaster;

    private GuildHome home;
    private GuildSkull guildSkull;
    private Status status;
    private GuildTier tier;
    private double balance;

    private List<GuildMember> members;

    private List<UUID> invitedMembers;
    private List<UUID> allies;
    private List<UUID> pendingAllies;

    private List<GuildCode> codes;

    private List<String> vaults;

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return getMembers().stream().filter(m -> m.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Get the itemstack of the guild skull
     * @return itemstack of skull
     */
    public ItemStack getSkull() {
        return SkullUtils.getSkull(guildSkull.getSerialized());
    }


    /**
     * Add player to a guild
     * @param player the player being added
     * @param guildHandler guild handler
     */
    public void addMember(OfflinePlayer player, GuildHandler guildHandler) {
        GuildMember member = new GuildMember(player.getUniqueId(), guildHandler.getLowestGuildRole());
        if (getMembers().contains(member)) return;
        removeInvitedMember(member.getUuid());
        getMembers().add(member);
    }

    /**
     * Invite a member by guild code
     * @param guildMember
     */
    public void addMemberByCode(GuildMember guildMember) {
        getMembers().add(guildMember);
    }

    /**
     * Remove a member by their GuildMember object
     * @param guildMember the guildmember to remove
     */
    public void removeMember(GuildMember guildMember){
        getMembers().remove(guildMember);
    }

    /**
     * Remove a member using it's OfflinePlayer object
     * @param player the OfflinePlayer to remove
     */
    public void removeMember(OfflinePlayer player){
        removeMember(getMember(player.getUniqueId()));
    }

    /**
     * Removes an ally's id from the list
     * @param guild the guild to remove
     */
    public void removeAlly(Guild guild) {
        getAllies().remove(guild.getId());
    }

    /**
     * Add an ally's id to the list
     * @param guild the guild to add
     */
    public void addAlly(Guild guild) {
        getAllies().add(guild.getId());
    }

    /**
     * Simple method to check if a guild has allies
     * @return if they have allies or not
     */
    public boolean hasAllies() {
        return getAllies().size() > 0;
    }

    /**
     * Check if a guild has pending allies
     * @return if a guild has pending allies or not
     */
    public boolean hasPendingAllies() {
        return getPendingAllies().size() > 0;
    }

    /**
     * Adds a pending ally's id to the list
     * @param guild the guild to add
     */
    public void addPendingAlly(Guild guild) {
        getPendingAllies().add(guild.getId());
    }

    /**
     * Check if an ally request is pending from another guild
     * @param guild the guild to check
     * @return if they have a pending invite
     */
    public boolean isAllyPending(Guild guild) {
        return getPendingAllies().contains(guild.getId());
    }

    /**
     * Removes a pending ally's id from the list
     * @param guild the guild to remove
     */
    public void removePendingAlly(Guild guild) {
        getPendingAllies().remove(guild.getId());
    }

    /**
     * Invites a member to this guild.
     *
     * @param uuid the UUID of the player.
     */
    public void inviteMember(UUID uuid) {
        if (getInvitedMembers().contains(uuid)) return;
        getInvitedMembers().add(uuid);
    }

    /**
     * Create a simple method to check if invited
     * @param player the player being checked
     * @return invited or not
     */
    public boolean checkIfInvited(Player player) {
        return getInvitedMembers().contains(player.getUniqueId());
    }

    /**
     * Removes an invited member
     *
     * @param uuid the member to remove from the invites.
     */
    public void removeInvitedMember(UUID uuid) {
        getInvitedMembers().remove(uuid);
    }

    /**
     * Get the amount of members
     * @return size of members list.
     */
    public int getSize() {
        return getMembers().size();
    }

    /**
     * Returns amount of online members
     *
     * @return a List of online members
     */
    public List<GuildMember> getOnlineMembers() {
        return getMembers().stream().filter(GuildMember::isOnline).collect(Collectors.toList());
    }

    /**
     * Get all online members as players
     * @return list of players
     */
    public List<Player> getOnlineAsPlayers() {
        return getOnlineMembers().stream().map(m -> Bukkit.getPlayer(m.getUuid())).collect(Collectors.toList());
    }

    /**
     * Get all players as a list
     * @return list of players
     */
    public List<OfflinePlayer> getAllAsPlayers() {
        return getMembers().stream().map(m -> Bukkit.getOfflinePlayer(m.getUuid())).collect(Collectors.toList());
    }

    /**
     * Check if a guild has a code
     * @param code the code being checked
     * @return if the guild has it or not
     */
    public boolean hasInviteCode(String code) {
        return getCodes().stream().anyMatch(c -> c.getId().equals(code));
    }

    /**
     * Get a guild code object by the id
     * @param code the code looking for
     * @return the guild code object
     */
    public GuildCode getCode(String code) {
        return getCodes().stream().filter(c -> c.getId().equals(code)).findFirst().orElse(null);
    }

    /**
     * Get a list of active codes
     * @return list of active codes
     */
    public List<GuildCode> getActiveCodes() {
        return getCodes().stream().filter(c -> c.getUses() > 0).collect(Collectors.toList());
    }

    /**
     * Add a new code to a guild
     * @param code the code being added
     * @param uses the amount of uses it has
     * @param creator the creator of the code
     */
    public void addCode(String code, int uses, Player creator) {
        getCodes().add(new GuildCode(code, uses, creator.getUniqueId(), new ArrayList<>()));
    }

    /**
     * Remove a code from a Guild if it exists
     * @param code the code being checked
     */
    public void removeCode(String code) {
        getCodes().removeIf(s -> s.getId().equals(code));
    }

    /**
     * Checks if they can make another code
     * @param amount the max amount
     * @return if they can make it or not
     */
    public boolean getActiveCheck(int amount) {
        return (getActiveCodes().size() >= amount);
    }

    /**
     * Get all the redeemers of a code
     * @param code the code being checked
     * @return a list of all redeemers for a code
     */
    public String getRedeemers(String code) {
        GuildCode gc = getCode(code);
        StringBuilder builder = new StringBuilder();
        gc.getRedeemers().forEach(r -> builder.append(Bukkit.getOfflinePlayer(r).getName() + ", "));
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    /**
     * Send a message to the guild
     * @param manager get the manager to send custom messages
     * @param key the message to send
     * @param replacements any args we need to handle
     */
    public void sendMessage(CommandManager manager, Messages key, String... replacements) {
        getOnlineMembers().forEach(m -> manager.getCommandIssuer(Bukkit.getPlayer(m.getUuid())).sendInfo(key, replacements));
    }

    /**
     * Send a message to the guild
     * @param message the message to send
     */
    public void sendMessage(String message) {
        getOnlineAsPlayers().forEach(m -> m.sendMessage(message));
    }

    /**
     * Toggle the status of a guild
     */
    public void toggleStatus() {
        if (getStatus() == Status.Private) {
            setStatus(Status.Public);
        } else {
            setStatus(Status.Private);
        }
    }

    /**
     * Super simple method to set a home's location to null
     */
    public void delHome() {
        setHome(null);
    }

    /**
     * Simple method to create a new guild home
     * @param player the player running the command
     */
    public void setNewHome(Player player) {
        Location l = player.getLocation();
        setHome(new GuildHome(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
    }

    /**
     * Simple method to check if a guild is public or private
     * @return public or private
     */
    public boolean isPrivate() {
        return getStatus() == Status.Private;
    }

    /**
     * Simple method to check if the player in question is the guild master
     * @param player the player in question
     * @return master or not
     */
    public boolean isMaster(OfflinePlayer player) {
        return getGuildMaster().getUuid().equals(player.getUniqueId());
    }

    /**
     * Transfer a guild from one player to another
     * @param oldPlayer old player
     * @param newPlayer new player
     */
    public void transferGuild(Player oldPlayer, Player newPlayer) {

        GuildMember oldMaster = getMember(oldPlayer.getUniqueId());
        GuildMember newMaster = getMember(newPlayer.getUniqueId());

        if (newMaster.getRole().getLevel() != 1)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_OFFICER));

        oldMaster.setRole(newMaster.getRole());
        setGuildMaster(newMaster);
    }

    /**
     * Simple method to add a buff to all online members
     * @param type the potion type
     * @param length the length of the potion
     * @param amplifier the strength of the potion
     */
    public void addPotion(String type, int length, int amplifier) {
        getOnlineAsPlayers().forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(type), length, amplifier)));
    }
}

