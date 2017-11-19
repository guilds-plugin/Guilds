package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by GlareMasters on 11/19/2017.
 */
public class CommandUnclaim extends CommandBase {

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    public CommandUnclaim() {
        super("unclaim", Main.getInstance().getConfig().getString("commands.description.unclaim"),
                "guilds.command.unclaim", false,
                null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {

        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canUnclaimLand()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        if (!Main.getInstance().getConfig().getBoolean("hooks.worldguard")) {
            Message.sendMessage(player, Message.COMMAND_CLAIM_WORLDGUARD_REQUIRED);
            return;
        }
        RegionContainer container = getWorldGuard().getRegionContainer();
        RegionManager regions = container.get(player.getWorld());

        if (regions.getRegion(guild.getName()) == null) {
            Message.sendMessage(player, Message.COMMAND_CLAIM_NO_CLAIM_FOUND);
            return;
        }
        Message.sendMessage(player, Message.COMMAND_CLAIM_REMOVE_CONFIRM);
        Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                regions.removeRegion(guild.getName());
                Message.sendMessage(player, Message.COMMAND_CLAIM_REMOVED);
            }

            @Override
            public void decline() {
                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });

    }


}
