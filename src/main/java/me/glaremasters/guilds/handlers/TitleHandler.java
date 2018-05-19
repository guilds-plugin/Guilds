package me.glaremasters.guilds.handlers;

import static me.glaremasters.guilds.util.ConfigUtil.getBoolean;
import static me.glaremasters.guilds.util.ConfigUtil.getInt;
import static me.glaremasters.guilds.util.ConfigUtil.getString;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class TitleHandler {

    private final Guilds plugin;

    public TitleHandler(Guilds plugin) {
        this.plugin = plugin;
    }

    public void joinTitles(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (getBoolean("titles.enabled")) {
            try {
                String creation = "titles.events.player-joins-guild";
                guild.sendTitle(
                        getString(creation + ".title").replace("{username}", player.getName()), getString(creation + ".sub-title").replace("{username}", player.getName()),
                        getInt(creation + ".fade-in") * 20,
                        getInt(creation + ".stay") * 20,
                        getInt(creation + ".fade-out") * 20);
            } catch (NoSuchMethodError error) {
                String creation = "titles.events.player-joins-guild";
                guild.sendTitleOld(getString(creation + ".title").replace("{username}", player.getName()), getString(creation + ".sub-title")
                        .replace("{username}", player.getName()));
            }

        }
    }

    public void createTitles(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (plugin.getConfig().getBoolean("titles.enabled")) {
            try {
                String creation = "titles.events.guild-creation";
                guild.sendTitle(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(creation + ".title")
                                .replace("{guild}", guild.getName())),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{guild}", guild.getName())),
                        plugin.getConfig().getInt(creation + ".fade-in") * 20,
                        plugin.getConfig().getInt(creation + ".stay") * 20,
                        plugin.getConfig().getInt(creation + ".fade-out") * 20);
            } catch (NoSuchMethodError error) {
                String creation = "titles.events.guild-creation";
                guild.sendTitleOld(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(creation + ".title")
                                .replace("{guild}", guild.getName())),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{guild}", guild.getName())));
            }

        }
    }

    public void tierTitles(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (plugin.getConfig().getBoolean("titles.enabled")) {
            try {
                String creation = "titles.events.guild-tier-upgrade";
                guild.sendTitle(ChatColor
                                .translateAlternateColorCodes('&',
                                        plugin.getConfig().getString(creation + ".title").replace("{tier}",
                                                Integer.toString(guild.getTier()))),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{tier}",
                                                Integer.toString(guild.getTier()))),
                        plugin.getConfig().getInt(creation + ".fade-in") * 20,
                        plugin.getConfig().getInt(creation + ".stay") * 20,
                        plugin.getConfig().getInt(creation + ".fade-out") * 20);
            } catch (NoSuchMethodError error) {
                String creation = "titles.events.guild-tier-upgrade";
                guild.sendTitleOld(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(creation + ".title")
                                .replace("{tier}", Integer.toString(guild.getTier()))),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{tier}",
                                                Integer.toString(guild.getTier()))));
            }

        }
    }

    public void prefixTitles(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (plugin.getConfig().getBoolean("titles.enabled")) {
            try {
                String creation = "titles.events.guild-prefix-change";
                guild.sendTitle(ChatColor.translateAlternateColorCodes('&',
                        Guilds.getInstance().getConfig().getString(creation + ".title")
                                .replace("{prefix}", guild.getPrefix())),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{prefix}", guild.getPrefix())),
                        plugin.getConfig().getInt(creation + ".fade-in") * 20,
                        plugin.getConfig().getInt(creation + ".stay") * 20,
                        plugin.getConfig().getInt(creation + ".fade-out") * 20);
            } catch (NoSuchMethodError error) {
                String creation = "titles.events.guild-prefix-change";
                guild.sendTitleOld(ChatColor.translateAlternateColorCodes('&',
                        Guilds.getInstance().getConfig().getString(creation + ".title")
                                .replace("{prefix}", guild.getPrefix())),
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString(creation + ".sub-title")
                                        .replace("{prefix}", guild.getPrefix())));
            }

        }
    }

}
