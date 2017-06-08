package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import me.bramhaag.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@SuppressWarnings("deprecation")
public class CommandAdmin extends CommandBase {

    public CommandAdmin() {
        super("admin", "Admin command for managing guilds", "guilds.command.admin", true, null, "<remove | info> <guild name>, or <addplayer | removeplayer> <guild name> <player name>", 2, 3);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Guild guild = Guild.getGuild(args[1]);

        if (guild == null) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", args[1]));

            Main.getInstance().getCommandHandler().addAction(sender, new ConfirmAction() {
                @Override
                public void accept() {
                    Main.getInstance().getDatabaseProvider().removeGuild(guild, (result, exception) -> {
                        if (result) {
                            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_SUCCESSFUL.replace("{guild}", guild.getName()));
                            Main.getInstance().getScoreboardHandler().update();
                        } else {
                            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_ERROR);

                            Main.getInstance().getLogger().log(Level.SEVERE, String.format("An error occurred while player '%s' was trying to delete guild '%s'", sender.getName(), guild.getName()));
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                        }
                    });

                    Main.getInstance().getCommandHandler().removeAction(sender);
                }

                @Override
                public void decline() {
                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_CANCELLED);
                    Main.getInstance().getCommandHandler().removeAction(sender);
                }
            });
        } else if (args[0].equalsIgnoreCase("addplayer")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }

            Player player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_PLAYER_NOT_FOUND);
                return;
            }

            if (Guild.getGuild(player.getUniqueId()) != null) {
                Message.sendMessage(sender, Message.COMMAND_ADMIN_PLAYER_ALREADY_IN_GUILD);
                return;
            }

            guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());

            Message.sendMessage(player, Message.COMMAND_ACCEPT_SUCCESSFUL);
            Message.sendMessage(sender, Message.COMMAND_ADMIN_ADDED_PLAYER);
        } else if (args[0].equalsIgnoreCase("removeplayer")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }

            Player player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_PLAYER_NOT_FOUND);
                return;
            }

            if (Guild.getGuild(player.getUniqueId()) == null) {
                Message.sendMessage(sender, Message.COMMAND_ADMIN_PLAYER_NOT_IN_GUILD);
                return;
            }

            guild.removeMember(player.getUniqueId());

            Message.sendMessage(player, Message.COMMAND_LEAVE_SUCCESSFUL);
            Message.sendMessage(sender, Message.COMMAND_ADMIN_REMOVED_PLAYER);
        } else if (args[0].equalsIgnoreCase("info")) {
            Message.sendMessage(sender, Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
            Message.sendMessage(sender, Message.COMMAND_INFO_NAME.replace("{guild}", guild.getName(), "{prefix}", guild.getPrefix()));
            Message.sendMessage(sender, Message.COMMAND_INFO_MEMBER_COUNT.replace("{members}", String.valueOf(guild.getMembers().size()), "{members-online}", String.valueOf(guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId())).filter(OfflinePlayer::isOnline).count())));
        }
    }
}
