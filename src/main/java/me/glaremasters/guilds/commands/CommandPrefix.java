package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class CommandPrefix extends CommandBase {

    public CommandPrefix() {
        super("prefix", "",
                "guilds.command.prefix", false, null,
                "<prefix>", 1, 1);
    }

    @Override
    public void execute(Player player,String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        guild.updatePrefix(color(args[0]));
    }

}
