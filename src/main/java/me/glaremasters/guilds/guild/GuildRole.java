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

package me.glaremasters.guilds.guild;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildRole {

    private transient String name;
    private transient String node;

    private int level;

    private transient boolean chat, invite, kick, promote, demote, changePrefix, changeName, changeHome, removeGuild, addAlly,
            removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
            withdrawMoney, claimLand, unclaimLand, destroy, place, interact, createCode, deleteCode, seeCodeRedeemers, modifyMotd, initiateWar;

    public GuildRole(String name, String node, int level, boolean chat, boolean invite, boolean kick, boolean promote, boolean demote, boolean changePrefix, boolean changeName, boolean changeHome, boolean removeGuild, boolean addAlly, boolean removeAlly, boolean allyChat, boolean openVault, boolean transferGuild, boolean changeStatus, boolean activateBuff, boolean upgradeGuild, boolean depositMoney, boolean withdrawMoney, boolean claimLand, boolean unclaimLand, boolean destroy, boolean place, boolean interact, boolean createCode, boolean deleteCode, boolean seeCodeRedeemers, boolean modifyMotd, boolean initiateWar) {
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
        this.createCode = createCode;
        this.deleteCode = deleteCode;
        this.seeCodeRedeemers = seeCodeRedeemers;
        this.modifyMotd = modifyMotd;
        this.initiateWar = initiateWar;
    }

    public static GuildRoleBuilder builder() {
        return new GuildRoleBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getNode() {
        return this.node;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isChat() {
        return this.chat;
    }

    public boolean isInvite() {
        return this.invite;
    }

    public boolean isKick() {
        return this.kick;
    }

    public boolean isPromote() {
        return this.promote;
    }

    public boolean isDemote() {
        return this.demote;
    }

    public boolean isChangePrefix() {
        return this.changePrefix;
    }

    public boolean isChangeName() {
        return this.changeName;
    }

    public boolean isChangeHome() {
        return this.changeHome;
    }

    public boolean isRemoveGuild() {
        return this.removeGuild;
    }

    public boolean isAddAlly() {
        return this.addAlly;
    }

    public boolean isRemoveAlly() {
        return this.removeAlly;
    }

    public boolean isAllyChat() {
        return this.allyChat;
    }

    public boolean isOpenVault() {
        return this.openVault;
    }

    public boolean isTransferGuild() {
        return this.transferGuild;
    }

    public boolean isChangeStatus() {
        return this.changeStatus;
    }

    public boolean isActivateBuff() {
        return this.activateBuff;
    }

    public boolean isUpgradeGuild() {
        return this.upgradeGuild;
    }

    public boolean isDepositMoney() {
        return this.depositMoney;
    }

    public boolean isWithdrawMoney() {
        return this.withdrawMoney;
    }

    public boolean isClaimLand() {
        return this.claimLand;
    }

    public boolean isUnclaimLand() {
        return this.unclaimLand;
    }

    public boolean isDestroy() {
        return this.destroy;
    }

    public boolean isPlace() {
        return this.place;
    }

    public boolean isInteract() {
        return this.interact;
    }

    public boolean isCreateCode() {
        return this.createCode;
    }

    public boolean isDeleteCode() {
        return this.deleteCode;
    }

    public boolean isSeeCodeRedeemers() {
        return this.seeCodeRedeemers;
    }

    public boolean isModifyMotd() {
        return this.modifyMotd;
    }

    public boolean isInitiateWar() {
        return this.initiateWar;
    }

    public static class GuildRoleBuilder {
        private String name;
        private String node;
        private int level;
        private boolean chat;
        private boolean invite;
        private boolean kick;
        private boolean promote;
        private boolean demote;
        private boolean changePrefix;
        private boolean changeName;
        private boolean changeHome;
        private boolean removeGuild;
        private boolean addAlly;
        private boolean removeAlly;
        private boolean allyChat;
        private boolean openVault;
        private boolean transferGuild;
        private boolean changeStatus;
        private boolean activateBuff;
        private boolean upgradeGuild;
        private boolean depositMoney;
        private boolean withdrawMoney;
        private boolean claimLand;
        private boolean unclaimLand;
        private boolean destroy;
        private boolean place;
        private boolean interact;
        private boolean createCode;
        private boolean deleteCode;
        private boolean seeCodeRedeemers;
        private boolean modifyMotd;
        private boolean initiateWar;

        GuildRoleBuilder() {
        }

        public GuildRole.GuildRoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GuildRole.GuildRoleBuilder node(String node) {
            this.node = node;
            return this;
        }

        public GuildRole.GuildRoleBuilder level(int level) {
            this.level = level;
            return this;
        }

        public GuildRole.GuildRoleBuilder chat(boolean chat) {
            this.chat = chat;
            return this;
        }

        public GuildRole.GuildRoleBuilder invite(boolean invite) {
            this.invite = invite;
            return this;
        }

        public GuildRole.GuildRoleBuilder kick(boolean kick) {
            this.kick = kick;
            return this;
        }

        public GuildRole.GuildRoleBuilder promote(boolean promote) {
            this.promote = promote;
            return this;
        }

        public GuildRole.GuildRoleBuilder demote(boolean demote) {
            this.demote = demote;
            return this;
        }

        public GuildRole.GuildRoleBuilder changePrefix(boolean changePrefix) {
            this.changePrefix = changePrefix;
            return this;
        }

        public GuildRole.GuildRoleBuilder changeName(boolean changeName) {
            this.changeName = changeName;
            return this;
        }

        public GuildRole.GuildRoleBuilder changeHome(boolean changeHome) {
            this.changeHome = changeHome;
            return this;
        }

        public GuildRole.GuildRoleBuilder removeGuild(boolean removeGuild) {
            this.removeGuild = removeGuild;
            return this;
        }

        public GuildRole.GuildRoleBuilder addAlly(boolean addAlly) {
            this.addAlly = addAlly;
            return this;
        }

        public GuildRole.GuildRoleBuilder removeAlly(boolean removeAlly) {
            this.removeAlly = removeAlly;
            return this;
        }

        public GuildRole.GuildRoleBuilder allyChat(boolean allyChat) {
            this.allyChat = allyChat;
            return this;
        }

        public GuildRole.GuildRoleBuilder openVault(boolean openVault) {
            this.openVault = openVault;
            return this;
        }

        public GuildRole.GuildRoleBuilder transferGuild(boolean transferGuild) {
            this.transferGuild = transferGuild;
            return this;
        }

        public GuildRole.GuildRoleBuilder changeStatus(boolean changeStatus) {
            this.changeStatus = changeStatus;
            return this;
        }

        public GuildRole.GuildRoleBuilder activateBuff(boolean activateBuff) {
            this.activateBuff = activateBuff;
            return this;
        }

        public GuildRole.GuildRoleBuilder upgradeGuild(boolean upgradeGuild) {
            this.upgradeGuild = upgradeGuild;
            return this;
        }

        public GuildRole.GuildRoleBuilder depositMoney(boolean depositMoney) {
            this.depositMoney = depositMoney;
            return this;
        }

        public GuildRole.GuildRoleBuilder withdrawMoney(boolean withdrawMoney) {
            this.withdrawMoney = withdrawMoney;
            return this;
        }

        public GuildRole.GuildRoleBuilder claimLand(boolean claimLand) {
            this.claimLand = claimLand;
            return this;
        }

        public GuildRole.GuildRoleBuilder unclaimLand(boolean unclaimLand) {
            this.unclaimLand = unclaimLand;
            return this;
        }

        public GuildRole.GuildRoleBuilder destroy(boolean destroy) {
            this.destroy = destroy;
            return this;
        }

        public GuildRole.GuildRoleBuilder place(boolean place) {
            this.place = place;
            return this;
        }

        public GuildRole.GuildRoleBuilder interact(boolean interact) {
            this.interact = interact;
            return this;
        }

        public GuildRole.GuildRoleBuilder createCode(boolean createCode) {
            this.createCode = createCode;
            return this;
        }

        public GuildRole.GuildRoleBuilder deleteCode(boolean deleteCode) {
            this.deleteCode = deleteCode;
            return this;
        }

        public GuildRole.GuildRoleBuilder seeCodeRedeemers(boolean seeCodeRedeemers) {
            this.seeCodeRedeemers = seeCodeRedeemers;
            return this;
        }

        public GuildRole.GuildRoleBuilder modifyMotd(boolean modifyMotd) {
            this.modifyMotd = modifyMotd;
            return this;
        }

        public GuildRole.GuildRoleBuilder initiateWar(boolean initiateWar) {
            this.initiateWar = initiateWar;
            return this;
        }

        public GuildRole build() {
            return new GuildRole(name, node, level, chat, invite, kick, promote, demote, changePrefix, changeName, changeHome, removeGuild, addAlly, removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney, withdrawMoney, claimLand, unclaimLand, destroy, place, interact, createCode, deleteCode, seeCodeRedeemers, modifyMotd, initiateWar);
        }

        public String toString() {
            return "GuildRole.GuildRoleBuilder(name=" + this.name + ", node=" + this.node + ", level=" + this.level + ", chat=" + this.chat + ", invite=" + this.invite + ", kick=" + this.kick + ", promote=" + this.promote + ", demote=" + this.demote + ", changePrefix=" + this.changePrefix + ", changeName=" + this.changeName + ", changeHome=" + this.changeHome + ", removeGuild=" + this.removeGuild + ", addAlly=" + this.addAlly + ", removeAlly=" + this.removeAlly + ", allyChat=" + this.allyChat + ", openVault=" + this.openVault + ", transferGuild=" + this.transferGuild + ", changeStatus=" + this.changeStatus + ", activateBuff=" + this.activateBuff + ", upgradeGuild=" + this.upgradeGuild + ", depositMoney=" + this.depositMoney + ", withdrawMoney=" + this.withdrawMoney + ", claimLand=" + this.claimLand + ", unclaimLand=" + this.unclaimLand + ", destroy=" + this.destroy + ", place=" + this.place + ", interact=" + this.interact + ", createCode=" + this.createCode + ", deleteCode=" + this.deleteCode + ", seeCodeRedeemers=" + this.seeCodeRedeemers + ", modifyMotd=" + this.modifyMotd + ", initiateWar=" + this.initiateWar + ")";
        }
    }
}
