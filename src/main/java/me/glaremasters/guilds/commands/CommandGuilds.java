package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Message;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ConfirmAction;
import me.glaremasters.guilds.utils.Serialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginDescriptionFile;

import static me.glaremasters.guilds.utils.ConfigUtils.color;
import static me.glaremasters.guilds.utils.ConfigUtils.getInt;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 4:57 PM
 */
@CommandAlias("guild|guilds")
public class CommandGuilds extends BaseCommand {

    @Dependency private Guilds guilds;

    @Subcommand("create")
    @Description("Create a Guild")
    @CommandPermission("guilds.command.create")
    @Syntax("<name>")
    public void onCreate(Player player, String name) {
        if (Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }

        for (String guildName : guilds.getGuildHandler().getGuilds().keySet()) {
            if (guildName.equalsIgnoreCase(name)) {
                Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_NAME_TAKEN);
                return;
            }
        }
        Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                Guild guild = new Guild(color(name), player.getUniqueId());
                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                if (event.isCancelled()) return;
                guilds.getDatabase().createGuild(guild);
                Message.sendMessage(player, Message.COMMAND_CREATE_SUCCESSFUL.replace("{guild}", name));
                guilds.getActionHandler().removeAction(player);
            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_CREATE_CANCELLED);
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("confirm")
    @Description("Confirm an action")
    @CommandPermission("guilds.command.confirm")
    public void onConfirm(Player player) {
        ConfirmAction action = guilds.getActionHandler().getActions().get(player);
        if (action != null) action.accept();
    }

    @Subcommand("cancel")
    @Description("Cancel an action")
    @CommandPermission("guilds.command.cancel")
    public void onCancel(Player player) {
        ConfirmAction action = guilds.getActionHandler().getActions().get(player);
        if (action != null) action.decline();
    }

    @Subcommand("reload")
    @Description("Reloads the plugin's configuration file")
    @CommandPermission("guilds.command.reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage("Reloading config");
        guilds.reloadConfig();
    }

    @Subcommand("sethome")
    @Description("Set your Guild's home")
    @CommandPermission("guilds.command.sethome")
    public void onSetHome(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        String world = player.getWorld().getName();
        double xloc = player.getLocation().getX();
        double yloc = player.getLocation().getY();
        double zloc = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        guild.updateHome(world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":" + pitch);
    }

    @Subcommand("home")
    @Description("Go to your Guild home")
    @CommandPermission("guilds.command.home")
    public void onHome(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        if (guild.getHome().equals("")) return;
        String[] data = guild.getHome().split(":");
        World w = Bukkit.getWorld(data[0]);
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);

        Location guildhome = new Location(w, x, y, z);
        guildhome.setYaw(Float.parseFloat(data[4]));
        guildhome.setPitch(Float.parseFloat(data[5]));
        player.teleport(guildhome);
    }

    @Subcommand("rename")
    @Description("Change the name of your Guild")
    @CommandPermission("guilds.command.rename")
    @Syntax("<name>")
    public void onRename(Player player, String name) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        String oldName = guild.getName();
        guilds.getDatabase().removeGuild(Guild.getGuild(oldName));
        guild.updateName(color(name));
    }

    @Subcommand("status")
    @Description("Change the status of your Guild")
    @CommandPermission("guilds.command.status")
    @Syntax("<private/public>")
    public void onStatus(Player player, String status) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        boolean argCheck = !status.equalsIgnoreCase("private") && !status.equalsIgnoreCase("public");
        if (argCheck) return;
        String updatedStatus = StringUtils.capitalize(status);
        guild.updateStatus(updatedStatus);
    }

    @Subcommand("prefix")
    @Description("Change the prefix of your Guild")
    @CommandPermission("guilds.command.prefix")
    @Syntax("<prefix>")
    public void onPrefix(Player player, String prefix) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        guild.updatePrefix(color(prefix));
    }

    @Subcommand("version|v|ver")
    @Description("Information about the plugin")
    public void onVersion(CommandSender sender) {
        SpigotUpdater updater = new SpigotUpdater(guilds, 48920);
        PluginDescriptionFile pdf = guilds.getDescription();
        try {
            String message;
            if (updater.getLatestVersion().equalsIgnoreCase(pdf.getVersion())) {
                message = "";
            } else {
                message = "\n&8» &7An update has been found! &f- " + updater.getResourceURL();
            }
            sender.sendMessage(
                    color("&8&m--------------------------------------------------"
                            + "\n&8» &7Name - &a"
                            + pdf.getName() + "\n&8» &7Version - &a" + pdf.getVersion()
                            + "\n&8» &7Author - &a" + pdf.getAuthors() + "\n&8» &7Support - &a"
                            + pdf.getWebsite() + message
                            + "\n&8&m--------------------------------------------------"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subcommand("upgrade")
    @Description("Upgrade your Guild tier!")
    @CommandPermission("guilds.command.upgrade")
    public void onUpgrade(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        int tier = guild.getTier();
        if (tier >= getInt("max-number-of-tiers")) return;
        if (guild.getMembersToRankup() != 0 && guild.getMembers().size() < guild.getMembersToRankup()) return;
        double balance = guild.getBalance();
        double upgradeCost = guild.getTierCost();
        if (balance < upgradeCost) return;
        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (balance < upgradeCost) return;
                // Carry over perms check
                guild.updateTier(tier + 1);
                // Add new perms
            }

            @Override
            public void decline() {
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("transfer")
    @Description("Transfer your Guild to another user")
    @CommandPermission("guilds.command.transfer")
    @Syntax("<player>")
    public void onTransfer(Player player, String target) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        Player transferPlayer = Bukkit.getPlayerExact(target);
        if (transferPlayer == null) return;

        GuildMember oldMaster = guild.getMember(player.getUniqueId());
        GuildMember newMaster = guild.getMember(transferPlayer.getUniqueId());

        if (newMaster == null) return;
        if (newMaster.getRole() != 1) return;

        GuildRole newRole, oldRole;
        int currentLevel = newMaster.getRole();
        newRole = GuildRole.getRole(currentLevel - 1);
        oldRole = GuildRole.getRole(currentLevel + 1);

        if (oldMaster.getRole() == 0) {
            oldMaster.setRole(oldRole);
            newMaster.setRole(newRole);
            guild.updateGuild("", guild.getName(), Guild.getGuild(guild.getName()).getName());
        }

    }

    @Subcommand("leave|exit")
    @Description("Leave your Guild")
    @CommandPermission("guilds.command.leave")
    public void onLeave(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildLeaveEvent event = new GuildLeaveEvent(player, guild);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                // If guild master remove perms from all members
                if (guild.getGuildMaster().getUniqueId().equals(player.getUniqueId())) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                    guilds.getServer().getPluginManager().callEvent(removeEvent);
                    if (removeEvent.isCancelled()) return;
                    guilds.getDatabase().removeGuild(guild);
                    guild.removeMember(player.getUniqueId());
                    guilds.getActionHandler().removeAction(player);
                } else {
                    // Send message player left guild
                }
            }

            @Override
            public void decline() {
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("delete")
    @Description("Delete your guild")
    @CommandPermission("guilds.command.delete")
    public void onDelete(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        guilds.getActionHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                guilds.getDatabase().removeGuild(guild);
                guilds.getActionHandler().removeAction(player);
            }

            @Override
            public void decline() {
                guilds.getActionHandler().removeAction(player);
            }
        });
    }

    @Subcommand("decline")
    @Description("Decline a Guild invite")
    @CommandPermission("guilds.command.decline")
    @Syntax("<guild name>")
    public void onDecline(Player player, String name) {
        Guild guild = Guild.getGuild(name);
        if (Guild.getGuild(player.getUniqueId()) != null) return;
        if (guild == null) return;
        if (!guild.getInvitedMembers().contains(player.getUniqueId())) return;
        guild.removeInvitedPlayer(player.getUniqueId());
    }

    @Subcommand("boot|kick")
    @Description("Kick someone from your Guild")
    @CommandPermission("guilds.command.boot")
    @Syntax("<name>")
    public void onKick(Player player, String name) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(name);
        if (bootedPlayer == null) return;
        if (bootedPlayer.getUniqueId() == null) return;

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());
        if (kickedPlayer == null) return;

        Guild targetGuild = Guild.getGuild(kickedPlayer.getUniqueId());
        if (targetGuild == null) return;
        if (!guild.getName().equals(targetGuild.getName())) return;

        if (kickedPlayer.equals(guild.getGuildMaster())) return;
        guild.removeMember(kickedPlayer.getUniqueId());
    }

    @Subcommand("vault")
    @Description("Opens the Guild vault")
    @CommandPermission("guilds.command.vault")
    public void onVault(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        if (guild.getInventory().equalsIgnoreCase("")) {
            Inventory inv = Bukkit.createInventory(null, 54, guild.getName() + "'s Guild Vault");
            player.openInventory(inv);
            return;
        }
        try {
            player.openInventory(Serialization.deserializeInventory(guild.getInventory()));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @HelpCommand
    @CommandPermission("guilds.command.help")
    @Syntax("")
    @Description("Show this help menu")
    public static void onHelp(CommandHelp help) {
        help.showHelp();
    }


}
