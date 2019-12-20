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

package me.glaremasters.guilds.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PlaceholderAPI extends PlaceholderExpansion {

    private GuildHandler guildHandler;
    private DecimalFormat df = new DecimalFormat("###.##");

    public PlaceholderAPI(GuildHandler guildHandler) {
        this.guildHandler = guildHandler;
    }

    /**
     * Get the identifier of the expansion
     * @return identifier
     */
    @Override
    public String getIdentifier() {
        return "guilds";
    }

    /**
     * Keep alive through papi reloads
     * @return
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Get the author of the expansion
     * @return author
     */
    @Override
    public String getAuthor() {
        return "blockslayer22";
    }

    /**
     * Get the version of the expansion
     * @return version
     */
    @Override
    public String getVersion() {
        return "2.1";
    }

    /**
     * ADd in the placeholders
     * @param p player to check
     * @param arg the placeholder to check with
     * @return placeholder
     */
    @Override
    public String onPlaceholderRequest(Player p, String arg) {

        if (p == null)  return null;

        GuildsAPI api = Guilds.getApi();
        if (api == null) return null;

        String lowerArg = arg.toLowerCase();

        if (api.getGuild(p) == null) return "";

        switch (lowerArg) {
            case "id":
                return api.getGuild(p).getId().toString();
            case "name":
                return api.getGuild(p).getName();
            case "master":
                return Bukkit.getOfflinePlayer(api.getGuild(p).getGuildMaster().getUuid()).getName();
            case "member_count":
                return String.valueOf(api.getGuild(p).getMembers().size());
            case "prefix":
                return api.getGuild(p).getPrefix();
            case "members_online":
                return String.valueOf(api.getGuild(p).getOnlineMembers().size());
            case "status":
                return api.getGuild(p).getStatus().name();
            case "role":
                return api.getGuildRole(p).getName();
            case "tier":
                return String.valueOf(api.getGuild(p).getTier().getLevel());
            case "tier_name":
                return api.getGuild(p).getTier().getName();
            case "balance":
                return df.format(api.getGuild(p).getBalance());
            case "code_amount":
                return String.valueOf(api.getGuild(p).getCodes().size());
            case "max_members":
                return String.valueOf(api.getGuild(p).getTier().getMaxMembers());
            case "max_balance":
                return String.valueOf(api.getGuild(p).getTier().getMaxBankBalance());
            case "formatted":
                return guildHandler.getFormattedPlaceholder(p);
            case "challenge_wins":
                return String.valueOf(api.getGuild(p).getGuildScore().getWins());
            case "challenge_loses":
                return String.valueOf(api.getGuild(p).getGuildScore().getLoses());
            case "motd":
                return api.getGuild(p).getMotd();
            default:
                return "";
        }
    }

}
