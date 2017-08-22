package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GlareMasters on 7/27/2017.
 */
public class CommandCheck extends CommandBase {

    public CommandCheck() {
        super("check", Main.getInstance().getConfig().getString("commands.description.check"),
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
        for (Guild guild : Main.getInstance().getGuildHandler().getGuilds().values()) {
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

