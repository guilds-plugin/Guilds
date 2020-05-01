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

import java.util.List;

public class GuildTier {

    private final int level;
    private final transient String name;
    private final transient double cost;
    private final transient int maxMembers;
    private final transient int vaultAmount;
    private final transient double mobXpMultiplier;
    private final transient double damageMultiplier;
    private final transient double maxBankBalance;
    private final transient int membersToRankup;
    private final transient int maxAllies;
    private final transient boolean useBuffs;
    private final transient List<String> permissions;

    public GuildTier(int level, String name, double cost, int maxMembers, int vaultAmount, double mobXpMultiplier, double damageMultiplier, double maxBankBalance, int membersToRankup, int maxAllies, boolean useBuffs, List<String> permissions) {
        this.level = level;
        this.name = name;
        this.cost = cost;
        this.maxMembers = maxMembers;
        this.vaultAmount = vaultAmount;
        this.mobXpMultiplier = mobXpMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.maxBankBalance = maxBankBalance;
        this.membersToRankup = membersToRankup;
        this.maxAllies = maxAllies;
        this.useBuffs = useBuffs;
        this.permissions = permissions;
    }

    public static GuildTierBuilder builder() {
        return new GuildTierBuilder();
    }

    public int getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    public double getCost() {
        return this.cost;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public int getVaultAmount() {
        return this.vaultAmount;
    }

    public double getMobXpMultiplier() {
        return this.mobXpMultiplier;
    }

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public double getMaxBankBalance() {
        return this.maxBankBalance;
    }

    public int getMembersToRankup() {
        return this.membersToRankup;
    }

    public int getMaxAllies() {
        return this.maxAllies;
    }

    public boolean isUseBuffs() {
        return this.useBuffs;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public String toString() {
        return "GuildTier(level=" + this.getLevel() + ", name=" + this.getName() + ", cost=" + this.getCost() + ", maxMembers=" + this.getMaxMembers() + ", vaultAmount=" + this.getVaultAmount() + ", mobXpMultiplier=" + this.getMobXpMultiplier() + ", damageMultiplier=" + this.getDamageMultiplier() + ", maxBankBalance=" + this.getMaxBankBalance() + ", membersToRankup=" + this.getMembersToRankup() + ", useBuffs=" + this.isUseBuffs() + ", permissions=" + this.getPermissions() + ")";
    }

    public static class GuildTierBuilder {
        private int level;
        private String name;
        private double cost;
        private int maxMembers;
        private int vaultAmount;
        private double mobXpMultiplier;
        private double damageMultiplier;
        private double maxBankBalance;
        private int membersToRankup;
        private int maxAllies;
        private boolean useBuffs;
        private List<String> permissions;

        GuildTierBuilder() {
        }

        public GuildTier.GuildTierBuilder level(int level) {
            this.level = level;
            return this;
        }

        public GuildTier.GuildTierBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GuildTier.GuildTierBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public GuildTier.GuildTierBuilder maxMembers(int maxMembers) {
            this.maxMembers = maxMembers;
            return this;
        }

        public GuildTier.GuildTierBuilder vaultAmount(int vaultAmount) {
            this.vaultAmount = vaultAmount;
            return this;
        }

        public GuildTier.GuildTierBuilder mobXpMultiplier(double mobXpMultiplier) {
            this.mobXpMultiplier = mobXpMultiplier;
            return this;
        }

        public GuildTier.GuildTierBuilder damageMultiplier(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
            return this;
        }

        public GuildTier.GuildTierBuilder maxBankBalance(double maxBankBalance) {
            this.maxBankBalance = maxBankBalance;
            return this;
        }

        public GuildTier.GuildTierBuilder membersToRankup(int membersToRankup) {
            this.membersToRankup = membersToRankup;
            return this;
        }

        public GuildTier.GuildTierBuilder maxAllies(int maxAllies) {
            this.maxAllies = maxAllies;
            return this;
        }

        public GuildTier.GuildTierBuilder useBuffs(boolean useBuffs) {
            this.useBuffs = useBuffs;
            return this;
        }

        public GuildTier.GuildTierBuilder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public GuildTier build() {
            return new GuildTier(level, name, cost, maxMembers, vaultAmount, mobXpMultiplier, damageMultiplier, maxBankBalance, membersToRankup, maxAllies, useBuffs, permissions);
        }

        public String toString() {
            return "GuildTier.GuildTierBuilder(level=" + this.level + ", name=" + this.name + ", cost=" + this.cost + ", maxMembers=" + this.maxMembers + ", vaultAmount=" + this.vaultAmount + ", mobXpMultiplier=" + this.mobXpMultiplier + ", damageMultiplier=" + this.damageMultiplier + ", maxBankBalance=" + this.maxBankBalance + ", membersToRankup=" + this.membersToRankup + ", useBuffs=" + this.useBuffs + ", permissions=" + this.permissions + ")";
        }
    }
}
