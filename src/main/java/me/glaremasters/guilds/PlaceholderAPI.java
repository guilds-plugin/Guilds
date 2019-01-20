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

package me.glaremasters.guilds;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.glaremasters.guilds.api.GuildsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "guilds";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "blockslayer22";
    }

    @Override
    public String getVersion() {
        return "1.0.8";
    }

    public String onPlaceholderRequest(Player p, String arg) {

        if (p == null) {
            return null;
        }

        GuildsAPI api = Guilds.getApi();
        if (api == null) return null;

        switch (arg) {
            case "name":
                return api.getGuild(p).getName();
            case "master":
                return Bukkit.getOfflinePlayer(api.getGuild(p).getGuildMaster().getUuid()).getName();
            case "member_count":
                return String.valueOf(api.getGuildMemberCount(p));
            case "prefix":
                return api.getGuildPrefix(p);
            case "members_online":
                return String.valueOf(api.getGuildMembersOnline(p));
            case "status":
                return String.valueOf(api.getGuildStatus(p));
            case "role":
                return api.getGuildRole(p).getName();
            case "tier":
                return String.valueOf(api.getGuildTier(p));
            case "balance":
                return String.valueOf(api.getBankBalance(p));
            case "tier_name":
                return api.getGuildTier(p).getName();
            case "role_node":
                return api.getGuildRole(p).getNode();
        }
        return null;
    }

}
