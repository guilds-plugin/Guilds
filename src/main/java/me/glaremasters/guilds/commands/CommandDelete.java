package me.glaremasters.guilds.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.logging.Level;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.handlers.NameTagEditHandler;
import me.glaremasters.guilds.handlers.TablistHandler;
import me.glaremasters.guilds.handlers.WorldGuardHandler;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandDelete extends CommandBase {

    public CommandDelete() {
        super("delete", Guilds.getInstance().getConfig().getString("commands.description.delete"),
                "guilds.command.delete", false,
                new String[]{"disband"}, null, 0, 0);
    }

    WorldGuardHandler WorldGuard = new WorldGuardHandler();
    TablistHandler TablistHandler = new TablistHandler(Guilds.getInstance());
    NameTagEditHandler NTEHandler = new NameTagEditHandler(Guilds.getInstance());

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

        Guilds.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event =
                        new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                Guilds guilds = Guilds.getInstance();
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }

                guilds.getDatabaseProvider().removeGuild(guild, (result, exception) -> {
                    if (result) {
                        if (Guilds.getInstance().getConfig().getBoolean("hooks.worldguard")) {
                            RegionContainer container = WorldGuard.getWorldGuard()
                                    .getRegionContainer();
                            RegionManager regions = container.get(player.getWorld());

                            if (regions.getRegion(guild.getName()) != null) {
                                regions.removeRegion(guild.getName());
                            }
                        }

                        Message.sendMessage(player,
                                Message.COMMAND_DELETE_SUCCESSFUL
                                        .replace("{guild}", guild.getName()));
                        guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                                .forEach(member -> {
                                    for (int i = 1; i <= guild.getTier(); i++) {
                                        for (String perms : Guilds.getInstance().getConfig().getStringList("tier" + i + ".permissions")) {
                                            Guilds.getPermissions().playerRemove(null, member, perms);
                                        }
                                    }
                                });
                        guilds.getGuildHandler().removeGuild(guild);
                        guilds.guildBanksConfig
                                .set(guild.getName(), null);
                        guilds.guildTiersConfig
                                .set(guild.getName(), null);
                        guilds.guildHomesConfig
                                .set(guild.getName(), null);
                        guilds.saveGuildData();




                    } else {
                        Message.sendMessage(player, Message.COMMAND_DELETE_ERROR);

                        guilds.getLogger().log(Level.SEVERE, String.format(
                                "An error occurred while player '%s' was trying to delete guild '%s'",
                                player.getName(), guild.getName()));
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }
                });

                TablistHandler.leaveTablist(player);
                NTEHandler.removeTag(player);

                guilds.getCommandHandler().removeAction(player);
            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_DELETE_CANCELLED);
                Guilds.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }
}
