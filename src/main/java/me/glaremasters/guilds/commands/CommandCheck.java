package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;


/**
 * Created by GlareMasters on 7/27/2017.
 */
public class CommandCheck extends CommandBase {

    public CommandCheck() {
        super("check", Guilds.getInstance().getConfig().getString("commands.description.check"),
                "guilds.command.check", false,
                null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild2 = Guild.getGuild(player.getUniqueId());
        if (!(guild2 == null)) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }
        List<String> guilds = new ArrayList<>();
        for (Guild guild : Guilds.getInstance().getGuildHandler().getGuilds().values()) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                continue;
            }

            guilds.add(guild.getName());
        }

        if (guilds.size() > 0) {
            Message.sendMessage(player, Message.EVENT_JOIN_PENDING_INVITES
                    .replace("{number}", String.valueOf(guilds.size()), "{guilds}",
                            String.join(",", guilds)));
        }
    }
}

