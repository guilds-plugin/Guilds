package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandLeave extends CommandBase {

    public CommandLeave() {
        super("leave", "Leave your current guild", "guilds.command.leave", false, null, null, 0, 0);
    }

    public void execute(Player player, String[] args) {
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
            @Override public void accept() {
                GuildLeaveEvent leaveEvent = new GuildLeaveEvent(player, guild);
                if (leaveEvent.isCancelled()) {
                    return;
                }

                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    GuildRemoveEvent removeEvent =
                        new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
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
                player.setPlayerListName(
                    ChatColor
                        .translateAlternateColorCodes('&',  ChatColor.RESET + player.getName()));

                Main.getInstance().getCommandHandler().removeAction(player);

                guild.sendMessage(
                    Message.COMMAND_LEAVE_PLAYER_LEFT.replace("{player}", player.getName()));
            }

            @Override public void decline() {
                Message.sendMessage(player, Message.COMMAND_LEAVE_CANCELLED);

                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }
}
