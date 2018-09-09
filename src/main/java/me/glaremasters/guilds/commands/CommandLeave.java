package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 2:39 PM
 */
public class CommandLeave extends CommandBase {

    private Guilds guilds;

    public CommandLeave(Guilds guilds) {
        super(guilds, "leave",false, null, null, 0, 0);
        this.guilds = guilds;
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        guilds.getCommandHandler().addAction(player, new ConfirmAction() {
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
                    guilds.getCommandHandler().removeAction(player);
                } else {
                    // Send message player left guild
                }
            }

            @Override
            public void decline() {
                guilds.getCommandHandler().removeAction(player);
            }
        });
    }
}
