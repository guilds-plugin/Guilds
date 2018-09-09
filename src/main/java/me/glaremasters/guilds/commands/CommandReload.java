package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 2:39 PM
 */
public class CommandReload extends CommandBase {

    private Guilds guilds;

    public CommandReload(Guilds guilds) {
        super(guilds, "reload",false, null, null, 0, 0);
        this.guilds = guilds;
    }

    @Override
    public void execute(Player player, String[] args) {
        guilds.reloadConfig();
    }
}
