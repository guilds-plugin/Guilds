//package me.glaremasters.guilds.commands;
//
//import co.aikar.commands.BaseCommand;
//import co.aikar.commands.InvalidCommandArgument;
//import co.aikar.commands.annotation.CommandAlias;
//import co.aikar.commands.annotation.CommandPermission;
//import co.aikar.commands.annotation.Subcommand;
//import me.glaremasters.guilds.Main;
//import me.glaremasters.guilds.api.events.GuildCreateEvent;
//import me.glaremasters.guilds.api.events.GuildJoinEvent;
//import me.glaremasters.guilds.guild.Guild;
//import me.glaremasters.guilds.guild.GuildRole;
//import me.glaremasters.guilds.message.Message;
//import me.glaremasters.guilds.util.ConfirmAction;
//import me.glaremasters.guilds.util.GuildMessageKeys;
//import net.milkbowl.vault.economy.EconomyResponse;
//import org.bukkit.ChatColor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//import java.util.logging.Level;
//
//@CommandAlias("guilds|guild")
//public class GuildsCommands extends BaseCommand {
//
//    @Subcommand("accept")
//    @CommandPermission("guilds.command.accept")
//    public void acceptGuildInvite(Player player, Guild guild) throws InvalidCommandArgument {
//        if (guild.getMember(player.getUniqueId()) != null) {
//            throw new InvalidCommandArgument(GuildMessageKeys.ALREADY_A_MEMBER);
//        }
//
//        if (guild.getStatus().equalsIgnoreCase("private")
//                && !guild.getInvitedMembers().contains(player.getUniqueId())) {
//            throw new InvalidCommandArgument(GuildMessageKeys.NOT_INVITED, "{guild}", guild.getName());
//        }
//
//        if (guild.getMembers().size() >= guild.getMaxMembers()) {
//            throw new InvalidCommandArgument(GuildMessageKeys.GUILD_FULL);
//        }
//
//        GuildJoinEvent ev = new GuildJoinEvent(player, guild);
//
//        if (ev.isCancelled()) {
//            return;
//        }
//
//        guild.sendMessage(Message.COMMAND_ACCEPT_PLAYER_JOINED
//                .replace("{player}", player.getName()));
//        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
//        guild.removeInvitedPlayer(player.getUniqueId());
//        String name = Main.getInstance().getConfig().getBoolean("tablist-use-display-name") ? player
//                .getDisplayName() : player.getName();
//        player.setPlayerListName(
//                ChatColor.translateAlternateColorCodes('&',
//                        Main.getInstance().getConfig().getString("tablist")
//                                .replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())
//                                + name));
//        Message.sendMessage(player,
//                Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
//    }
//
//    @Subcommand("y|confirm|ok|accept")
//    public void confirmAction(CommandSender sender, ConfirmAction action) {
//        action.accept();
//    }
//
//    @Subcommand("n|no|deny|decline")
//    public void denyAction(CommandSender sender, ConfirmAction action) {
//        action.decline();
//    }
//
//    @Subcommand("create")
//    @CommandPermission("guilds.command.create")
//    public void createGuild(Player player, String name) {
//        if (Guild.getGuild(player.getUniqueId()) != null) {
//            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
//            return;
//        }
//        int min = Main.getInstance().getConfig().getInt("name.min-length");
//        int max = Main.getInstance().getConfig().getInt("name.max-length");
//        String regex = Main.getInstance().getConfig().getString("name.regex");
//
//        if (name.length() < min || name.length() > max || !name.matches(regex)) {
//            Message.sendMessage(player, Message.COMMAND_CREATE_NAME_REQUIREMENTS);
//        }
//
//        for (String guildName : Main.getInstance().getGuildHandler().getGuilds().keySet()) {
//            if (guildName.equalsIgnoreCase(name)) {
//                Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_NAME_TAKEN);
//                return;
//            }
//        }
//
//        double requiredMoney = Main.getInstance().getConfig().getDouble("Requirement.cost");
//        if (Main.vault && requiredMoney != -1) {
//            Message.sendMessage(player, Message.COMMAND_CREATE_MONEY_WARNING
//                    .replace("{amount}", String.valueOf(requiredMoney)));
//        } else {
//            Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
//        }
//
//        Main.getInstance().queueConfirmationAction(player, new ConfirmAction() {
//            @Override
//            public void accept() {
//                Guild guild = new Guild(
//                        ChatColor.translateAlternateColorCodes('&', name),
//                        player.getUniqueId()
//                );
//                GuildCreateEvent ev = new GuildCreateEvent(player, guild);
//                Main.getInstance().getServer().getPluginManager().callEvent(ev);
//                if (ev.isCancelled()) {
//                    return;
//                }
//
//                if (requiredMoney != -1) {
//                    EconomyResponse res = Main.getInstance().getEconomy().withdrawPlayer(player, requiredMoney);
//                    if (!res.transactionSuccess()) {
//                        Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
//                        return;
//                    }
//                }
//
//                Main.getInstance().getDatabaseProvider().createGuild(guild, (result, exception) -> {
//                    if (result) {
//                        Message.sendMessage(player,
//                                Message.COMMAND_CREATE_SUCCESSFUL.replace("{guild}", name));
//
//                        Main.getInstance().getScoreboardHandler().update();
//                        Main.getInstance().getScoreboardHandler().show(player);
//                        Main.getInstance().guildStatusConfig.set(guild.getName(), "Private");
//                        Main.getInstance().guildTiersConfig.set(guild.getName(), 1);
//                        Main.getInstance().saveGuildStatus();
//                        Main.getInstance().saveGuildTiers();
//                        String name =
//                                Main.getInstance().getConfig().getBoolean("tablist-use-display-name") ? player
//                                        .getDisplayName() : player.getName();
//                        player.setPlayerListName(
//                                ChatColor.translateAlternateColorCodes('&',
//                                        Main.getInstance().getConfig().getString("tablist")
//                                                .replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())
//                                                + name));
//                    } else {
//                        Message.sendMessage(player, Message.COMMAND_CREATE_ERROR);
//
//                        Main.getInstance().getLogger().log(Level.SEVERE, String.format(
//                                "An error occurred while player '%s' was trying to create guild '%s'",
//                                player.getName(), name));
//                        if (exception != null) {
//                            exception.printStackTrace();
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void decline() {
//                Message.sendMessage(player, Message.COMMAND_CREATE_CANCELLED);
//            }
//        });
//    }
//
//    @Subcommand("admin")
//    @CommandPermission("guilds.command.admin")
//    public class GuildCommandAdmin extends BaseCommand {
//
//        @Subcommand("delete|remove")
//        public void deleteGuild(CommandSender sender, Guild guild) {
//            Message.sendMessage(sender,
//                    Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", guild.getName()));
//            Main.getInstance().queueConfirmationAction(sender, new ConfirmAction() {
//                @Override
//                public void accept() {
//                    Main.getInstance().getDatabaseProvider()
//                            .removeGuild(guild, (res, ex) -> {
//                                if (res) {
//                                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_SUCCESSFUL
//                                            .replace("{guild}", guild.getName()));
//                                    Main.getInstance().getScoreboardHandler().update();
//                                } else {
//                                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_ERROR);
//
//                                    Main.getInstance().getLogger().log(Level.SEVERE, String.format(
//                                            "An error occurred while player '%s' was trying to delete guild '%s'",
//                                            sender.getName(), guild.getName()));
//                                    if (ex != null) {
//                                        ex.printStackTrace();
//                                    }
//                                }
//                            });
//                }
//
//                @Override
//                public void decline() {
//                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_CANCELLED);
//                }
//            });
//        }
//    }
//}
