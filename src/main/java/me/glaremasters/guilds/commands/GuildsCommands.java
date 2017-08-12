package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import me.glaremasters.guilds.util.GuildMessageKeys;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@CommandAlias("guilds|guild")
public class GuildsCommands extends BaseCommand {

    @Subcommand("accept")
    @CommandPermission("guilds.command.accept")
    public void acceptGuildInvite(Player player, Guild guild) throws InvalidCommandArgument {
        if (guild.getMember(player.getUniqueId()) != null) {
            throw new InvalidCommandArgument(GuildMessageKeys.ALREADY_A_MEMBER);
        }

        if (guild.getStatus().equalsIgnoreCase("private")
                && !guild.getInvitedMembers().contains(player.getUniqueId())) {
            throw new InvalidCommandArgument(GuildMessageKeys.NOT_INVITED, "{guild}", guild.getName());
        }

        if (guild.getMembers().size() >= guild.getMaxMembers()) {
            throw new InvalidCommandArgument(GuildMessageKeys.GUILD_FULL);
        }

        GuildJoinEvent ev = new GuildJoinEvent(player, guild);

        if (ev.isCancelled()) {
            return;
        }

        guild.sendMessage(Message.COMMAND_ACCEPT_PLAYER_JOINED
                .replace("{player}", player.getName()));
        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
        guild.removeInvitedPlayer(player.getUniqueId());
        String name = Main.getInstance().getConfig().getBoolean("tablist-use-display-name") ? player
                .getDisplayName() : player.getName();
        player.setPlayerListName(
                ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfig().getString("tablist")
                                .replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())
                                + name));
        Message.sendMessage(player,
                Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
    }

    @Subcommand("y|confirm|ok|accept")
    public void confirmAction(CommandSender sender, ConfirmAction action) {
        action.accept();
    }

    @Subcommand("n|no|deny|decline")
    public void denyAction(CommandSender sender, ConfirmAction action) {
        action.decline();
    }

    @Subcommand("admin")
    @CommandPermission("guilds.command.admin")
    public static class GuildCommandAdmin extends BaseCommand {

        @Subcommand("delete|remove")
        public void deleteGuild(CommandSender sender, Guild guild) {
            Message.sendMessage(sender,
                    Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", guild.getName()));
            Main.getInstance().queueConfirmationAction(sender, new ConfirmAction() {
                @Override
                public void accept() {
                    Main.getInstance().getDatabaseProvider()
                            .removeGuild(guild, (res, ex) -> {
                                if (res) {
                                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_SUCCESSFUL
                                            .replace("{guild}", guild.getName()));
                                    Main.getInstance().getScoreboardHandler().update();
                                } else {
                                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_ERROR);

                                    Main.getInstance().getLogger().log(Level.SEVERE, String.format(
                                            "An error occurred while player '%s' was trying to delete guild '%s'",
                                            sender.getName(), guild.getName()));
                                    if (ex != null) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                }

                @Override
                public void decline() {
                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_CANCELLED);
                }
            });
        }
    }
}
