package me.glaremasters.guilds.commands;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandSetHome extends CommandBase {

    private HashMap<String, Long> cooldowns = new HashMap<>();

    public CommandSetHome() {
        super("sethome", Main.getInstance().getConfig().getString("commands.description.sethome"),
                "guilds.command.sethome", false, null, null, 0,
                0);
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }


    @Override
    public void execute(Player player, String[] args) {
        final FileConfiguration config = Main.getInstance().getConfig();
        int cooldownTime = config
                .getInt("sethome.cool-down"); // Get number of seconds from wherever you want
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        if (cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (
                    System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                // Still cooling down
                Message.sendMessage(player, Message.COMMAND_ERROR_SETHOME_COOLDOWN
                        .replace("{time}", String.valueOf(secondsLeft)));
                return;
            }
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangeHome()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        double setHomeCost = config.getDouble("Requirement.sethome-cost");

        if (Main.vault && setHomeCost != -1) {
            if (Main.getInstance().getEconomy().getBalance(player) < setHomeCost) {
                Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
                return;
            }

            Message.sendMessage(player, Message.COMMAND_CREATE_MONEY_WARNING_SETHOME
                    .replace("{amount}", String.valueOf(setHomeCost)));
        } else {
            Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
        }

        Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (config.getBoolean("require-money")) {

                    EconomyResponse response =
                            Main.getInstance().getEconomy().withdrawPlayer(player, setHomeCost);
                    if (!response.transactionSuccess()) {
                        Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
                        return;
                    }
                }

                String world = player.getWorld().getName();
                double xloc = player.getLocation().getX();
                double yloc = player.getLocation().getY();
                double zloc = player.getLocation().getZ();
                float yaw = player.getLocation().getYaw();
                float pitch = player.getLocation().getPitch();

                Main.getInstance().guildHomesConfig
                        .set(Guild.getGuild(player.getUniqueId()).getName(),
                                world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":"
                                        + pitch);
                Main.getInstance().saveGuildData();
                Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);
                cooldowns.put(player.getName(), System.currentTimeMillis());

                if (config.getBoolean("worldguard.claims")) {

                    BlockVector min = new BlockVector(player.getLocation().getX(), 0,
                            player.getLocation().getZ());
                    BlockVector max = new BlockVector(player.getLocation().getX() + 100, 255,
                            player.getLocation().getZ() + 100);
                    ProtectedRegion region = new ProtectedCuboidRegion(guild.getName(), min, max);
                    RegionContainer container = getWorldGuard().getRegionContainer();
                    RegionManager regions = container.get(player.getWorld());
/*
                    Location loc = player.getLocation();

                    ApplicableRegionSet set = regions.getApplicableRegions(loc);
                    int size = set.size();
                    if (size > 0) {
                        return;
                    }


                    for (ProtectedRegion region2 : set) {
                       if (set.size() > 0) {
                           return;

                       }

                    }

*/
                    if (region != null) {
                        regions.removeRegion(guild.getName());
                    }
                    regions.addRegion(region);
                    player.sendMessage(ChatColor.GREEN +
                            "Remember these! Your claim coordinates are: " + ChatColor.BLUE + Math
                            .ceil(player.getLocation()
                                    .getX()) + ", " + "0.0" + ", " + Math.ceil(player
                            .getLocation().getZ()) + ChatColor.GREEN + " to " + ChatColor.BLUE
                            + (Math.ceil(player.getLocation().getX()
                            + 100)) + ", " + "255.0, " + (Math
                            .ceil(player.getLocation().getZ() + 100)));
                    region.setFlag(DefaultFlag.GREET_MESSAGE,
                            "Entering " + guild.getName() + "'s base");
                    region.setFlag(DefaultFlag.FAREWELL_MESSAGE,
                            "Leaving " + guild.getName() + "'s base");

                    DefaultDomain members = region.getMembers();
                    DefaultDomain owners = region.getOwners();
                    owners.addPlayer(player.getName());
                    guild.getMembers().stream()
                            .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                            .forEach(member -> {
                                members.addPlayer(member.getName());
                            });
                }
            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_CREATE_CANCELLED_SETHOME);
                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });

    }
}