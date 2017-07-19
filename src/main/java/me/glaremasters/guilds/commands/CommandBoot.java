package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandBoot extends CommandBase {

    public CommandBoot() {
        super("boot", "Kick a player from your guild", "guilds.command.boot", false,
            new String[] {"kick"}, "<player>", 1, 1);
    }

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

        Player kickedPlayer = Bukkit.getPlayer(args[0]);

        if (kickedPlayer == null || !kickedPlayer.isOnline()) {
            Message.sendMessage(player,
                Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        guild.removeMember(kickedPlayer.getUniqueId());

        Message.sendMessage(kickedPlayer,
            Message.COMMAND_BOOT_KICKED.replace("{kicker}", player.getName()));
        Message.sendMessage(player,
            Message.COMMAND_BOOT_SUCCESSFUL.replace("{player}", kickedPlayer.getName()));

        guild.sendMessage(Message.COMMAND_BOOT_PLAYER_KICKED
            .replace("{player}", kickedPlayer.getName(), "{kicker}", player.getName()));
    }
}
