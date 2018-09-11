package me.glaremasters.guilds.guild;

import me.glaremasters.guilds.Guilds;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildRole {

    private String name;
    private String node;

    private int level;

    private boolean chat, invite, kick, promote, demote, changePrefix, changeHome, removeGuild, addAlly,
            removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
            withdrawMoney, claimLand, unclaimLand, destroy, place, interact;

    public GuildRole(String name, String node, int level, boolean chat, boolean invite, boolean kick,
            boolean promote, boolean demote, boolean changePrefix, boolean changeHome,
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

    public String getName() {
        return name;
    }

    public String getNode() {
        return node;
    }

    public int getLevel() {
        return level;
    }

    public boolean canChat() {
        return chat;
    }

    public boolean canInvite() {
        return invite;
    }

    public boolean canKick() {
        return kick;
    }

    public boolean canPromote() {
        return promote;
    }

    public boolean canDemote() {
        return demote;
    }

    public boolean canChangePrefix() {
        return changePrefix;
    }

    public boolean canChangeHome() {
        return changeHome;
    }

    public boolean canRemoveGuild() {
        return removeGuild;
    }

    public boolean canAddAlly() {
        return addAlly;
    }

    public boolean canRemoveAlly() {
        return removeAlly;
    }

    public boolean useAllyChat() {
        return allyChat;
    }

    public boolean canOpenVault() {
        return openVault;
    }

    public boolean canTransfer() {
        return transferGuild;
    }

    public boolean canChangeStatus() { return changeStatus; }

    public boolean canActivateBuff() {
        return activateBuff;
    }

    public boolean canUpgradeGuild() {
        return upgradeGuild;
    }

    public boolean canDepositMoney() {
        return depositMoney;
    }

    public boolean canWithdrawMoney() {
        return withdrawMoney;
    }

    public boolean canClaimLand() { return claimLand; }

    public boolean canUnclaimLand() { return unclaimLand; }

    public boolean canDestroy() { return destroy; }

    public boolean canPlace() { return place; }

    public boolean canInteract() { return interact; }
}
