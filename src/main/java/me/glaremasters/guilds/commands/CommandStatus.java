package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/12/2017.
 */
public class CommandStatus extends CommandBase {

    public CommandStatus() {
        super("status", Guilds.getInstance().getConfig().getString("commands.description.status"),
                "guilds.command.status", false, null,
                "<public | private>", 1, 1);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangeStatus()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }
        if (!(args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public"))) {
            Message.sendMessage(player, Message.COMMAND_STATUS_ERROR);
        } else {

            String status = StringUtils.capitalize(args[0]);


            Guilds.getInstance().guildStatusConfig
                    .set(Guild.getGuild(player.getUniqueId()).getName(),
                            status);
            Guild.getGuild(player.getUniqueId()).updateGuild("");

            Message.sendMessage(player,
                    Message.COMMAND_STATUS_SUCCESSFUL.replace("{status}", status));
            Guilds.getInstance().saveGuildData();
        }
    }
}
