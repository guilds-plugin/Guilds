package me.glaremasters.guilds.commands;

import com.nametagedit.plugin.NametagEdit;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.logging.Level;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import me.glaremasters.guilds.util.WorldGuardHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandLeave extends CommandBase {

    public CommandLeave() {
        super("leave", Main.getInstance().getConfig().getString("commands.description.leave"),
                "guilds.command.leave", false, null, null, 0, 0);
    }

    WorldGuardHandler wg = new WorldGuardHandler();


    public void execute(Player player, String[] args) {
        final FileConfiguration config = Main.getInstance().getConfig();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
            Message.sendMessage(player, Message.COMMAND_LEAVE_WARNING_GUILDMASTER);
        } else {
            Message.sendMessage(player, Message.COMMAND_LEAVE_WARNING);
        }

        Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {

                RegionContainer container = wg.getWorldGuard().getRegionContainer();
                RegionManager regions = container.get(player.getWorld());

                if (regions.getRegion(guild.getName()) != null) {
                    regions.getRegion(guild.getName()).getMembers().removePlayer(player.getName());
                }

                GuildLeaveEvent leaveEvent = new GuildLeaveEvent(player, guild);
                Main.getInstance().getServer().getPluginManager().callEvent(leaveEvent);
                if (leaveEvent.isCancelled()) {
                    return;
                }

                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    if (Main.getInstance().getConfig().getBoolean("hooks.worldguard")) {
                        if (regions.getRegion(guild.getName()) != null) {
                            regions.removeRegion(guild.getName());
                        }
                    }

                    GuildRemoveEvent removeEvent =
                            new GuildRemoveEvent(player, guild,
                                    GuildRemoveEvent.RemoveCause.REMOVED);
                    Main.getInstance().getServer().getPluginManager().callEvent(removeEvent);
                    Main.getInstance().guildBanksConfig
                            .set(guild.getName(), 0);
                    Main.getInstance().guildTiersConfig
                            .set(guild.getName(), 1);
                    Main.getInstance().guildHomesConfig
                            .set(guild.getName(), 0);
                    Main.getInstance().saveGuildData();
                    if (removeEvent.isCancelled()) {
                        return;
                    }

                    Main.getInstance().getDatabaseProvider()
                            .removeGuild(guild, (result, exception) -> {
                                if (result) {
                                    Main.getInstance().getScoreboardHandler().update();
                                } else {
                                    Main.getInstance().getLogger().log(Level.SEVERE, String.format(
                                            "An error occurred while player '%s' was trying to delete guild '%s'",
                                            player.getName(), guild.getName()));
                                    if (exception != null) {
                                        exception.printStackTrace();
                                    }
                                }
                            });
                }

                guild.removeMember(player.getUniqueId());
                Message.sendMessage(player, Message.COMMAND_LEAVE_SUCCESSFUL);



                if (config.getBoolean("titles.enabled")) {
                    try {
                        String creation = "titles.events.player-leaves-guild";
                        guild.sendTitle(config.getString(creation + ".title")
                                        .replace("{username}", player.getName()),
                                config.getString(creation + ".sub-title")
                                        .replace("{username}", player.getName()),
                                config.getInt(creation + ".fade-in") * 20,
                                config.getInt(creation + ".stay") * 20,
                                config.getInt(creation + ".fade-out") * 20);
                    } catch (NoSuchMethodError error) {
                        String creation = "titles.events.player-leaves-guild";
                        guild.sendTitleOld(config.getString(creation + ".title")
                                        .replace("{username}", player.getName()),
                                config.getString(creation + ".sub-title")
                                        .replace("{username}", player.getName()));
                    }

                }

                if (config.getBoolean("tablist-guilds")) {
                    String name =
                            config.getBoolean("tablist-use-display-name")
                                    ? player
                                    .getDisplayName() : player.getName();
                    player.setPlayerListName(
                            ChatColor.translateAlternateColorCodes('&',
                                    name));
                }
                if (config.getBoolean("hooks.nametagedit")) {
                    NametagEdit.getApi()
                            .setPrefix(player, "");
                }
                Main.getInstance().getCommandHandler().removeAction(player);
                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    guild.sendMessage(
                            Message.COMMAND_LEAVE_GUILDMASTER_LEFT
                                    .replace("{player}", player.getName()));
                } else {
                    guild.sendMessage(
                            Message.COMMAND_LEAVE_PLAYER_LEFT
                                    .replace("{player}", player.getName()));
                }

            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_LEAVE_CANCELLED);

                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }
}
