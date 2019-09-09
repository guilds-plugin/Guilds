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

import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildMember {

    private final UUID uuid;
    private long joinDate;
    private long lastLogin;
    private GuildRole role;

    public GuildMember(UUID uuid, GuildRole role) {
        this.uuid = uuid;
        this.role = role;
        this.joinDate = 0;
        this.lastLogin = 0;
    }

    /**
     * Check and see if a guild member is online
     * @return true or false
     */
    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public GuildRole getRole() {
        return this.role;
    }

    public void setRole(GuildRole role) {
        this.role = role;
    }

    public long getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(long joinDate) {
        this.joinDate = joinDate;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}