package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.glaremasters.guilds.Main;
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
        super("boot", Main.getInstance().getConfig().getString("commands.description.boot"),
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

        OfflinePlayer kickedPlayer = Bukkit.getOfflinePlayer(args[0]);

        if (kickedPlayer == null || kickedPlayer.getUniqueId() == null) {

            Message.sendMessage(player,
                    Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }
        GuildMember kickedPlayer2 = guild.getMember(kickedPlayer.getUniqueId());
        Guild guild2 = Guild.getGuild(kickedPlayer2.getUniqueId());

        if (!guild.getName().equals(guild2.getName())) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD);
            return;
        }


        if (kickedPlayer2.equals(guild.getGuildMaster())) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }
        if (kickedPlayer2 == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD
                    .replace("{player}", kickedPlayer.getName()));
            return;
        }

        if (Main.getInstance().getConfig().getBoolean("hooks.worldguard")) {

            RegionContainer container = WorldGuard.getWorldGuard().getRegionContainer();
            RegionManager regions = container.get(player.getWorld());

            if (regions.getRegion(guild.getName()) != null) {
                regions.getRegion(guild.getName()).getMembers().removePlayer(kickedPlayer.getName());
            }
        }

        guild.removeMember(kickedPlayer.getUniqueId());

        Message.sendMessage(player,
                Message.COMMAND_BOOT_SUCCESSFUL.replace("{player}", kickedPlayer.getName()));

        guild.sendMessage(Message.COMMAND_BOOT_PLAYER_KICKED
                .replace("{player}", kickedPlayer.getName(), "{kicker}", player.getName()));
    }
}