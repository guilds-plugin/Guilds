package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.logging.Level;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.handlers.NameTagEditHandler;
import me.glaremasters.guilds.handlers.TablistHandler;
import me.glaremasters.guilds.handlers.WorldGuardHandler;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandLeave extends CommandBase {

    public CommandLeave() {
        super("leave", Guilds.getInstance().getConfig().getString("commands.description.leave"),
                "guilds.command.leave", false, null, null, 0, 0);
    }

    WorldGuardHandler WorldGuard = new WorldGuardHandler();
    TablistHandler TablistHandler = new TablistHandler(Guilds.getInstance());
    NameTagEditHandler NTEHandler = new NameTagEditHandler(Guilds.getInstance());

    public void execute(Player player, String[] args) {
        final FileConfiguration config = Guilds.getInstance().getConfig();
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

        Guilds.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (Guilds.getInstance().getConfig().getBoolean("hooks.worldguard")) {
                    RegionContainer container = WorldGuard.getWorldGuard().getRegionContainer();
                    RegionManager regions = container.get(player.getWorld());

                    if (regions.getRegion(guild.getName()) != null) {
                        regions.getRegion(guild.getName()).getMembers()
                                .removePlayer(player.getName());
                    }

                    GuildLeaveEvent leaveEvent = new GuildLeaveEvent(player, guild);
                    Guilds.getInstance().getServer().getPluginManager().callEvent(leaveEvent);
                    if (leaveEvent.isCancelled()) {
                        return;
                    }

                    if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                        if (regions.getRegion(guild.getName()) != null) {
                            regions.removeRegion(guild.getName());
                        }
                    }
                }
                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                            .forEach(member -> {
                                for (String perms : guild.getGuildPerms()) {
                                    Guilds.getPermissions().playerRemove(null, member, perms);
                                }
                            });
                    GuildRemoveEvent removeEvent =
                            new GuildRemoveEvent(player, guild,
                                    GuildRemoveEvent.RemoveCause.REMOVED);
                    Guilds.getInstance().getServer().getPluginManager().callEvent(removeEvent);
                    Guilds.getInstance().guildBanksConfig
                            .set(guild.getName(), null);
                    Guilds.getInstance().guildTiersConfig
                            .set(guild.getName(), null);
                    Guilds.getInstance().guildHomesConfig
                            .set(guild.getName(), null);
                    Guilds.getInstance().saveGuildData();
                    if (removeEvent.isCancelled()) {
                        return;
                    }

                    Guilds.getInstance().getDatabaseProvider()
                            .removeGuild(guild, (result, exception) -> {
                                if (result) {
                                } else {
                                    Guilds.getInstance().getLogger().log(Level.SEVERE, String.format(
                                            "An error occurred while player '%s' was trying to delete guild '%s'",
                                            player.getName(), guild.getName()));
                                    if (exception != null) {
                                        exception.printStackTrace();
                                    }
                                }
                            });
                }
                for (String perms : guild.getGuildPerms()) {
                    Guilds.getPermissions().playerRemove(null, player, perms);
                }
                guild.removeMember(player.getUniqueId());
                Message.sendMessage(player, Message.COMMAND_LEAVE_SUCCESSFUL);

                TablistHandler.leaveTablist(player);

                NTEHandler.removeTag(player);
                Guilds.getInstance().getCommandHandler().removeAction(player);
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

                Guilds.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }
}
