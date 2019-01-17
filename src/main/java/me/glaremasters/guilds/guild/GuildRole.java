package me.glaremasters.guilds.guild;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter
public class GuildRole {

    private String name;
    private String node;

    private int level;

    private boolean chat, invite, kick, promote, demote, changePrefix, changeName, changeHome, removeGuild, addAlly,
            removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
            withdrawMoney, claimLand, unclaimLand, destroy, place, interact;


    //todo an actual correct javadoc
    @Builder
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
}
