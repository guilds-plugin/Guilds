package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildInviteEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandInvite extends CommandBase {

    public CommandInvite() {
        super("invite", Main.getInstance().getConfig().getString("commands.description.invite"),
                "guilds.command.invite", false, null,
                "<player>", 1, 1);
    }

    @SuppressWarnings("deprecation")
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());

        if (!role.canInvite()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Player invitedPlayer = Bukkit.getPlayer(args[0]);

        if (invitedPlayer == null || !invitedPlayer.isOnline()) {
            Message.sendMessage(player,
                    Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        Guild invitedPlayerGuild = Guild.getGuild(invitedPlayer.getUniqueId());
        if (invitedPlayerGuild != null && guild.getName().equals(invitedPlayerGuild.getName())) {
            Message.sendMessage(player, Message.COMMAND_INVITE_ALREADY_IN_GUILD);
            return;
        }
        if (guild.getInvitedMembers().contains(invitedPlayer.getUniqueId())){
            Message.sendMessage(player, Message.COMMAND_INVITE_ALREADY_INVITED);
            return;
        }
        GuildInviteEvent event = new GuildInviteEvent(player, guild, invitedPlayer);
        Main.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        guild.inviteMember(invitedPlayer.getUniqueId());

        Message.sendMessage(invitedPlayer, Message.COMMAND_INVITE_MESSAGE
                .replace("{player}", player.getName(), "{guild}", guild.getName()));
        Message.sendMessage(player,
                Message.COMMAND_INVITE_SUCCESSFUL.replace("{player}", invitedPlayer.getName()));
    }
}
