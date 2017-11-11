package me.glaremasters.guilds.commands;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by GlareMasters on 11/11/2017.
 */
public class CommandClaim extends CommandBase {

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    public CommandClaim() {
        super("claim", Main.getInstance().getConfig().getString("commands.description.claim"),
                "guilds.command.claim", false,
                null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {

        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        if (!Main.getInstance().getConfig().getBoolean("hooks.worldguard")) {
            Message.sendMessage(player, Message.COMMAND_CLAIM_WORLDGUARD_REQUIRED);
            return;
        }

        BlockVector min = new BlockVector((player.getLocation().getX() - 50), 0,
                (player.getLocation().getZ() - 50));
        BlockVector max = new BlockVector((player.getLocation().getX() + 50), 255,
                (player.getLocation().getZ() + 50));
        ProtectedRegion region = new ProtectedCuboidRegion(guild.getName(), min, max);
        RegionContainer container = getWorldGuard().getRegionContainer();
        RegionManager regions = container.get(player.getWorld());

        if (region != null) {
            regions.removeRegion(guild.getName());
        }

        ApplicableRegionSet set = regions.getApplicableRegions(region);
        if (set.size() > 0) {
            Message.sendMessage(player, Message.COMMAND_CLAIM_TOO_CLOSE);
            return;
        }

        regions.addRegion(region);
        Message.sendMessage(player, Message.COMMAND_CLAIM_COORDINATES);
        player.sendMessage(ChatColor.BLUE + "" + Math
                .ceil((player.getLocation().getX() - 50)) + ", " + "0.0" + ", " +
                Math.ceil((player.getLocation().getZ() - 50)) + ChatColor.GREEN + " to " +
                ChatColor.BLUE + (Math.ceil((player.getLocation().getX() + 50)) + ", " + "255.0, "
                + (Math.ceil((player.getLocation().getZ() + 50)))));
        region.setFlag(DefaultFlag.GREET_MESSAGE,
                "Entering " + guild.getName() + "'s base");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE,
                "Leaving " + guild.getName() + "'s base");


        ProtectedRegion regionTest = regions.getRegion(guild.getName());
        Location outlineMin = new Location(player.getWorld(), 0, 0, 0);
        outlineMin.setX(regionTest.getMinimumPoint().getX());
        outlineMin.setY(player.getLocation().getY());
        outlineMin.setZ(regionTest.getMinimumPoint().getZ());

        Location outlineMax = new Location(player.getWorld(), 0, 0, 0);
        outlineMax.setX(regionTest.getMaximumPoint().getX());
        outlineMax.setY(player.getLocation().getY());
        outlineMax.setZ(regionTest.getMaximumPoint().getZ());



        for (double x1 = 0; x1 <= outlineMax.getX() - outlineMin.getX(); x1++) {
            player.sendBlockChange(outlineMin.clone().add(x1, 0, 0), Material.DIRT, (byte) 0);
        }

        for (double z = 0; z <= outlineMax.getZ() - outlineMin.getZ(); z++) {
            player.sendBlockChange(outlineMin.clone().add(0, 0, z), Material.DIRT, (byte) 0);
        }



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



