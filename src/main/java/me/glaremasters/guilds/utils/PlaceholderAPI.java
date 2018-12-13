package me.glaremasters.guilds.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 12/12/2018
 * Time: 8:36 PM
 */
public class PlaceholderAPI extends PlaceholderExpansion {

    private Guilds guilds;

    @Override
    public String getIdentifier() {
        return "guilds";
    }

    @Override
    public String getPlugin() {
        return null;
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
            return "";
        }

        GuildsAPI api = guilds.getApi();

        switch (arg) {
            case "name":
                return api.getGuild(p);
            case "master":
                return api.getGuildMaster(p);
            case "member_count":
                return api.getGuildMemberCount(p);
            case "prefix":
                return api.getGuildPrefix(p);
            case "members_online":
                return api.getGuildMembersOnline(p);
            case "status":
                return api.getGuildStatus(p);
            case "role":
                return api.getGuildRole(p);
            case "tier":
                return Integer.toString(api.getGuildTier(p));
            case "balance":
                return Double.toString(api.getBankBalance(p));
            case "tier_name":
                return api.getTierName(p);
            case "role_node":
                return api.getRolePermission(p);
        }
        return null;
    }

}
