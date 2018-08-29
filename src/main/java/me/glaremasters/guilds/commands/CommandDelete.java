package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 10:13 AM
 */
public class CommandDelete extends CommandBase {

    private Guilds guilds;

    public CommandDelete(Guilds guilds) {
        super("delete", "", "guilds.command.delete", false, new String[]{"disband"}, null, 0, 0);
        this.guilds = guilds;
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canRemoveGuild()) return;

        guilds.getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildRemoveEvent event = new GuildRemoveEvent(player, guild, GuildRemoveEvent.RemoveCause.REMOVED);
                guilds.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                guilds.getDatabase().removeGuild(guild);
                guilds.getCommandHandler().removeAction(player);
            }

            @Override
            public void decline() {
                guilds.getCommandHandler().removeAction(player);
            }
        });
    }
}
