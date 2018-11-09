package me.glaremasters.guilds.guild;

import me.glaremasters.guilds.Guilds;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildRole {

    private String name;
    private String node;

    private int level;

    private boolean chat, invite, kick, promote, demote, changePrefix, changeName, changeHome, removeGuild, addAlly,
            removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
            withdrawMoney, claimLand, unclaimLand, destroy, place, interact;

    /**
     * Dear god this is a big notation. Maybe this will make it look more fancy. I plan to clean this up.
     * @param name
     * @param node
     * @param level
     * @param chat
     * @param invite
     * @param kick
     * @param promote
     * @param demote
     * @param changePrefix
     * @param changeName
     * @param changeHome
     * @param removeGuild
     * @param addAlly
     * @param removeAlly
     * @param allyChat
     * @param openVault
     * @param transferGuild
     * @param changeStatus
     * @param activateBuff
     * @param upgradeGuild
     * @param depositMoney
     * @param withdrawMoney
     * @param claimLand
     * @param unclaimLand
     * @param destroy
     * @param place
     * @param interact
     */
    public GuildRole(String name, String node, int level, boolean chat, boolean invite, boolean kick,
            boolean promote, boolean demote, boolean changePrefix, boolean changeName, boolean changeHome,
            boolean removeGuild, boolean addAlly, boolean removeAlly, boolean allyChat,
            boolean openVault, boolean transferGuild, boolean changeStatus, boolean activateBuff,
            boolean upgradeGuild, boolean depositMoney, boolean withdrawMoney, boolean claimLand,
            boolean unclaimLand, boolean destroy, boolean place, boolean interact) {
        this.name = name;
        this.node = node;
        this.level = level;
        this.chat = chat;
        this.invite = invite;
        this.kick = kick;
        this.promote = promote;
        this.demote = demote;
        this.changePrefix = changePrefix;
        this.changeName = changeName;
        this.changeHome = changeHome;
        this.removeGuild = removeGuild;
        this.addAlly = addAlly;
        this.removeAlly = removeAlly;
        this.allyChat = allyChat;
        this.openVault = openVault;
        this.transferGuild = transferGuild;
        this.changeStatus = changeStatus;
        this.activateBuff = activateBuff;
        this.upgradeGuild = upgradeGuild;
        this.depositMoney = depositMoney;
        this.withdrawMoney = withdrawMoney;
        this.claimLand = claimLand;
        this.unclaimLand = unclaimLand;
        this.destroy = destroy;
        this.place = place;
        this.interact = interact;
    }

    /**
     * Get the role
     * @param level the level of the role
     * @return the level of the role
     */
    public static GuildRole getRole(int level) {
        return Guilds.getGuilds().getGuildHandler().getRoles().stream().filter(role -> role.getLevel() == level).findFirst().orElse(null);
    }

    /**
     * Check the lowest role possible
     * @return the lowest role possible
     */
    public static GuildRole getLowestRole() {
        GuildRole lowest = null;

        for (GuildRole role  : Guilds.getGuilds().getGuildHandler().getRoles()) {
            if (lowest == null || lowest.getLevel() < role.getLevel()) lowest = role;
        }
        return lowest;
    }

    /**
     * Get the name of the role
     * @return name of role
     */
    public String getName() {
        return name;
    }

    /**
     * I don't think I'm using this yet
     * @return
     */
    public String getNode() {
        return node;
    }

    /**
     * Get level of role
     * @return level of role
     */
    public int getLevel() {
        return level;
    }

    /**
     * Check if user can use guild chat
     * @return
     */
    public boolean canChat() {
        return chat;
    }

    /**
     * Check if user can invite other players
     * @return
     */
    public boolean canInvite() {
        return invite;
    }

    /**
     * Check if user can kick others from guild
     * @return
     */
    public boolean canKick() {
        return kick;
    }

    /**
     * Check if user can promote others in guild
     * @return
     */
    public boolean canPromote() {
        return promote;
    }

    /**
     * Check if user can demote others in guild
     * @return
     */
    public boolean canDemote() {
        return demote;
    }

    /**
     * Check if user can change guild prefix
     * @return
     */
    public boolean canChangePrefix() {
        return changePrefix;
    }

    /**
     * Check if user can change name of guild
     * @return
     */
    public boolean canChangeName() {
        return changeName;
    }

    /**
     * Check if user can change guild home
     * @return
     */
    public boolean canChangeHome() {
        return changeHome;
    }

    /**
     * Check is user can disband guild
     * @return
     */
    public boolean canRemoveGuild() {
        return removeGuild;
    }

    /**
     * Check if user can add allies
     * @return
     */
    public boolean canAddAlly() {
        return addAlly;
    }

    /**
     * Check if user can remove allies
     * @return
     */
    public boolean canRemoveAlly() {
        return removeAlly;
    }

    /**
     * Check is user can message allies
     * @return
     */
    public boolean useAllyChat() {
        return allyChat;
    }

    /**
     * Check if user can open guild vault
     * @return
     */
    public boolean canOpenVault() {
        return openVault;
    }

    /**
     * Check if user can transfer guild
     * @return
     */
    public boolean canTransfer() {
        return transferGuild;
    }

    /**
     * Check if user can change guild status
     * @return
     */
    public boolean canChangeStatus() {
        return changeStatus;
    }

    /**
     * Check if user can activate guild buffs
     * @return
     */
    public boolean canActivateBuff() {
        return activateBuff;
    }

    /**
     * Check if user can upgrade guild tier
     * @return
     */
    public boolean canUpgradeGuild() {
        return upgradeGuild;
    }

    /**
     * Check if user can deposit money
     * @return
     */
    public boolean canDepositMoney() {
        return depositMoney;
    }

    /**
     * Check if user can withdraw money
     * @return
     */
    public boolean canWithdrawMoney() {
        return withdrawMoney;
    }

    public boolean canClaimLand() { return claimLand; }

    public boolean canUnclaimLand() { return unclaimLand; }

    public boolean canDestroy() { return destroy; }

    public boolean canPlace() { return place; }

    public boolean canInteract() { return interact; }
}
