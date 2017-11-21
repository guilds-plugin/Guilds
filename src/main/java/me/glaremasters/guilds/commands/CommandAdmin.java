package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class CommandAdmin extends CommandBase {

    public CommandAdmin() {
        super("admin", Main.getInstance().getConfig().getString("commands.description.admin"),
                "guilds.command.admin", true, null,
                "<remove | info> <guild name>, "
                        + "or <addplayer | removeplayer> <guild name> <player name>, "
                        + "or <upgrade> <guild name>, or <status> <guild name> <Public | Private>, "
                        + "or <prefix> <guild name> <new prefix>",
                2, 3);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        Guild guild = Guild.getGuild(args[1]);

        if (guild == null) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            Message.sendMessage(sender,
                    Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", args[1]));

            Main.getInstance().getCommandHandler().addAction(sender, new ConfirmAction() {
                @Override
                public void accept() {
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
        } else if (args[0].equalsIgnoreCase("upgrade")) {
            FileConfiguration config = Main.getInstance().getConfig();
            int tier = guild.getTier();
            if (guild.getTier() >= Main.getInstance().getConfig().getInt("max-number-of-tiers")) {
                Message.sendMessage(sender, Message.COMMAND_UPGRADE_TIER_MAX);
                return;
            }
            Message.sendMessage(sender, Message.COMMAND_UPGRADE_SUCCESS);
            Main.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
            Main.getInstance().saveGuildData();

            guild.updateGuild("");
        } else if (args[0].equalsIgnoreCase("status")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }
            if (!(args[2].equalsIgnoreCase("private") || args[2].equalsIgnoreCase("public"))) {
                Message.sendMessage(sender, Message.COMMAND_STATUS_ERROR);
            } else {
                String status = args[2];
                Main.getInstance().guildStatusConfig
                        .set(args[1],
                                status);
                Guild.getGuild(args[1]).updateGuild("");

                Message.sendMessage(sender,
                        Message.COMMAND_STATUS_SUCCESSFUL.replace("{status}", status));
                Main.getInstance().saveGuildData();
            }
        } else if (args[0].equalsIgnoreCase("prefix")) {
            FileConfiguration config = Main.getInstance().getConfig();
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }
            if (!args[2].matches(Main.getInstance().getConfig().getString("prefix.regex"))) {
                Message.sendMessage(sender, Message.COMMAND_PREFIX_REQUIREMENTS);
                return;
            }
            Message.sendMessage(sender, Message.COMMAND_PREFIX_SUCCESSFUL);
            guild.updatePrefix(ChatColor.translateAlternateColorCodes('&', args[2]));
        }
    }
}
