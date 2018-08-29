package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 10:00 AM
 */
public class CommandConfirm extends CommandBase {

    private Guilds guilds;

    public CommandConfirm(Guilds guilds) {
        super(guilds, "confirm", false, null, null, 0, 0);
        this.guilds = guilds;
    }

    public void execute(Player player, String[] args) {
        ConfirmAction action = guilds.getCommandHandler().getActions().get(player);
        if (action != null) action.accept();
    }
}
