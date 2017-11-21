package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.logging.Level;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.handlers.WorldGuardHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/22/2017.
 */
public class CommandTransfer extends CommandBase {

    public CommandTransfer() {
        super("transfer", Main.getInstance().getConfig().getString("commands.description.transfer"),
                "guilds.command.transfer", false, null,
                "<name>", 1, 1);
    }

    WorldGuardHandler WorldGuard = new WorldGuardHandler();

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canTransfer()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Player transferPlayer = Bukkit.getPlayer(args[0]);

        if (transferPlayer == null || !transferPlayer.isOnline()) {
            Message.sendMessage(player,
                    Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }
        GuildMember oldGuildMaster = guild.getMember(player.getUniqueId());
        GuildMember newGuildMaster = guild.getMember(transferPlayer.getUniqueId());

        if (newGuildMaster == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD
                    .replace("{player}", transferPlayer.getName()));
            return;
        }
        if (newGuildMaster.getRole() != 1) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_OFFICER);
            return;
        }
        GuildRole promotedRole;
        GuildRole oldGuildMasterRole;
        int currentLevel = newGuildMaster.getRole();
        promotedRole = GuildRole.getRole(currentLevel - 1);
        oldGuildMasterRole = GuildRole.getRole(currentLevel + 1);
        if (guild.getMember(player.getUniqueId()).getRole() == 0) {
            oldGuildMaster.setRole(oldGuildMasterRole);
            newGuildMaster.setRole(promotedRole);
            updateGuild("", guild.getName(), Guild.getGuild(guild.getName()).getName());
            Message.sendMessage(player, Message.COMMAND_TRANSFER_SUCCESS);
            Message.sendMessage(transferPlayer, Message.COMMAND_TRANSFER_NEWMASTER);

            RegionContainer container = WorldGuard.getWorldGuard().getRegionContainer();
            RegionManager regions = container.get(player.getWorld());

            if (regions.getRegion(guild.getName()) != null) {
                DefaultDomain owners = regions.getRegion(guild.getName()).getOwners();
                owners.removePlayer(oldGuildMaster.getUniqueId());
                owners.addPlayer(newGuildMaster.getUniqueId());
            }


        }
    }

    public void updateGuild(String errorMessage, String guild, String... params) {
        Main.getInstance().getDatabaseProvider()
                .updateGuild(Guild.getGuild(guild), (result, exception) -> {
                    if (!result) {
                        Main.getInstance().getLogger()
                                .log(Level.SEVERE, String.format(errorMessage, params));

                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }
                });
    }
}


