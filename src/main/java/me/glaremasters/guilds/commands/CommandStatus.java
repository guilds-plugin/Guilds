package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class CommandStatus extends CommandBase {

    public CommandStatus() {
        super("status", Guilds.getGuilds().getConfig().getString("commands.description.status"),
                "guilds.command.status", false, null,
                "<status>", 1, 1);
    }

    @Override
    public void execute(Player player,String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        boolean argCheck = !args[0].equalsIgnoreCase("private") && !args[0].equalsIgnoreCase("public");
        if (argCheck) return;
        String status = StringUtils.capitalize(args[0]);
        guild.updateStatus(status);
    }

}
