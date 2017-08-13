package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandDelete extends CommandBase {

    public CommandDelete() {
        super("delete", "delete your current guild", "guilds.command.delete", false,
                new String[]{"disband"}, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canRemoveGuild()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Message.sendMessage(player,
                Message.COMMAND_DELETE_WARNING.replace("{guild}", guild.getName()));

        Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event =
                        new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                Main.getInstance().getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }

                Main.getInstance().getDatabaseProvider().removeGuild(guild, (result, exception) -> {
                    if (result) {
                        Message.sendMessage(player,
                                Message.COMMAND_DELETE_SUCCESSFUL.replace("{guild}", guild.getName()));
                        Main.getInstance().getGuildHandler().removeGuild(guild);
                        Main.getInstance().getScoreboardHandler().update();
                    } else {
                        Message.sendMessage(player, Message.COMMAND_DELETE_ERROR);

                        Main.getInstance().getLogger().log(Level.SEVERE, String.format(
                                "An error occurred while player '%s' was trying to delete guild '%s'",
                                player.getName(), guild.getName()));
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }
                });

                String name = Main.getInstance().getConfig().getBoolean("tablist-use-display-name") ? player
                        .getDisplayName() : player.getName();
                player.setPlayerListName(
                        ChatColor.translateAlternateColorCodes('&',
                                name));

                Main.getInstance().getCommandHandler().removeAction(player);
            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_DELETE_CANCELLED);
                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }
}
