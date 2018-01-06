package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.handlers.WorldGuardHandler;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandBoot extends CommandBase {

    public CommandBoot() {
        super("boot", Guilds.getInstance().getConfig().getString("commands.description.boot"),
                "guilds.command.boot", false,
                new String[]{"kick"}, "<player>", 1, 1);
    }

    WorldGuardHandler WorldGuard = new WorldGuardHandler();


    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        if (!role.canKick()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(args[0]);


        if (bootedPlayer == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        if (bootedPlayer.getUniqueId() == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());


        if (kickedPlayer == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD.replace("{player}", bootedPlayer.getName()));
            return;
        }

        Guild targetGuild = Guild.getGuild(kickedPlayer.getUniqueId());

        if (targetGuild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if (!guild.getName().equals(targetGuild.getName())) {
            return;
        }

        if (kickedPlayer.equals(guild.getGuildMaster())) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }


        if (Guilds.getInstance().getConfig().getBoolean("hooks.worldguard")) {

            RegionContainer container = WorldGuard.getWorldGuard().getRegionContainer();
            RegionManager regions = container.get(player.getWorld());

            if (regions.getRegion(guild.getName()) != null) {
                regions.getRegion(guild.getName()).getMembers().removePlayer(bootedPlayer.getName());
            }
        }

        guild.removeMember(kickedPlayer.getUniqueId());

        for (String perms : guild.getGuildPerms()) {
            Guilds.getPermissions().playerRemove(null, bootedPlayer, perms);
        }

        Message.sendMessage(player,
                Message.COMMAND_BOOT_SUCCESSFUL.replace("{player}", bootedPlayer.getName()));

        guild.sendMessage(Message.COMMAND_BOOT_PLAYER_KICKED
                .replace("{player}", bootedPlayer.getName(), "{kicker}", player.getName()));

            Player online = Bukkit.getPlayerExact(args[0]);

            if (online != null) {
            Message.sendMessage(online, Message.COMMAND_BOOT_KICKED.replace("{kicker}", player.getName()));
        } else {
                return;
            }

    }
}