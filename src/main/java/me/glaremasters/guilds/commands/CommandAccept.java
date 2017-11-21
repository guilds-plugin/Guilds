package me.glaremasters.guilds.commands;

import com.nametagedit.plugin.NametagEdit;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.WorldGuardHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandAccept extends CommandBase {

    public CommandAccept() {
        super("accept", Main.getInstance().getConfig().getString("commands.description.accept"),
                "guilds.command.accept", false,
                new String[]{"join"}, "<guild id>", 0, 1);
    }

    WorldGuardHandler wg = new WorldGuardHandler();


    public void execute(Player player, String[] args) {
        final FileConfiguration config = Main.getInstance().getConfig();
        if (Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }
        Guild guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values()
                .toArray()[0];
        try {
            if (args.length == 0) {
                int invites = 0;
                int indexes = 0;
                for (int i = 0;
                        i < Main.getInstance().getGuildHandler().getGuilds().values().size(); i++) {
                    Guild guildtmp = (Guild) Main.getInstance().getGuildHandler().getGuilds()
                            .values().toArray()[i];
                    if (guildtmp.getInvitedMembers().contains(player.getUniqueId())) {
                        invites++;
                        indexes = i;
                    }
                }
                if (invites == 1) {
                    guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values()
                            .toArray()[indexes];
                } else {
                    Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
                    return;
                }
            } else {
                guild = Guild.getGuild(args[0]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (guild == null) {
            Message.sendMessage(player,
                    Message.COMMAND_ERROR_GUILD_NOT_FOUND.replace("{input}", args[0]));
            return;
        }

        if (guild.getStatus().equalsIgnoreCase("private")) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
                return;
            }
        }

        int maxMembers = guild.getMaxMembers();
        if (guild.getMembers().size() >= maxMembers) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        if (event.isCancelled()) {
            return;
        }

        guild.sendMessage(
                Message.COMMAND_ACCEPT_PLAYER_JOINED.replace("{player}", player.getName()));

        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
        guild.removeInvitedPlayer(player.getUniqueId());
        if (Main.getInstance().getConfig().getBoolean("hooks.worldguard")) {

            RegionContainer container = wg.getWorldGuard().getRegionContainer();
            RegionManager regions = container.get(player.getWorld());

            if (regions.getRegion(guild.getName()) != null) {
                regions.getRegion(guild.getName()).getMembers().addPlayer(player.getName());
            }
        }

        if (config.getBoolean("titles.enabled")) {
            try {
                String creation = "titles.events.player-joins-guild";
                guild.sendTitle(config.getString(creation + ".title")
                                .replace("{username}", player.getName()),
                        config.getString(creation + ".sub-title")
                                .replace("{username}", player.getName()),
                        config.getInt(creation + ".fade-in") * 20,
                        config.getInt(creation + ".stay") * 20,
                        config.getInt(creation + ".fade-out") * 20);
            } catch (NoSuchMethodError error) {
                String creation = "titles.events.player-joins-guild";
                guild.sendTitleOld(config.getString(creation + ".title")
                                .replace("{username}", player.getName()),
                        config.getString(creation + ".sub-title")
                                .replace("{username}", player.getName()));
            }

        }

        if (config.getBoolean("tablist-guilds")) {
            String name =
                    config.getBoolean("tablist-use-display-name") ? player
                            .getDisplayName() : player.getName();
            player.setPlayerListName(
                    ChatColor.translateAlternateColorCodes('&',
                            config.getString("tablist")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())
                                    + name));
        }
        if (config.getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi()
                    .setPrefix(player, ChatColor.translateAlternateColorCodes('&',
                            config
                                    .getString("nametagedit.name")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())));
        }

        Message.sendMessage(player,
                Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
    }
}
